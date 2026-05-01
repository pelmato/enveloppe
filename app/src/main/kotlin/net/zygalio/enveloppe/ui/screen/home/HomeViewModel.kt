package net.zygalio.enveloppe.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import net.zygalio.enveloppe.data.repository.EnvelopeRepository
import net.zygalio.enveloppe.domain.model.EnvelopeSummary
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: EnvelopeRepository,
) : ViewModel() {

    val envelopes: StateFlow<List<EnvelopeSummary>> = repository
        .getSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
