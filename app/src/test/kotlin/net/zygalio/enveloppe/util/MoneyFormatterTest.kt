package net.zygalio.enveloppe.util

import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {

    @Test
    fun `les montants entiers n'affichent pas de décimales`() {
        assertEquals("0", 0.0.formatMoney())
        assertEquals("10", 10.0.formatMoney())
        assertEquals("100", 100.0.formatMoney())
        assertEquals("1000", 1000.0.formatMoney())
    }

    @Test
    fun `les centimes non nuls s'affichent avec exactement 2 décimales`() {
        // Le séparateur décimal suit la locale système (virgule en français)
        val sep = (1.5).let { "%.2f".format(it)[1].toString() } // "," ou "."
        assertEquals("10${sep}50", 10.5.formatMoney())
        assertEquals("10${sep}55", 10.55.formatMoney())
        assertEquals("10${sep}10", 10.1.formatMoney())
        assertEquals("0${sep}50", 0.5.formatMoney())
        assertEquals("0${sep}01", 0.01.formatMoney())
    }

    @Test
    fun `les valeurs sont arrondies à 2 décimales`() {
        val sep = (1.5).let { "%.2f".format(it)[1].toString() }
        assertEquals("10${sep}01", 10.005.formatMoney())
        assertEquals("9${sep}99", 9.994.formatMoney())
    }
}
