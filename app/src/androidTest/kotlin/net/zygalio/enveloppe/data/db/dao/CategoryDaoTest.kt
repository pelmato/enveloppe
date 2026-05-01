package net.zygalio.enveloppe.data.db.dao

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import net.zygalio.enveloppe.data.db.AppDatabase
import net.zygalio.enveloppe.data.db.entity.CategoryEntity
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

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

    // --- Appartenance à une enveloppe ---

    @Test
    fun categorie_appartient_a_son_enveloppe() = runTest {
        val env1 = envelopeDao.insert(EnvelopeEntity(name = "E1", budget = 100.0, endDate = 9_999_999_999L))
        val env2 = envelopeDao.insert(EnvelopeEntity(name = "E2", budget = 100.0, endDate = 9_999_999_999L))
        categoryDao.insert(CategoryEntity(envelopeId = env1, name = "Cat A"))
        categoryDao.insert(CategoryEntity(envelopeId = env2, name = "Cat B"))

        val cats = categoryDao.getByEnvelope(env1)
        assertEquals(1, cats.size)
        assertEquals("Cat A", cats.first().name)
    }

    // --- Comptage de dépenses ---

    @Test
    fun expenseCount_vaut_zero_sans_depense_associee() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catId = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "Nourriture"))
        assertEquals(0, categoryDao.expenseCount(catId))
    }

    @Test
    fun expenseCount_reflète_le_nombre_de_depenses_associees() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catId = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "Nourriture"))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catId, amount = 10.0, dateTime = 1_000L))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catId, amount = 5.0, dateTime = 2_000L))
        assertEquals(2, categoryDao.expenseCount(catId))
    }

    // --- Renommage ---

    @Test
    fun categorie_peut_etre_renommee() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catId = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "Ancien nom"))
        categoryDao.update(CategoryEntity(id = catId, envelopeId = envId, name = "Nouveau nom"))
        assertEquals("Nouveau nom", categoryDao.getByEnvelope(envId).first().name)
    }

    // --- Contrainte de suppression ---

    @Test
    fun supprimer_categorie_non_referencee_reussit() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catId = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "Nourriture"))
        categoryDao.delete(CategoryEntity(id = catId, envelopeId = envId, name = "Nourriture"))
        assertTrue(categoryDao.getByEnvelope(envId).isEmpty())
    }

    @Test
    fun supprimer_categorie_referencee_par_une_depense_leve_une_exception() = runTest {
        val envId = envelopeDao.insert(EnvelopeEntity(name = "Test", budget = 100.0, endDate = 9_999_999_999L))
        val catId = categoryDao.insert(CategoryEntity(envelopeId = envId, name = "Nourriture"))
        expenseDao.insert(ExpenseEntity(envelopeId = envId, categoryId = catId, amount = 10.0, dateTime = 1_000L))

        try {
            categoryDao.delete(CategoryEntity(id = catId, envelopeId = envId, name = "Nourriture"))
            fail("SQLiteConstraintException attendue (FK RESTRICT)")
        } catch (e: SQLiteConstraintException) {
            // Attendu : la clé étrangère RESTRICT empêche la suppression
        }
    }
}
