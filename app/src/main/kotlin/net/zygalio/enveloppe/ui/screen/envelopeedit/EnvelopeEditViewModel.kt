package net.zygalio.enveloppe.ui.screen.envelopeedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.zygalio.enveloppe.data.db.entity.EnvelopeEntity
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import javax.inject.Inject

data class CategoryEntry(val id: Long = 0L, val name: String)

data class EnvelopeEditUiState(
    val name: String = "",
    val budget: String = "",
    val endDate: Long? = null,
    val categories: List<CategoryEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EnvelopeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EnvelopeRepository,
) : ViewModel() {

    private val envelopeId: Long = savedStateHandle.get<Long>("envelopeId") ?: -1L
    val isEditing: Boolean get() = envelopeId > 0

    private val _uiState = MutableStateFlow(EnvelopeEditUiState())
    val uiState: StateFlow<EnvelopeEditUiState> = _uiState.asStateFlow()

    init {
        if (isEditing) loadEnvelope() else _uiState.update { it.copy(isLoading = false) }
    }

    private fun loadEnvelope() {
        viewModelScope.launch {
            val entity = repository.getById(envelopeId)
            val categories = repository.getCategories(envelopeId)
            if (entity != null) {
                _uiState.update {
                    it.copy(
                        name = entity.name,
                        budget = entity.budget.let { b ->
                            if (b % 1.0 == 0.0) b.toLong().toString() else "%.2f".format(b)
                        },
                        endDate = entity.endDate,
                        categories = categories.map { c -> CategoryEntry(c.id, c.name) },
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value) }
    fun setBudget(value: String) = _uiState.update { it.copy(budget = value) }
    fun setEndDate(value: Long) = _uiState.update { it.copy(endDate = value) }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        _uiState.update { it.copy(categories = it.categories + CategoryEntry(name = name)) }
    }

    fun updateCategory(index: Int, name: String) {
        _uiState.update {
            val updated = it.categories.toMutableList()
            updated[index] = updated[index].copy(name = name)
            it.copy(categories = updated)
        }
    }

    fun removeCategory(index: Int) {
        _uiState.update {
            val updated = it.categories.toMutableList()
            updated.removeAt(index)
            it.copy(categories = updated)
        }
    }

    fun save() {
        val state = _uiState.value
        val budget = state.budget.replace(',', '.').toDoubleOrNull()
        val endDate = state.endDate

        if (state.name.isBlank() || budget == null || budget <= 0 || endDate == null) {
            _uiState.update { it.copy(error = "Champs obligatoires manquants ou invalides") }
            return
        }

        viewModelScope.launch {
            try {
                val entity = EnvelopeEntity(
                    id = if (isEditing) envelopeId else 0L,
                    name = state.name.trim(),
                    budget = budget,
                    endDate = endDate,
                )
                repository.save(
                    entity = entity,
                    categoryNames = state.categories.map { it.name },
                    existingCategoryIds = state.categories.map { it.id },
                )
                _uiState.update { it.copy(isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun duplicate() {
        if (!isEditing) return
        viewModelScope.launch {
            repository.duplicate(envelopeId)
            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun delete() {
        if (!isEditing) return
        viewModelScope.launch {
            repository.delete(envelopeId)
            _uiState.update { it.copy(isDeleted = true) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
