package net.zygalio.enveloppe.ui.screen.expenseedit

import androidx.lifecycle.SavedStateHandle
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import net.zygalio.enveloppe.data.repository.ExpenseRepository
import net.zygalio.enveloppe.domain.model.Category
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        envelopeEndDate: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
        categories: List<Category> = emptyList(),
    ): ExpenseEditViewModel {
        val envelopeRepo = mockk<EnvelopeRepository>()
        val expenseRepo = mockk<ExpenseRepository>(relaxed = true)
        coEvery { envelopeRepo.getCategories(any()) } returns categories
        coEvery { envelopeRepo.getById(any()) } returns
            EnvelopeEntity(1L, "Test", 100.0, envelopeEndDate)
        return ExpenseEditViewModel(
            SavedStateHandle(mapOf("envelopeId" to 1L)),
            expenseRepo,
            envelopeRepo,
        )
    }

    // --- Validation du montant ---

    @Test
    fun `montant vide produit une erreur`() {
        val vm = createViewModel()
        vm.setAmount("")
        vm.setName("Courses")
        vm.save()
        assertEquals("Montant invalide", vm.uiState.value.error)
    }

    @Test
    fun `montant négatif produit une erreur`() {
        val vm = createViewModel()
        vm.setAmount("-5")
        vm.setName("Courses")
        vm.save()
        assertEquals("Montant invalide", vm.uiState.value.error)
    }

    @Test
    fun `montant zéro produit une erreur`() {
        val vm = createViewModel()
        vm.setAmount("0")
        vm.setName("Courses")
        vm.save()
        assertEquals("Montant invalide", vm.uiState.value.error)
    }

    // --- Nom obligatoire si pas de catégorie ---

    @Test
    fun `nom vide sans catégorie produit une erreur`() {
        val vm = createViewModel(categories = emptyList())
        vm.setAmount("10")
        vm.setName("")
        vm.save()
        assertEquals("Le nom est obligatoire", vm.uiState.value.error)
    }

    @Test
    fun `nom renseigné sans catégorie est valide`() {
        val vm = createViewModel(categories = emptyList())
        vm.setAmount("10")
        vm.setName("Courses")
        vm.save()
        assertNull(vm.uiState.value.error)
    }

    // --- Catégorie obligatoire si l'enveloppe en a ---

    @Test
    fun `aucune catégorie sélectionnée quand l'enveloppe en a produit une erreur`() {
        val cats = listOf(Category(1L, 1L, "Nourriture"))
        val vm = createViewModel(categories = cats)
        vm.setAmount("10")
        vm.setCategoryId(null)
        vm.save()
        assertEquals("Veuillez choisir une catégorie", vm.uiState.value.error)
    }

    @Test
    fun `catégorie sélectionnée quand l'enveloppe en a est valide`() {
        val cats = listOf(Category(1L, 1L, "Nourriture"))
        val vm = createViewModel(categories = cats)
        vm.setAmount("10")
        vm.setCategoryId(1L)
        vm.save()
        assertNull(vm.uiState.value.error)
    }

    // --- Contraintes de date ---

    @Test
    fun `date dans le futur produit une erreur`() {
        val vm = createViewModel()
        vm.setAmount("10")
        vm.setName("Courses")
        vm.setDateTime(System.currentTimeMillis() + 60_000L)
        vm.save()
        assertEquals("La date ne peut pas être dans le futur", vm.uiState.value.error)
    }

    @Test
    fun `date postérieure à la date de fin de l'enveloppe produit une erreur`() {
        val now = System.currentTimeMillis()
        // Enveloppe expirée hier ; la dépense est datée d'il y a 1h (passé, mais après la fin)
        val endDate = now - 24 * 3600 * 1000L
        val expenseDate = now - 3600 * 1000L
        val vm = createViewModel(envelopeEndDate = endDate)
        vm.setAmount("10")
        vm.setName("Courses")
        vm.setDateTime(expenseDate)
        vm.save()
        assertEquals("La date dépasse la date de fin de l'enveloppe", vm.uiState.value.error)
    }

    @Test
    fun `données valides passent la validation et marquent isSaved`() {
        val vm = createViewModel()
        vm.setAmount("10.50")
        vm.setName("Courses")
        vm.setDateTime(System.currentTimeMillis() - 60_000L)
        vm.save()
        assertNull(vm.uiState.value.error)
        assertTrue(vm.uiState.value.isSaved)
    }

    // --- clearError ---

    @Test
    fun `clearError supprime le message d'erreur`() {
        val vm = createViewModel()
        vm.setAmount("")
        vm.save()
        vm.clearError()
        assertNull(vm.uiState.value.error)
    }
}
