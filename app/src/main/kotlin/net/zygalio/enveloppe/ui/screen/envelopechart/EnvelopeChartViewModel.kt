package net.zygalio.enveloppe.ui.screen.envelopechart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import net.zygalio.enveloppe.domain.model.CategoryBreakdown
import javax.inject.Inject

@HiltViewModel
class EnvelopeChartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val envelopeRepository: EnvelopeRepository,
) : ViewModel() {

    private val envelopeId: Long = checkNotNull(savedStateHandle["envelopeId"])

    private val _breakdown = MutableStateFlow<List<CategoryBreakdown>>(emptyList())
    val breakdown: StateFlow<List<CategoryBreakdown>> = _breakdown.asStateFlow()

    init {
        viewModelScope.launch {
            _breakdown.value = envelopeRepository.getCategoryBreakdown(envelopeId)
        }
    }
}
