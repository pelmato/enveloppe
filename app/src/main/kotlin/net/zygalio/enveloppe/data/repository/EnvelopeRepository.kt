package net.zygalio.enveloppe.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.zygalio.enveloppe.data.db.dao.CategoryDao
import net.zygalio.enveloppe.data.db.dao.EnvelopeDao
import net.zygalio.enveloppe.data.db.dao.ExpenseDao
import net.zygalio.enveloppe.data.db.entity.CategoryEntity
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.domain.model.Category
import net.zygalio.enveloppe.domain.model.CategoryBreakdown
import net.zygalio.enveloppe.domain.model.EnvelopeDetail
import net.zygalio.enveloppe.domain.model.EnvelopeSummary
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnvelopeRepository @Inject constructor(
    private val envelopeDao: EnvelopeDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
) {
    fun getSummaries(): Flow<List<EnvelopeSummary>> =
        envelopeDao.getSummariesFlow(todayStartMs()).map { list ->
            list.map { db ->
                val daysLeft = daysLeft(db.endDate)
                val remaining = db.budget - db.totalConsumed
                val dailyBudget = remaining / (daysLeft + 1)
                EnvelopeSummary(
                    id = db.id,
                    name = db.name,
                    budget = db.budget,
                    endDate = db.endDate,
                    consumed = db.totalConsumed,
                    dailyRemaining = dailyBudget - db.todayConsumed,
                )
            }
        }

    suspend fun getDetail(envelopeId: Long): EnvelopeDetail? {
        val entity = envelopeDao.getById(envelopeId) ?: return null
        val categories = categoryDao.getByEnvelope(envelopeId).map { it.toDomain() }
        val consumed = expenseDao.totalByEnvelope(envelopeId)
        val todayConsumed = expenseDao.todayTotalByEnvelope(envelopeId, todayStartMs())
        val daysLeft = daysLeft(entity.endDate)
        val remaining = entity.budget - consumed
        val dailyBudget = remaining / (daysLeft + 1)
        return EnvelopeDetail(
            id = entity.id,
            name = entity.name,
            budget = entity.budget,
            endDate = entity.endDate,
            categories = categories,
            consumed = consumed,
            dailyBudget = dailyBudget,
            dailyRemaining = dailyBudget - todayConsumed,
        )
    }

    suspend fun getCategoryBreakdown(envelopeId: Long): List<CategoryBreakdown> {
        val totals = expenseDao.totalsByCategory(envelopeId)
        val categories = categoryDao.getByEnvelope(envelopeId).associateBy { it.id }
        return totals.mapNotNull { ct ->
            val name = categories[ct.categoryId]?.name ?: return@mapNotNull null
            CategoryBreakdown(name, ct.total)
        }.sortedByDescending { it.total }
    }

    suspend fun getCategories(envelopeId: Long): List<Category> =
        categoryDao.getByEnvelope(envelopeId).map { it.toDomain() }

    suspend fun getById(id: Long): EnvelopeEntity? = envelopeDao.getById(id)

    suspend fun save(
        entity: EnvelopeEntity,
        categoryNames: List<String>,
        existingCategoryIds: List<Long>,
    ): Long {
        val id = envelopeDao.insert(entity.copy(id = if (entity.id == 0L) 0L else entity.id))
        syncCategories(id, categoryNames, existingCategoryIds)
        return id
    }

    suspend fun duplicate(sourceId: Long): Long? {
        val source = envelopeDao.getById(sourceId) ?: return null
        val newId = envelopeDao.insert(source.copy(id = 0, name = "Copie de ${source.name}"))
        val sourceCategories = categoryDao.getByEnvelope(sourceId)
        categoryDao.insertAll(sourceCategories.map { it.copy(id = 0, envelopeId = newId) })
        return newId
    }

    suspend fun delete(envelopeId: Long) {
        envelopeDao.getById(envelopeId)?.let { envelopeDao.delete(it) }
    }

    suspend fun canDeleteCategory(categoryId: Long): Boolean =
        categoryDao.expenseCount(categoryId) == 0

    suspend fun deleteCategory(category: Category): Boolean {
        if (!canDeleteCategory(category.id)) return false
        categoryDao.delete(category.toEntity())
        return true
    }

    private suspend fun syncCategories(
        envelopeId: Long,
        newNames: List<String>,
        existingIds: List<Long>,
    ) {
        val existing = categoryDao.getByEnvelope(envelopeId)
        val toDelete = existing.filter { it.id !in existingIds }
        toDelete.forEach { categoryDao.delete(it) }
        newNames.forEachIndexed { index, name ->
            val existingId = existingIds.getOrNull(index)
            if (existingId != null && existingId != 0L) {
                categoryDao.update(CategoryEntity(existingId, envelopeId, name))
            } else {
                categoryDao.insert(CategoryEntity(envelopeId = envelopeId, name = name))
            }
        }
    }

    private fun todayStartMs(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun daysLeft(endDate: Long): Int {
        val diff = endDate - todayStartMs()
        return maxOf(0, (diff / (1000L * 60 * 60 * 24)).toInt())
    }
}

private fun CategoryEntity.toDomain() = Category(id, envelopeId, name)
private fun Category.toEntity() = CategoryEntity(id, envelopeId, name)
