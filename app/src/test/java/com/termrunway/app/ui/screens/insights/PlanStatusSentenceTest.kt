package com.termrunway.app.ui.screens.insights

import com.termrunway.app.data.PlanMetrics
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanStatusSentenceTest {

    private fun createMetrics(
        expectedSpend: Long = 100_000L,
        actualSpend: Long = 100_000L,
        status: String = "On track",
        actualRemaining: Long = 500_000L
    ): PlanMetrics {
        return PlanMetrics(
            totalExpectedIncomePaise = 0L,
            totalPlannedExpensePaise = 200_000L,
            actualIncomePaise = 0L,
            actualExpensePaise = actualSpend,
            expectedRemainingPaise = 300_000L,
            actualRemainingPaise = actualRemaining,
            daysTotal = 10,
            daysElapsed = 5,
            daysRemaining = 5,
            availablePerDayPaise = 100_000L,
            actualAverageDailySpendPaise = actualSpend / 5,
            expectedSpendToDatePaise = expectedSpend,
            spendVariancePaise = actualSpend - expectedSpend,
            status = status,
            guidance = ""
        )
    }

    @Test
    fun testOnTrackSentence() {
        val m = createMetrics(expectedSpend = 100_000L, actualSpend = 100_000L)
        assertEquals("Your spending is currently on track with your plan.", planStatusSentence(m))
    }

    @Test
    fun testSlightlyAboveSentence() {
        val m = createMetrics(expectedSpend = 100_000L, actualSpend = 110_000L)
        assertEquals("Your spending is slightly above the pace expected by your plan.", planStatusSentence(m))
    }

    @Test
    fun testNoticeablyAboveSentence() {
        val m = createMetrics(expectedSpend = 100_000L, actualSpend = 150_000L)
        assertEquals("Your spending is noticeably above the pace expected by your plan.", planStatusSentence(m))
    }

    @Test
    fun testMuchAboveSentence() {
        val m = createMetrics(expectedSpend = 100_000L, actualSpend = 250_000L)
        assertEquals("Your spending is much faster than the pace expected by your plan.", planStatusSentence(m))
    }

    @Test
    fun testFarAboveSentence() {
        val m = createMetrics(expectedSpend = 100_000L, actualSpend = 1000_000L)
        assertEquals("Your spending is far above the pace expected by your plan.", planStatusSentence(m))
    }

    @Test
    fun testTinyExpectedValueAboveDoesNotReturnOnTrack() {
        val m = createMetrics(expectedSpend = 100L, actualSpend = 2_000L) // Expected ₹1, Actual ₹20
        assertEquals("Your spending is above the expected amount so far.", planStatusSentence(m))
    }

    @Test
    fun testZeroExpectedZeroActual() {
        val m = createMetrics(expectedSpend = 0L, actualSpend = 0L)
        assertEquals("No spending has been recorded yet, and none was expected by this point in the plan.", planStatusSentence(m))
    }

    @Test
    fun testZeroExpectedWithActual() {
        val m = createMetrics(expectedSpend = 0L, actualSpend = 50_000L)
        assertEquals("No spending was expected by this point in your plan, but ₹500.00 has been recorded.", planStatusSentence(m))
    }

    @Test
    fun testOverdrawnSentence() {
        val m = createMetrics(status = "Overdrawn", actualRemaining = -50_000L)
        assertEquals("Your current spending is higher than the money available in this plan.", planStatusSentence(m))
    }

    @Test
    fun testUpcomingSentence() {
        val m = createMetrics(status = "Upcoming")
        assertEquals("Your plan hasn't started yet. Your planned income and expenses are ready for the period.", planStatusSentence(m))
    }

    @Test
    fun testCompletedSentence() {
        val m = createMetrics(status = "Completed")
        assertEquals("Your plan has ended. Review how your actual spending compared with your plan.", planStatusSentence(m))
    }
}
