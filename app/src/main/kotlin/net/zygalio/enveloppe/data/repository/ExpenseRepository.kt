package net.zygalio.enveloppe.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.zygalio.enveloppe.data.db.dao.CategoryDao
import net.zygalio.enveloppe.data.db.dao.ExpenseDao
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import net.zygalio.enveloppe.domain.model.Expense
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: CategoryDao,
) {
    fun getByEnvelope(envelopeId: Long): Flow<List<Expense>> =
        expenseDao.getByEnvelopeFlow(envelopeId).map { entities ->
            val categories = categoryDao.getByEnvelope(envelopeId).associateBy { it.id }
            entities.map { e ->
                Expense(
                    id = e.id,
                    envelopeId = e.envelopeId,
                    categoryId = e.categoryId,
                    categoryName = e.categoryId?.let { categories[it]?.name },
                    name = e.name,
                    amount = e.amount,
                    dateTime = e.dateTime,
                )
            }
        }

    suspend fun getById(id: Long): ExpenseEntity? = expenseDao.getById(id)

    suspend fun save(expense: ExpenseEntity): Long = expenseDao.insert(expense)

    suspend fun delete(expenseId: Long) {
        expenseDao.getById(expenseId)?.let { expenseDao.delete(it) }
    }
}
