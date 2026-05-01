package net.zygalio.enveloppe.ui.screen.envelopedetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.zygalio.enveloppe.domain.model.Expense
import net.zygalio.enveloppe.ui.component.BudgetProgressBar
import net.zygalio.enveloppe.ui.component.PieChart
import net.zygalio.enveloppe.util.formatDate
import net.zygalio.enveloppe.util.formatDateTime
import net.zygalio.enveloppe.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnvelopeDetailScreen(
    onBack: () -> Unit,
    onEditEnvelope: (Long) -> Unit,
    onNewExpense: (Long) -> Unit,
    onEditExpense: (Long, Long) -> Unit,
    viewModel: EnvelopeDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val detail = state.detail

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(detail?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    if (detail != null) {
                        IconButton(onClick = { onEditEnvelope(detail.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Modifier")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            if (detail != null) {
                FloatingActionButton(onClick = { onNewExpense(detail.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Nouvelle dépense")
                }
            }
        },
    ) { padding ->
        if (detail == null) return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Budget", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "${detail.consumed.formatMoney()} / ${detail.budget.formatMoney()}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        BudgetProgressBar(progress = detail.progress)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text("Budget journalier", style = MaterialTheme.typography.labelMedium)
                                Text(detail.dailyBudget.formatMoney(), style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Restant aujourd'hui", style = MaterialTheme.typography.labelMedium)
                                Text(detail.dailyRemaining.formatMoney(), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Restant total", style = MaterialTheme.typography.labelMedium)
                            Text(detail.remaining.formatMoney(), style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Date de fin", style = MaterialTheme.typography.labelMedium)
                            Text(detail.endDate.formatDate(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            if (state.breakdown.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Répartition par catégorie", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(12.dp))
                            PieChart(data = state.breakdown)
                        }
                    }
                }
            }

            if (state.expenses.isNotEmpty()) {
                item {
                    Text("Dépenses", style = MaterialTheme.typography.titleSmall)
                }
                items(state.expenses, key = { it.id }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        onClick = { onEditExpense(detail.id, expense.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val label = expense.categoryName ?: expense.name ?: ""
            if (label.isNotEmpty()) {
                if (expense.categoryName != null) {
                    SuggestionChip(onClick = {}, label = { Text(label) })
                } else {
                    Text(label, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                expense.dateTime.formatDateTime(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            expense.amount.formatMoney(),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    HorizontalDivider()
}
