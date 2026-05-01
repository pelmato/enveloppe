package net.zygalio.enveloppe.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.zygalio.enveloppe.data.db.AppDatabase
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnvelopeRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: EnvelopeRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = EnvelopeRepository(db.envelopeDao(), db.categoryDao(), db.expenseDao())
    }

    @After
    fun tearDown() = db.close()

    // --- Suppression d'une catégorie ---

    @Test
    fun canDeleteCategory_vrai_quand_aucune_depense_ne_la_reference() = runTest {
        val envId = saveEnvelope(categoryNames = listOf("Nourriture"))
        val catId = repository.getCategories(envId).first().id
        assertTrue(repository.canDeleteCategory(catId))
    }

    @Test
    fun canDeleteCategory_faux_quand_une_depense_la_reference() = runTest {
        val envId = saveEnvelope(categoryNames = listOf("Nourriture"))
        val catId = repository.getCategories(envId).first().id
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, categoryId = catId, amount = 10.0, dateTime = 1_000L))
        assertFalse(repository.canDeleteCategory(catId))
    }

    @Test
    fun deleteCategory_reussit_et_supprime_la_categorie() = runTest {
        val envId = saveEnvelope(categoryNames = listOf("Nourriture"))
        val category = repository.getCategories(envId).first()
        assertTrue(repository.deleteCategory(category))
        assertTrue(repository.getCategories(envId).isEmpty())
    }

    @Test
    fun deleteCategory_echoue_quand_une_depense_la_reference() = runTest {
        val envId = saveEnvelope(categoryNames = listOf("Nourriture"))
        val category = repository.getCategories(envId).first()
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, categoryId = category.id, amount = 10.0, dateTime = 1_000L))
        assertFalse(repository.deleteCategory(category))
        assertEquals(1, repository.getCategories(envId).size) // toujours présente
    }

    // --- Suppression d'une enveloppe ---

    @Test
    fun delete_supprime_l_enveloppe_et_ses_depenses() = runTest {
        val envId = saveEnvelope()
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, amount = 10.0, dateTime = 1_000L))
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, amount = 20.0, dateTime = 2_000L))

        repository.delete(envId)

        assertNull(repository.getById(envId))
        assertEquals(0.0, db.expenseDao().totalByEnvelope(envId), 0.001)
    }

    // --- Duplication ---

    @Test
    fun duplicate_cree_nouvelle_enveloppe_avec_meme_budget_et_categories() = runTest {
        val originalId = saveEnvelope(budget = 200.0, categoryNames = listOf("Nourriture", "Transport"))

        val newId = repository.duplicate(originalId)!!

        assertNotEquals(originalId, newId)
        assertEquals(200.0, repository.getById(newId)!!.budget, 0.001)
        val newCategories = repository.getCategories(newId).map { it.name }.toSet()
        assertEquals(setOf("Nourriture", "Transport"), newCategories)
    }

    @Test
    fun duplicate_retourne_null_si_enveloppe_inexistante() = runTest {
        assertNull(repository.duplicate(9_999L))
    }

    // --- Calculs budgétaires ---

    @Test
    fun getDetail_consumed_egal_somme_des_depenses() = runTest {
        val envId = saveEnvelope(budget = 100.0)
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, amount = 10.0, dateTime = 1_000L))
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, amount = 25.5, dateTime = 2_000L))

        val detail = repository.getDetail(envId)!!
        assertEquals(35.5, detail.consumed, 0.001)
        assertEquals(64.5, detail.remaining, 0.001) // 100 - 35.5
    }

    @Test
    fun getDetail_dailyBudget_egal_remaining_quand_enveloppe_expiree() = runTest {
        // daysLeft = 0 (endDate dans le passé) → dailyBudget = remaining / 1 = remaining
        val envId = saveEnvelope(budget = 100.0, endDate = 1_000L) // passé lointain
        db.expenseDao().insert(ExpenseEntity(envelopeId = envId, amount = 30.0, dateTime = 1_000L))

        val detail = repository.getDetail(envId)!!
        assertEquals(70.0, detail.dailyBudget, 0.001)
    }

    @Test
    fun getDetail_retourne_null_pour_enveloppe_inexistante() = runTest {
        assertNull(repository.getDetail(9_999L))
    }

    // --- Helpers ---

    private suspend fun saveEnvelope(
        name: String = "Test",
        budget: Double = 100.0,
        endDate: Long = 9_999_999_999L,
        categoryNames: List<String> = emptyList(),
    ): Long = repository.save(
        EnvelopeEntity(name = name, budget = budget, endDate = endDate),
        categoryNames,
        List(categoryNames.size) { 0L },
    )
}
