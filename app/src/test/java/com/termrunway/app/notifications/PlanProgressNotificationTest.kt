package com.termrunway.app.notifications

import com.termrunway.app.domain.FinancialCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PlanProgressNotificationTest {

    private fun getProgressPercent(startDate: LocalDate, endDate: LocalDate, today: LocalDate): Double {
        val totalDays = FinancialCalculator.daysInclusive(startDate, endDate).coerceAtLeast(1)
        val elapsedDays = FinancialCalculator.daysInclusive(startDate, today)
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
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(50)
        val percent = getProgressPercent(start, end, today)
        assertEquals(50, detectMilestone(percent))
    }

    @Test
    fun milestone75DetectedAt75Percent() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(75)
        val percent = getProgressPercent(start, end, today)
        assertEquals(75, detectMilestone(percent))
    }

    @Test
    fun milestone90DetectedAt90Percent() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(90)
        val percent = getProgressPercent(start, end, today)
        assertEquals(90, detectMilestone(percent))
    }

    @Test
    fun milestone95DetectedAt95Percent() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(95)
        val percent = getProgressPercent(start, end, today)
        assertEquals(95, detectMilestone(percent))
    }

    @Test
    fun milestone100DetectedAtCompletion() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(100)
        val percent = getProgressPercent(start, end, today)
        assertEquals(100, detectMilestone(percent))
    }

    @Test
    fun noMilestoneDetectedBefore50Percent() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(30)
        val percent = getProgressPercent(start, end, today)
        assertNull(detectMilestone(percent))
    }
}
