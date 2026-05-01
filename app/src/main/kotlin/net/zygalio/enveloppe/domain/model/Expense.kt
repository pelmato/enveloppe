package net.zygalio.enveloppe.domain.model

data class Expense(
    val id: Long = 0,
    val envelopeId: Long,
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val name: String? = null,
    val amount: Double,
    val dateTime: Long,
)
