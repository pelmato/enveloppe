package net.zygalio.enveloppe.data.db.model

data class EnvelopeSummaryDb(
    val id: Long,
    val name: String,
    val budget: Double,
    val endDate: Long,
    val totalConsumed: Double,
    val todayConsumed: Double,
    val lastExpenseDate: Long?,
)
