package net.zygalio.enveloppe.ui.screen.envelopeedit

import androidx.lifecycle.SavedStateHandle
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EnvelopeEditViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EnvelopeEditViewModel {
        // Création (pas d'envelopeId) — aucun appel repository attendu
        val repository = mockk<EnvelopeRepository>(relaxed = true)
        return EnvelopeEditViewModel(SavedStateHandle(), repository)
    }

    @Test
    fun `nom vide produit une erreur`() {
        val vm = createViewModel()
        vm.setName("")
        vm.setBudget("100")
        vm.setEndDate(System.currentTimeMillis() + 86400_000L)
        vm.save()
        assertEquals("Champs obligatoires manquants ou invalides", vm.uiState.value.error)
    }

    @Test
    fun `budget absent produit une erreur`() {
        val vm = createViewModel()
        vm.setName("Courses")
        vm.setBudget("")
        vm.setEndDate(System.currentTimeMillis() + 86400_000L)
        vm.save()
        assertEquals("Champs obligatoires manquants ou invalides", vm.uiState.value.error)
    }

    @Test
    fun `budget négatif ou nul produit une erreur`() {
        val vm = createViewModel()
        vm.setName("Courses")
        vm.setBudget("0")
        vm.setEndDate(System.currentTimeMillis() + 86400_000L)
        vm.save()
        assertEquals("Champs obligatoires manquants ou invalides", vm.uiState.value.error)
    }

    @Test
    fun `date de fin absente produit une erreur`() {
        val vm = createViewModel()
        vm.setName("Courses")
        vm.setBudget("100")
        // endDate non renseignée (null par défaut)
        vm.save()
        assertEquals("Champs obligatoires manquants ou invalides", vm.uiState.value.error)
    }

    @Test
    fun `données valides ne produisent pas d'erreur`() {
        val vm = createViewModel()
        vm.setName("Courses")
        vm.setBudget("100")
        vm.setEndDate(System.currentTimeMillis() + 86400_000L)
        vm.save()
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `addCategory ajoute la catégorie à la liste`() {
        val vm = createViewModel()
        vm.addCategory("Nourriture")
        vm.addCategory("Transport")
        assertEquals(2, vm.uiState.value.categories.size)
        assertEquals("Nourriture", vm.uiState.value.categories[0].name)
        assertEquals("Transport", vm.uiState.value.categories[1].name)
    }

    @Test
    fun `updateCategory renomme la catégorie`() {
        val vm = createViewModel()
        vm.addCategory("Ancien nom")
        vm.updateCategory(0, "Nouveau nom")
        assertEquals("Nouveau nom", vm.uiState.value.categories[0].name)
    }

    @Test
    fun `removeCategory supprime la catégorie`() {
        val vm = createViewModel()
        vm.addCategory("Nourriture")
        vm.addCategory("Transport")
        vm.removeCategory(0)
        assertEquals(1, vm.uiState.value.categories.size)
        assertEquals("Transport", vm.uiState.value.categories[0].name)
    }
}
