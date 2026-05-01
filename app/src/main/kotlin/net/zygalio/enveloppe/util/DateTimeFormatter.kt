package net.zygalio.enveloppe.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

fun Long.formatDate(): String = dateFormat.format(Date(this))
fun Long.formatDateTime(): String = dateTimeFormat.format(Date(this))
