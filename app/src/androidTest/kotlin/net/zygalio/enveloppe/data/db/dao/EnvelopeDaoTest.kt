package net.zygalio.enveloppe.data.db.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.zygalio.enveloppe.data.db.AppDatabase
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EnvelopeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var envelopeDao: EnvelopeDao
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        envelopeDao = db.envelopeDao()
        expenseDao = db.expenseDao()
    }

    @After
    fun tearDown() = db.close()

    // --- Tri par dépense la plus récente ---

    @Test
    fun liste_triee_par_date_de_derniere_depense_decroissante() = runTest {
        val id1 = envelopeDao.insert(EnvelopeEntity(name = "Ancienne", budget = 100.0, endDate = 9_999_999_999L))
        val id2 = envelopeDao.insert(EnvelopeEntity(name = "Récente", budget = 200.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = id1, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = id2, amount = 20.0, dateTime = 2_000L))

        val list = envelopeDao.getSummariesFlow(0L).first()
        assertEquals("Récente", list[0].name)
        assertEquals("Ancienne", list[1].name)
    }

    @Test
    fun enveloppe_sans_depense_arrive_en_dernier() = runTest {
        val idSans = envelopeDao.insert(EnvelopeEntity(name = "Sans dépense", budget = 100.0, endDate = 9_999_999_999L))
        val idAvec = envelopeDao.insert(EnvelopeEntity(name = "Avec dépense", budget = 200.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = idAvec, amount = 20.0, dateTime = 1_000L))

        val list = envelopeDao.getSummariesFlow(0L).first()
        assertEquals("Avec dépense", list[0].name)
        assertEquals("Sans dépense", list[1].name)
    }

    // --- budget consommé = somme des dépenses ---

    @Test
    fun totalConsumed_egal_somme_des_depenses() = runTest {
        val id = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 25.5, dateTime = 2_000L))

        val summary = envelopeDao.getSummariesFlow(0L).first().first()
        assertEquals(35.5, summary.totalConsumed, 0.001)
    }

    @Test
    fun totalConsumed_vaut_zero_sans_depenses() = runTest {
        envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))

        val summary = envelopeDao.getSummariesFlow(0L).first().first()
        assertEquals(0.0, summary.totalConsumed, 0.001)
    }

    // --- budget journalier = dépenses du jour seulement ---

    @Test
    fun todayConsumed_inclut_uniquement_les_depenses_du_jour() = runTest {
        val id = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val todayStart = 10_000L
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 5.0, dateTime = 9_999L))    // avant aujourd'hui
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 15.0, dateTime = 10_000L))  // début du jour (inclus)
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 20.0, dateTime = 50_000L))  // aujourd'hui

        val summary = envelopeDao.getSummariesFlow(todayStart).first().first()
        assertEquals(35.0, summary.todayConsumed, 0.001)
    }

    // --- Suppression en cascade ---

    @Test
    fun supprimer_enveloppe_supprime_ses_depenses() = runTest {
        val id = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        expenseDao.insert(ExpenseEntity(envelopeId = id, amount = 10.0, dateTime = 1_000L))

        envelopeDao.delete(envelopeDao.getById(id)!!)

        assertNull(envelopeDao.getById(id))
        assertEquals(0.0, expenseDao.totalByEnvelope(id), 0.001)
    }
}
