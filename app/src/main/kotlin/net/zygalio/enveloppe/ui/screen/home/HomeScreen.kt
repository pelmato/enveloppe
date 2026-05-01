package net.zygalio.enveloppe.ui.screen.home

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.zygalio.enveloppe.domain.model.EnvelopeSummary
import net.zygalio.enveloppe.ui.component.BudgetProgressBar
import net.zygalio.enveloppe.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNewEnvelope: () -> Unit,
    onEnvelopeClick: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val envelopes by viewModel.envelopes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Enveloppes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onNewEnvelope) {
                Icon(Icons.Default.Add, contentDescription = "Nouvelle enveloppe")
            }
        },
    ) { padding ->
        if (envelopes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("Aucune enveloppe", style = MaterialTheme.typography.bodyLarge)
                Text("Appuyez sur + pour en créer une", style = MaterialTheme.typography.labelMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(envelopes, key = { it.id }) { envelope ->
                    EnvelopeCard(envelope = envelope, onClick = { onEnvelopeClick(envelope.id) })
                }
            }
        }
    }
}

@Composable
private fun EnvelopeCard(
    envelope: EnvelopeSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = envelope.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${envelope.consumed.formatMoney()} / ${envelope.budget.formatMoney()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            BudgetProgressBar(progress = envelope.progress)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Restant aujourd'hui : ${envelope.dailyRemaining.formatMoney()}",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
