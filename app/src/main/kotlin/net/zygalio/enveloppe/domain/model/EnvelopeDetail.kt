package net.zygalio.enveloppe.domain.model

data class EnvelopeDetail(
    val id: Long,
    val name: String,
    val budget: Double,
    val endDate: Long,
    val categories: List<Category>,
    val consumed: Double,
    val dailyBudget: Double,
    val dailyRemaining: Double,
) {
    val remaining: Double get() = budget - consumed
    val progress: Float get() = if (budget > 0) (consumed / budget).toFloat().coerceIn(0f, 1f) else 0f
}
