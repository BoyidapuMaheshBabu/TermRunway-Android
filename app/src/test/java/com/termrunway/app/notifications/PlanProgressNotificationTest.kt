package com.termrunway.app.notifications

import com.termrunway.app.domain.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlanProgressNotificationTest {

    private fun getProgressPercent(startMs: Long, endMs: Long, todayMs: Long): Double {
        val totalDays = FinancialCalculator.daysInclusive(startMs, endMs).coerceAtLeast(1)
        val elapsedDays = FinancialCalculator.daysInclusive(startMs, todayMs)
        return (elapsedDays.toDouble() / totalDays.toDouble() * 100.0)
    }

    private fun detectMilestone(percent: Double): Int? {
        return when {
            percent >= 100.0 -> 100
            percent >= 95.0 -> 95
            percent >= 90.0 -> 90
            percent >= 75.0 -> 75
            percent >= 50.0 -> 50
            else -> null
        }
    }

    @Test
    fun milestone50DetectedAtHalfway() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 50 * 86400000L)
        assertEquals(50, detectMilestone(percent))
    }

    @Test
    fun milestone75DetectedAt75Percent() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 75 * 86400000L)
        assertEquals(75, detectMilestone(percent))
    }

    @Test
    fun milestone90DetectedAt90Percent() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 90 * 86400000L)
        assertEquals(90, detectMilestone(percent))
    }

    @Test
    fun milestone95DetectedAt95Percent() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 95 * 86400000L)
        assertEquals(95, detectMilestone(percent))
    }

    @Test
    fun milestone100DetectedAtCompletion() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 100 * 86400000L)
        assertEquals(100, detectMilestone(percent))
    }

    @Test
    fun noMilestoneDetectedBefore50Percent() {
        val percent = getProgressPercent(0L, 99 * 86400000L, 30 * 86400000L)
        assertNull(detectMilestone(percent))
    }
}
