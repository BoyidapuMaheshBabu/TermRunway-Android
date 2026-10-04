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

        assertEquals(50_000, FinancialCalculator.balance(transactions))
        assertEquals(35_000, FinancialCalculator.dayExpense(transactions, day(0)))
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

        assertEquals(50_000, metrics.totalExpectedIncomePaise)
        assertEquals(80_000, metrics.totalPlannedExpensePaise)
        assertEquals(105_000, metrics.actualRemainingPaise)
        assertEquals(6, metrics.daysRemaining)
        assertTrue(metrics.availablePerDayPaise > 0)
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
        assertEquals(30_000, totals["Food"])
        assertEquals(25_000, totals["Transport"])
    }
}
