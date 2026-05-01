package net.zygalio.enveloppe.util

fun Double.formatMoney(): String {
    val rounded = Math.round(this * 100) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        "%.2f".format(rounded)
    }
}
