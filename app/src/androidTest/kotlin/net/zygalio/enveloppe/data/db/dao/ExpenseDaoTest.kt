package net.zygalio.enveloppe.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.zygalio.enveloppe.data.db.AppDatabase
import net.zygalio.enveloppe.data.db.entity.CategoryEntity
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var envelopeDao: EnvelopeDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        envelopeDao = db.envelopeDao()
        categoryDao = db.categoryDao()
        expenseDao = db.expenseDao()
    }

    @After
    fun tearDown() = db.close()

    // --- Tri par date décroissante ---

    @Test
    fun depenses_triees_par_date_decroissante() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 20.0, dateTime = 3_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 30.0, dateTime = 2_000L))

        val expenses = expenseDao.getByEnvelopeFlow(envId).first()
        assertEquals(3_000L, expenses[0].dateTime)
        assertEquals(2_000L, expenses[1].dateTime)
        assertEquals(1_000L, expenses[2].dateTime)
    }

    // --- budget consommé = somme de toutes les dépenses ---

    @Test
    fun totalByEnvelope_somme_toutes_les_depenses() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 25.5, dateTime = 2_000L))
        assertEquals(35.5, expenseDao.totalByEnvelope(envId), 0.001)
    }

    @Test
    fun totalByEnvelope_vaut_zero_sans_depenses() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        assertEquals(0.0, expenseDao.totalByEnvelope(envId), 0.001)
    }

    // --- budget journalier restant ---

    @Test
    fun todayTotal_inclut_uniquement_les_depenses_a_partir_de_todayStart() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val todayStart = 10_000L
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 5.0, dateTime = 9_999L))    // avant
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 15.0, dateTime = 10_000L))  // début du jour (inclus)
        expenseDao.insert(ExpenseEntity(envelopeId = envId, amount = 20.0, dateTime = 50_000L))  // pendant le jour

        assertEquals(35.0, expenseDao.todayTotalByEnvelope(envId, todayStart), 0.001)
    }

    // --- Totaux par catégorie ---

    @Test
    fun totalsByCategory_regroupe_par_categorie() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catA = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "A"))
        val catB = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "B"))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catA, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catA, amount = 5.0, dateTime = 2_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catB, amount = 20.0, dateTime = 3_000L))

        val totals = expenseDao.totalsByCategory(envId)
        assertEquals(15.0, totals.first { it.categoryId == catA }.total, 0.001)
        assertEquals(20.0, totals.first { it.categoryId == catB }.total, 0.001)
    }

    @Test
    fun totalsByCategory_exclut_les_depenses_sans_categorie() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catA = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "A"))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catA, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = null, amount = 5.0, dateTime = 2_000L))

        val totals = expenseDao.totalsByCategory(envId)
        assertEquals(1, totals.size)
        assertEquals(10.0, totals.first().total, 0.001)
    }
}
