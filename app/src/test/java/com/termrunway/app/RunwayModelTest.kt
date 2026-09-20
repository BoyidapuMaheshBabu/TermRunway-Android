package com.termrunway.app

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import com.termrunway.app.logic.RunwayStatus
import com.termrunway.app.logic.calculateRunway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Calendar

class RunwayModelTest {

    private fun date(daysOffset: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, daysOffset)
        }.timeInMillis
    }

    @Test
    fun testPositiveProjectedBalance() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(10),
            startingFundsCents = 10000_00,
            expectedIncomeCents = 5000_00,
            planningMode = "Semester",
            plannedExpenses = mapOf("Food" to 6000_00, "Transport" to 4000_00)
        )

        // 2000 spent on Food, 1000 spent on Transport
        val expenses = listOf(
            Expense(amountCents = 2000_00, category = "Food", dateMillis = date(-5)),
            Expense(amountCents = 1000_00, category = "Transport", dateMillis = date(-4))
        )
        val incomes = emptyList<Income>()

        val result = calculateRunway(plan, expenses, incomes, currentDateMillis = date(0))
        assertNotNull(result)

        // Available Now = 10000 - 2000 - 1000 = 7000
        assertEquals(7000_00L, result!!.availableNowCents)

        // Total Planned = 10000
        assertEquals(10000_00L, result.totalPlannedExpensesCents)

        // Remaining Planned = (6000 - 2000) + (4000 - 1000) = 4000 + 3000 = 7000
        assertEquals(7000_00L, result.remainingPlannedExpensesCents)

        // Projected Balance = Available Now (7000) + Expected Future (5000) - Remaining Planned (7000) = 5000
        assertEquals(5000_00L, result.projectedBalanceCents)

        assertEquals(10, result.daysRemaining)
        assertEquals(5000_00L / 10, result.dailySafeSpendingCents)
        assertEquals(RunwayStatus.BUFFER, result.status)
    }

    @Test
    fun testNegativeProjectedBalance() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(10),
            startingFundsCents = 5000_00,
            expectedIncomeCents = 1000_00,
            planningMode = "Semester",
            plannedExpenses = mapOf("Food" to 8000_00)
        )

        val result = calculateRunway(plan, emptyList(), emptyList(), currentDateMillis = date(0))
        assertNotNull(result)

        // Available Now = 5000
        // Expected Future = 1000
        // Remaining Planned = 8000
        // Projected = 5000 + 1000 - 8000 = -2000
        assertEquals(-2000_00L, result!!.projectedBalanceCents)
        assertEquals(RunwayStatus.SHORTFALL, result.status)
        assertEquals(0L, result.dailySafeSpendingCents) // Safe spending should not be negative
    }

    @Test
    fun testZeroIncomeAndExpenses() {
        val plan = TermPlan(
            startDateMillis = date(0),
            endDateMillis = date(10),
            startingFundsCents = 1000_00,
            expectedIncomeCents = 0L,
            planningMode = "Semester",
            plannedExpenses = emptyMap()
        )

        val result = calculateRunway(plan, emptyList(), emptyList(), currentDateMillis = date(0))
        assertNotNull(result)
        
        assertEquals(1000_00L, result!!.projectedBalanceCents)
        assertEquals(10, result.daysRemaining)
        assertEquals(100_00L, result.dailySafeSpendingCents)
        assertEquals(RunwayStatus.BUFFER, result.status)
    }

    @Test
    fun testDaysRemainingZero() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(0),
            startingFundsCents = 1000_00,
            expectedIncomeCents = 0L,
            planningMode = "Semester",
            plannedExpenses = emptyMap()
        )

        val result = calculateRunway(plan, emptyList(), emptyList(), currentDateMillis = date(0))
        assertNotNull(result)
        
        assertEquals(0, result!!.daysRemaining)
        assertEquals(0L, result.dailySafeSpendingCents) // Handle 0 days remaining safely
    }

    @Test
    fun testPurchaseEffect() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(10),
            startingFundsCents = 10000_00,
            expectedIncomeCents = 0L,
            planningMode = "Semester",
            plannedExpenses = emptyMap()
        )
        val result = calculateRunway(plan, emptyList(), emptyList(), currentDateMillis = date(0))
        assertNotNull(result)

        val purchaseAmount = 2000_00L
        val projectedAfter = result!!.projectedBalanceCents - purchaseAmount
        
        assertEquals(8000_00L, projectedAfter)

        val dailySafeAfter = if (result.daysRemaining > 0) {
            projectedAfter.coerceAtLeast(0L) / result.daysRemaining
        } else {
            0L
        }
        assertEquals(800_00L, dailySafeAfter)
    }

    @Test
    fun testIncomeMinusExpenseBalance() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(10),
            startingFundsCents = 0L,
            expectedIncomeCents = 0L,
            planningMode = "Semester",
            plannedExpenses = emptyMap()
        )
        
        val incomes = listOf(Income(amountCents = 5000_00, source = "Job", dateMillis = date(-5)))
        val expenses = listOf(Expense(amountCents = 2000_00, category = "Food", dateMillis = date(-4)))
        
        val result = calculateRunway(plan, expenses, incomes, currentDateMillis = date(0))
        assertNotNull(result)
        
        // 5000 income - 2000 expense = 3000 available
        assertEquals(3000_00L, result!!.availableNowCents)
        assertEquals(3000_00L, result.projectedBalanceCents)
    }

    @Test
    fun testPlannedVsActualTotals() {
        val plan = TermPlan(
            startDateMillis = date(-10),
            endDateMillis = date(10),
            startingFundsCents = 10000_00,
            expectedIncomeCents = 0L,
            planningMode = "Semester",
            plannedExpenses = mapOf("Food" to 5000_00)
        )
        
        // Spent 6000 on food (over budget)
        val expenses = listOf(Expense(amountCents = 6000_00, category = "Food", dateMillis = date(-5)))
        
        val result = calculateRunway(plan, expenses, emptyList(), currentDateMillis = date(0))
        assertNotNull(result)
        
        // Planned was 5000, Actual was 6000. Remaining for category is 0 (can't be negative).
        assertEquals(5000_00L, result!!.totalPlannedExpensesCents)
        assertEquals(0L, result.remainingPlannedExpensesCents)
        
        // Projected = Available (10000 - 6000 = 4000) + Expected (0) - Remaining Planned (0) = 4000
        assertEquals(4000_00L, result.projectedBalanceCents)
    }
}
