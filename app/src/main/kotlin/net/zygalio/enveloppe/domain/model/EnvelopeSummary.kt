package net.zygalio.enveloppe.domain.model

data class EnvelopeSummary(
    val id: Long,
    val name: String,
    val budget: Double,
    val endDate: Long,
    val consumed: Double,
    val dailyRemaining: Double,
) {
    val progress: Float get() = if (budget > 0) (consumed / budget).toFloat().coerceIn(0f, 1f) else 0f
}
