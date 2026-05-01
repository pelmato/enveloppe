package net.zygalio.enveloppe.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class EnvelopeDetailTest {

    private fun detail(budget: Double, consumed: Double) = EnvelopeDetail(
        id = 1L, name = "Test", budget = budget, endDate = 0L,
        categories = emptyList(), consumed = consumed,
        dailyBudget = 0.0, dailyRemaining = 0.0,
    )

    @Test
    fun `remaining = budget - consumed`() {
        assertEquals(70.0, detail(100.0, 30.0).remaining, 0.001)
        assertEquals(0.0, detail(100.0, 100.0).remaining, 0.001)
    }

    @Test
    fun `remaining peut être négatif en cas de dépassement`() {
        assertEquals(-20.0, detail(100.0, 120.0).remaining, 0.001)
    }

    @Test
    fun `progress est calculé sur le budget consommé`() {
        assertEquals(0.3f, detail(100.0, 30.0).progress, 0.001f)
    }

    @Test
    fun `progress est plafonné à 1 en cas de dépassement`() {
        assertEquals(1.0f, detail(100.0, 120.0).progress, 0.001f)
    }

    @Test
    fun `progress vaut 0 quand le budget est zéro`() {
        assertEquals(0.0f, detail(0.0, 0.0).progress, 0.001f)
    }
}
