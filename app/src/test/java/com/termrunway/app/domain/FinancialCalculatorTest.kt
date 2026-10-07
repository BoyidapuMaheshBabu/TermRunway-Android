package com.termrunway.app.domain

import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class FinancialCalculatorTest {
    private fun day(offset: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, offset)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun balanceUsesIncomeMinusExpense() {
        val transactions = listOf(
            Transaction(type = TransactionType.INCOME, amountPaise = 100_000, category = "Parents", description = "", dateMs = day(0)),
            Transaction(type = TransactionType.EXPENSE, amountPaise = 35_000, category = "Food", description = "", dateMs = day(0)),
            Transaction(type = TransactionType.EXPENSE, amountPaise = 15_000, category = "Transport", description = "", dateMs = day(-1))
        )

        assertEquals(50_000L, FinancialCalculator.balance(transactions))
        assertEquals(35_000L, FinancialCalculator.dayExpense(transactions, day(0)))
    }

    @Test
    fun planMetricsCalculateRemainingMoneyAndDailyGuidance() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "Test Semester",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 100_000
        )
        val incomes = listOf(
            PlannedIncome(planId = 1, source = "Parents", amountPaise = 50_000, expectedDateMs = start)
        )
        val expenses = listOf(
            PlannedExpense(planId = 1, category = "Food", amountPaise = 60_000, expectedDateMs = start),
            PlannedExpense(planId = 1, category = "Transport", amountPaise = 20_000, expectedDateMs = start)
        )
        val transactions = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 20_000, category = "Food", description = "", dateMs = day(2)),
            Transaction(type = TransactionType.INCOME, amountPaise = 25_000, category = "Parents", description = "", dateMs = day(3))
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = incomes,
            plannedExpenses = expenses,
            transactions = transactions,
            todayMs = day(4)
        )

        assertEquals(50_000L, metrics.totalExpectedIncomePaise)
        assertEquals(80_000L, metrics.totalPlannedExpensePaise)
        assertEquals(105_000L, metrics.actualRemainingPaise)
        assertEquals(6, metrics.daysRemaining)
        assertEquals(17_500L, metrics.safeToSpendTodayPaise)
        assertTrue(metrics.safeToSpendTodayPaise > 0)
    }

    @Test
    fun planMetricsHandlesOverdrawnGracefully() {
        val start = day(-2)
        val end = day(5)
        val plan = FinancialPlan(
            name = "Tight Month",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 10_000 // ₹100
        )
        val transactions = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 30_000, category = "Food", description = "", dateMs = day(0)) // ₹300
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = transactions,
            todayMs = day(0)
        )

        assertEquals("Overdrawn", metrics.status)
        assertEquals(-20_000L, metrics.actualRemainingPaise)
        assertEquals(0L, metrics.safeToSpendTodayPaise)
        assertTrue(metrics.guidance.contains("above available money"))
    }

    @Test
    fun planMetricsHandlesUpcomingPlan() {
        val start = day(5)
        val end = day(15)
        val plan = FinancialPlan(
            name = "Next Semester",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 500_000
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = day(0)
        )

        assertEquals("Upcoming", metrics.status)
        assertTrue(metrics.guidance.contains("Your plan starts on"))
    }

    @Test
    fun planMetricsHandlesCompletedPlan() {
        val start = day(-20)
        val end = day(-5)
        val plan = FinancialPlan(
            name = "Past Term",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 200_000
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = day(0)
        )

        assertEquals("Completed", metrics.status)
        assertEquals(0, metrics.daysRemaining)
        assertEquals(0L, metrics.safeToSpendTodayPaise)
        assertTrue(metrics.guidance.contains("This plan has ended"))
    }

    @Test
    fun planMetricsRecalculatesWhenTransactionsChange() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "Flexible Budget",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 100_000 // ₹1000
        )

        val initialMetrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = day(0)
        )

        assertEquals(10, initialMetrics.daysRemaining)
        assertEquals(10_000L, initialMetrics.safeToSpendTodayPaise) // ₹100 / day

        // User spends ₹500 on day 1 (more than ₹100 daily pace)
        val newTx = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 50_000, category = "Food", description = "", dateMs = day(0))
        )

        val updatedMetrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = newTx,
            todayMs = day(1) // 9 days remaining
        )

        assertEquals(50_000L, updatedMetrics.actualRemainingPaise) // ₹500 remaining
        assertEquals(9, updatedMetrics.daysRemaining)
        assertEquals(5_555L, updatedMetrics.safeToSpendTodayPaise) // ₹55.55 / day dynamically adjusted
    }

    @Test
    fun planMetricsSupportsLargeOneTimeExpense() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "College Term",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 1_000_000 // ₹10,000
        )
        val expenses = listOf(
            PlannedExpense(planId = 1, category = "Education", amountPaise = 500_000, expectedDateMs = start) // ₹5,000 college fee
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = expenses,
            transactions = emptyList(),
            todayMs = day(0)
        )

        assertEquals(500_000L, metrics.totalPlannedExpensePaise)
        assertEquals(500_000L, metrics.expectedRemainingPaise) // ₹5,000 expected remaining after college fee
    }

    @Test
    fun categoryTotalsAreSortedByHighestSpend() {
        val transactions = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 10_000, category = "Food", description = "", dateMs = day(0)),
            Transaction(type = TransactionType.EXPENSE, amountPaise = 25_000, category = "Transport", description = "", dateMs = day(0)),
            Transaction(type = TransactionType.EXPENSE, amountPaise = 20_000, category = "Food", description = "", dateMs = day(-1))
        )

        val totals = FinancialCalculator.categoryTotals(transactions, TransactionType.EXPENSE)

        assertEquals(listOf("Food", "Transport"), totals.keys.toList())
        assertEquals(30_000L, totals["Food"])
        assertEquals(25_000L, totals["Transport"])
    }
}
