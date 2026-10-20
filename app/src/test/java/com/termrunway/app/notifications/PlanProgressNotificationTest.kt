package com.termrunway.app.notifications

import com.termrunway.app.data.FinancialPlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class PlanProgressNotificationTest {

    @Test
    fun inclusiveStartAndEndDatesCalculation() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 10) // 10 days inclusive

        // Day 1 (2026-01-01): 1 / 10 = 10%
        val p1 = PlanProgressCalculator.calculateProgressPercent(start, end, start)
        assertEquals(10.0, p1, 0.01)

        // Day 5 (2026-01-05): 5 / 10 = 50%
        val p5 = PlanProgressCalculator.calculateProgressPercent(start, end, start.plusDays(4))
        assertEquals(50.0, p5, 0.01)

        // Day 10 (2026-01-10): 10 / 10 = 100%
        val p10 = PlanProgressCalculator.calculateProgressPercent(start, end, end)
        assertEquals(100.0, p10, 0.01)
    }

    @Test
    fun oneDayAndShortDurationPlansProgress() {
        val today = LocalDate.of(2026, 10, 20)

        // 1-day plan
        val oneDayPlanPercent = PlanProgressCalculator.calculateProgressPercent(today, today, today)
        assertEquals(100.0, oneDayPlanPercent, 0.01)
        val milestone1Day = PlanProgressCalculator.evaluateMilestoneToNotify(oneDayPlanPercent) { false }
        assertNotNull(milestone1Day)
        assertEquals(100, milestone1Day!!.percentage)

        // 2-day plan
        val twoDayStart = LocalDate.of(2026, 10, 20)
        val twoDayEnd = LocalDate.of(2026, 10, 21)
        val day1Percent = PlanProgressCalculator.calculateProgressPercent(twoDayStart, twoDayEnd, twoDayStart)
        assertEquals(50.0, day1Percent, 0.01)
        assertEquals(50, PlanProgressCalculator.evaluateMilestoneToNotify(day1Percent) { false }?.percentage)

        val day2Percent = PlanProgressCalculator.calculateProgressPercent(twoDayStart, twoDayEnd, twoDayEnd)
        assertEquals(100.0, day2Percent, 0.01)
        assertEquals(100, PlanProgressCalculator.evaluateMilestoneToNotify(day2Percent) { false }?.percentage)
    }

    @Test
    fun everyMilestoneBoundaryDetected() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10) // 100 days inclusive

        val notifiedMap = mutableSetOf<Int>()

        val day50 = start.plusDays(49) // Day 50
        val p50 = PlanProgressCalculator.calculateProgressPercent(start, end, day50)
        val m50 = PlanProgressCalculator.evaluateMilestoneToNotify(p50) { notifiedMap.contains(it) }
        assertNotNull(m50)
        assertEquals(50, m50!!.percentage)
        notifiedMap.add(50)

        val day75 = start.plusDays(74) // Day 75
        val p75 = PlanProgressCalculator.calculateProgressPercent(start, end, day75)
        val m75 = PlanProgressCalculator.evaluateMilestoneToNotify(p75) { notifiedMap.contains(it) }
        assertNotNull(m75)
        assertEquals(75, m75!!.percentage)
        notifiedMap.add(75)

        val day90 = start.plusDays(89) // Day 90
        val p90 = PlanProgressCalculator.calculateProgressPercent(start, end, day90)
        val m90 = PlanProgressCalculator.evaluateMilestoneToNotify(p90) { notifiedMap.contains(it) }
        assertNotNull(m90)
        assertEquals(90, m90!!.percentage)
        notifiedMap.add(90)

        val day95 = start.plusDays(94) // Day 95
        val p95 = PlanProgressCalculator.calculateProgressPercent(start, end, day95)
        val m95 = PlanProgressCalculator.evaluateMilestoneToNotify(p95) { notifiedMap.contains(it) }
        assertNotNull(m95)
        assertEquals(95, m95!!.percentage)
        notifiedMap.add(95)

        val day100 = end // Day 100
        val p100 = PlanProgressCalculator.calculateProgressPercent(start, end, day100)
        val m100 = PlanProgressCalculator.evaluateMilestoneToNotify(p100) { notifiedMap.contains(it) }
        assertNotNull(m100)
        assertEquals(100, m100!!.percentage)
    }

    @Test
    fun delayedExecutionAfterEndDateStillTriggersCompletionMilestone() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 10)
        val delayedToday = LocalDate.of(2026, 1, 15) // 5 days after end date

        val percent = PlanProgressCalculator.calculateProgressPercent(start, end, delayedToday)
        assertEquals(100.0, percent, 0.01)

        val milestone = PlanProgressCalculator.evaluateMilestoneToNotify(percent) { false }
        assertNotNull(milestone)
        assertEquals(100, milestone!!.percentage)
    }

    @Test
    fun oneTimeDeliveryAndDuplicatePrevention() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(55) // Progress > 50%

        val percent = PlanProgressCalculator.calculateProgressPercent(start, end, today)

        // First run: milestone 50 is evaluated
        val firstRunMilestone = PlanProgressCalculator.evaluateMilestoneToNotify(percent) { false }
        assertNotNull(firstRunMilestone)
        assertEquals(50, firstRunMilestone!!.percentage)

        // Second run with 50% already notified: no milestone returned
        val secondRunMilestone = PlanProgressCalculator.evaluateMilestoneToNotify(percent) { percentKey -> percentKey == 50 }
        assertNull(secondRunMilestone)
    }

    @Test
    fun missedHistoricalMilestonesWhenProgressJumps() {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 4, 10)
        val today = start.plusDays(92) // Jumps from <50% to 93%

        val percent = PlanProgressCalculator.calculateProgressPercent(start, end, today)

        // When evaluating unnotified, highest eligible milestone (90) is selected
        val milestone = PlanProgressCalculator.evaluateMilestoneToNotify(percent) { false }
        assertNotNull(milestone)
        assertEquals(90, milestone!!.percentage)
    }

    @Test
    fun planSelectionAndCompletedPlanHandling() {
        val today = LocalDate.of(2026, 10, 20)

        val completedPlan = FinancialPlan(
            id = 1,
            name = "Summer Plan",
            startDate = LocalDate.of(2026, 5, 1),
            endDate = LocalDate.of(2026, 8, 31)
        )
        val activePlan = FinancialPlan(
            id = 2,
            name = "Fall Plan",
            startDate = LocalDate.of(2026, 9, 1),
            endDate = LocalDate.of(2026, 12, 31)
        )

        val plans = listOf(completedPlan, activePlan)

        // Active plan is selected when active plan is not completed
        val selectedActive = PlanProgressCalculator.selectPlanForProgress(
            plans = plans,
            isPlanCompleted = { id -> id == 1L }, // Plan 1 completed, Plan 2 not completed
            today = today
        )
        assertNotNull(selectedActive)
        assertEquals(2L, selectedActive!!.id)

        // If active plan completes (100% milestone notified), no plan selected for progress
        val selectedNone = PlanProgressCalculator.selectPlanForProgress(
            plans = plans,
            isPlanCompleted = { id -> id == 1L || id == 2L }, // Both completed
            today = today
        )
        assertNull(selectedNone)
    }

    @Test
    fun recentlyEndedPlanWithUnnotifiedCompletionIsSelected() {
        val today = LocalDate.of(2026, 10, 20)

        val recentlyEndedPlan = FinancialPlan(
            id = 1,
            name = "Ended Yesterday",
            startDate = LocalDate.of(2026, 10, 1),
            endDate = LocalDate.of(2026, 10, 19)
        )

        val plans = listOf(recentlyEndedPlan)

        // Delayed worker runs on Oct 20. 100% milestone not delivered yet.
        val selected = PlanProgressCalculator.selectPlanForProgress(
            plans = plans,
            isPlanCompleted = { false }, // 100% not notified yet
            today = today
        )
        assertNotNull(selected)
        assertEquals(1L, selected!!.id)
    }
}
