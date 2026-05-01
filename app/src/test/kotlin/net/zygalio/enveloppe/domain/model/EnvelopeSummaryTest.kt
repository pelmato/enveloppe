package net.zygalio.enveloppe.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnvelopeSummaryTest {

    private fun summary(budget: Double, consumed: Double) = EnvelopeSummary(
        id = 1L, name = "Test", budget = budget, endDate = 0L,
        consumed = consumed, dailyRemaining = 0.0,
    )

    @Test
    fun `progress vaut 0 quand rien n'est consommé`() {
        assertEquals(0.0f, summary(100.0, 0.0).progress)
    }

    @Test
    fun `progress vaut 0_5 quand la moitié est consommée`() {
        assertEquals(0.5f, summary(100.0, 50.0).progress)
    }

    @Test
    fun `progress vaut 1 quand le budget est épuisé`() {
        assertEquals(1.0f, summary(100.0, 100.0).progress)
    }

    @Test
    fun `progress est plafonné à 1 en cas de dépassement`() {
        assertEquals(1.0f, summary(100.0, 150.0).progress)
    }

    @Test
    fun `progress vaut 0 quand le budget est zéro`() {
        assertEquals(0.0f, summary(0.0, 0.0).progress)
    }
}
