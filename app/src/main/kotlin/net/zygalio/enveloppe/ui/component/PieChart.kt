package net.zygalio.enveloppe.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import net.zygalio.enveloppe.domain.model.CategoryBreakdown
import net.zygalio.enveloppe.ui.theme.PieColors
import net.zygalio.enveloppe.util.formatMoney

@Composable
fun PieChart(
    data: List<CategoryBreakdown>,
    modifier: Modifier = Modifier,
) {
    if (data.isEmpty()) return
    val total = data.sumOf { it.total }
    if (total <= 0) return

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            var startAngle = -90f
            data.forEachIndexed { index, item ->
                val sweep = (item.total / total * 360f).toFloat()
                drawArc(
                    color = PieColors[index % PieColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 40.dp.toPx()),
                )
                startAngle += sweep
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            data.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .let {
                                it // color dot drawn via Canvas below
                            },
                    ) {
                        Canvas(modifier = Modifier.size(12.dp)) {
                            drawCircle(color = PieColors[index % PieColors.size])
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${item.categoryName} — ${item.total.formatMoney()}",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
