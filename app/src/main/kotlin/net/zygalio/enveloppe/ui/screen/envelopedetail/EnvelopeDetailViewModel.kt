package net.zygalio.enveloppe.ui.screen.envelopedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import net.zygalio.enveloppe.data.repository.ExpenseRepository
import net.zygalio.enveloppe.domain.model.CategoryBreakdown
import net.zygalio.enveloppe.domain.model.EnvelopeDetail
import net.zygalio.enveloppe.domain.model.Expense
import javax.inject.Inject

data class EnvelopeDetailUiState(
    val detail: EnvelopeDetail? = null,
    val expenses: List<Expense> = emptyList(),
    val breakdown: List<CategoryBreakdown> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class EnvelopeDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val envelopeRepository: EnvelopeRepository,
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    val envelopeId: Long = checkNotNull(savedStateHandle["envelopeId"])

    private val _uiState = MutableStateFlow(EnvelopeDetailUiState())
    val uiState: StateFlow<EnvelopeDetailUiState> = _uiState.asStateFlow()

    init {
        expenseRepository.getByEnvelope(envelopeId)
            .onEach { expenses ->
                val detail = envelopeRepository.getDetail(envelopeId)
                val breakdown = if (detail?.categories?.isNotEmpty() == true) {
                    envelopeRepository.getCategoryBreakdown(envelopeId)
                } else emptyList()
                _uiState.update {
                    it.copy(
                        detail = detail,
                        expenses = expenses,
                        breakdown = breakdown,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun refresh() {
        viewModelScope.launch {
            val detail = envelopeRepository.getDetail(envelopeId)
            val breakdown = if (detail?.categories?.isNotEmpty() == true) {
                envelopeRepository.getCategoryBreakdown(envelopeId)
            } else emptyList()
            _uiState.update { it.copy(detail = detail, breakdown = breakdown) }
        }
    }
}
