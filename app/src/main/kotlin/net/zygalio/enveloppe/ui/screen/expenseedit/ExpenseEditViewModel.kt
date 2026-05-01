package net.zygalio.enveloppe.ui.screen.expenseedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.zygalio.enveloppe.data.db.entity.ExpenseEntity
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import net.zygalio.enveloppe.data.repository.ExpenseRepository
import net.zygalio.enveloppe.domain.model.Category
import javax.inject.Inject

data class ExpenseEditUiState(
    val amount: String = "",
    val dateTime: Long = System.currentTimeMillis(),
    val categoryId: Long? = null,
    val name: String = "",
    val categories: List<Category> = emptyList(),
    val envelopeEndDate: Long = Long.MAX_VALUE,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val expenseRepository: ExpenseRepository,
    private val envelopeRepository: EnvelopeRepository,
) : ViewModel() {

    val envelopeId: Long = checkNotNull(savedStateHandle["envelopeId"])
    private val expenseId: Long = savedStateHandle.get<Long>("expenseId") ?: -1L
    val isEditing: Boolean get() = expenseId > 0

    private val _uiState = MutableStateFlow(ExpenseEditUiState())
    val uiState: StateFlow<ExpenseEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val categories = envelopeRepository.getCategories(envelopeId)
            val endDate = envelopeRepository.getById(envelopeId)?.endDate ?: Long.MAX_VALUE
            _uiState.update { it.copy(categories = categories, envelopeEndDate = endDate) }

            if (isEditing) {
                val entity = expenseRepository.getById(expenseId)
                if (entity != null) {
                    _uiState.update {
                        it.copy(
                            amount = entity.amount.let { a ->
                                if (a % 1.0 == 0.0) a.toLong().toString() else "%.2f".format(a)
                            },
                            dateTime = entity.dateTime,
                            categoryId = entity.categoryId,
                            name = entity.name ?: "",
                            isLoading = false,
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setAmount(value: String) = _uiState.update { it.copy(amount = value) }
    fun setDateTime(value: Long) = _uiState.update { it.copy(dateTime = value) }
    fun setCategoryId(value: Long?) = _uiState.update { it.copy(categoryId = value) }
    fun setName(value: String) = _uiState.update { it.copy(name = value) }

    fun save() {
        val state = _uiState.value
        val amount = state.amount.replace(',', '.').toDoubleOrNull()
        val hasCategories = state.categories.isNotEmpty()

        if (amount == null || amount <= 0) {
            _uiState.update { it.copy(error = "Montant invalide") }
            return
        }
        if (hasCategories && state.categoryId == null) {
            _uiState.update { it.copy(error = "Veuillez choisir une catégorie") }
            return
        }
        if (!hasCategories && state.name.isBlank()) {
            _uiState.update { it.copy(error = "Le nom est obligatoire") }
            return
        }
        val now = System.currentTimeMillis()
        if (state.dateTime > now) {
            _uiState.update { it.copy(error = "La date ne peut pas être dans le futur") }
            return
        }
        if (state.dateTime > state.envelopeEndDate) {
            _uiState.update { it.copy(error = "La date dépasse la date de fin de l'enveloppe") }
            return
        }

        viewModelScope.launch {
            try {
                val entity = ExpenseEntity(
                    id = if (isEditing) expenseId else 0L,
                    envelopeId = envelopeId,
                    categoryId = if (hasCategories) state.categoryId else null,
                    name = if (!hasCategories) state.name.trim() else null,
                    amount = amount,
                    dateTime = state.dateTime,
                )
                expenseRepository.save(entity)
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun delete() {
        if (!isEditing) return
        viewModelScope.launch {
            expenseRepository.delete(expenseId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
