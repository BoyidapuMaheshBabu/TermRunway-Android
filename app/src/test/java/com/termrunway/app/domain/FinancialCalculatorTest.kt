package com.termrunway.app.domain

import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.ui.util.PeriodPreset
import com.termrunway.app.ui.util.parseMoneyOrNull
import com.termrunway.app.ui.util.resolvePeriodRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
        assertTrue(metrics.safeToSpendTodayPaise > 0)
    }

    @Test
    fun expectedIncomeToDateExcludesFutureIncomeAndDoesNotIncreaseCurrentMoney() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "Income Plan",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 200_000 // ₹2,000
        )
        val incomes = listOf(
            PlannedIncome(planId = 1, source = "Future Allowance", amountPaise = 1000_000, expectedDateMs = day(7)) // ₹10,000 on day 7
        )

        val metricsDay2 = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = incomes,
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = day(2)
        )

        // Day 2: Expected income to date is 0L
        assertEquals(1000_000L, metricsDay2.totalExpectedIncomePaise)
        assertEquals(0L, metricsDay2.expectedIncomeToDatePaise)
        assertEquals(200_000L, metricsDay2.actualRemainingPaise) // Remains ₹2,000 (starting money)
        assertEquals(25_000L, metricsDay2.safeToSpendTodayPaise) // ₹2,000 / 8 remaining days = ₹250/day, NOT increased by future ₹10,000 income

        val metricsDay7 = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = incomes,
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = day(7)
        )

        // Day 7: Expected income to date is 1,000,000
        assertEquals(1000_000L, metricsDay7.expectedIncomeToDatePaise)
    }

    @Test
    fun unrelatedActualExpenseDoesNotFalselyReleaseFutureCollegeFee() {
        val start = day(0)
        val end = day(9) // 10 days
        val plan = FinancialPlan(
            name = "College Term Plan",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 1000_000 // ₹10,000 starting money
        )
        val expenses = listOf(
            PlannedExpense(planId = 1, category = "College Fee", amountPaise = 500_000, expectedDateMs = day(8)) // ₹5,000 fee due on day 8
        )
        val transactions = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 500_000, category = "Food", description = "", dateMs = day(1)) // ₹5,000 actual food expense on day 1
        )

        val metricsDay2 = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = expenses,
            transactions = transactions,
            todayMs = day(2)
        )

        // On day 2, actual remaining is ₹5,000. Future college fee is ₹5,000.
        // Discretionary remaining = max(0, 5,000 - 5,000) = ₹0.
        // Safe to spend today = ₹0. The ₹5,000 food expense did NOT falsely release the future college fee commitment!
        assertEquals(500_000L, metricsDay2.actualExpensePaise)
        assertEquals(500_000L, metricsDay2.actualRemainingPaise)
        assertEquals(0L, metricsDay2.safeToSpendTodayPaise)
    }

    @Test
    fun futureActualTransactionDoesNotCountForActivePlanSoFar() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "Active Term",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 100_000
        )
        val transactions = listOf(
            Transaction(type = TransactionType.EXPENSE, amountPaise = 10_000, category = "Food", description = "", dateMs = day(1)),
            Transaction(type = TransactionType.EXPENSE, amountPaise = 50_000, category = "Future Fee", description = "", dateMs = day(8)) // future dated
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = transactions,
            todayMs = day(2) // Today is day 2
        )

        // Only day 1 expense (10,000) counts towards actual expense so far
        assertEquals(10_000L, metrics.actualExpensePaise)
        assertEquals(90_000L, metrics.actualRemainingPaise)
    }

    @Test
    fun datedPlannedExpenseExcludedFromExpectedSpendBeforeExpectedDate() {
        val start = day(0)
        val end = day(9)
        val plan = FinancialPlan(
            name = "College Plan",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 1_000_000 // ₹10,000
        )
        val expenses = listOf(
            PlannedExpense(planId = 1, category = "College Fee", amountPaise = 500_000, expectedDateMs = day(8)) // Fee due on day 8
        )

        val metricsDay2 = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = expenses,
            transactions = emptyList(),
            todayMs = day(2)
        )

        // On Day 2 (before Day 8), expected spend so far excludes the fee
        assertEquals(0L, metricsDay2.expectedSpendToDatePaise)

        val metricsDay8 = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = expenses,
            transactions = emptyList(),
            todayMs = day(8)
        )

        // On Day 8, expected spend so far includes the fee
        assertEquals(500_000L, metricsDay8.expectedSpendToDatePaise)
    }

    @Test
    fun safeSpendingReservesFuturePlannedCommitments() {
        val start = day(0)
        val end = day(9) // 10 days
        val plan = FinancialPlan(
            name = "Fee Plan",
            startMs = start,
            endMs = end,
            startingMoneyPaise = 1_000_000 // ₹10,000
        )
        val expenses = listOf(
            PlannedExpense(planId = 1, category = "Fee", amountPaise = 500_000, expectedDateMs = day(8)) // ₹5,000 fee
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = expenses,
            transactions = emptyList(),
            todayMs = day(0)
        )

        // Actual remaining = ₹10,000. Future commitment = ₹5,000. Discretionary = ₹5,000.
        // Safe to spend per day = ₹5,000 / 10 days = ₹500 (50,000 paise)
        assertEquals(50_000L, metrics.safeToSpendTodayPaise)
    }

    @Test
    fun sameDayPlanHasOneDayRemaining() {
        val today = day(0)
        val plan = FinancialPlan(
            name = "One Day Plan",
            startMs = today,
            endMs = today,
            startingMoneyPaise = 10_000
        )

        val metrics = FinancialCalculator.planMetrics(
            plan = plan,
            plannedIncome = emptyList(),
            plannedExpenses = emptyList(),
            transactions = emptyList(),
            todayMs = today
        )

        assertEquals(1, metrics.daysTotal)
        assertEquals(1, metrics.daysRemaining)
    }

    @Test
    fun moneyParsingValidation() {
        assertNull(parseMoneyOrNull("abc"))
        assertNull(parseMoneyOrNull(""))
        assertNull(parseMoneyOrNull("   "))
        assertNull(parseMoneyOrNull("-500"))
        assertEquals(100_000L, parseMoneyOrNull("1000"))
        assertEquals(1_000_000_00L, parseMoneyOrNull("1000000")) // ₹10,00,000
    }

    @Test
    fun customReversedDatesNormalizesChronologically() {
        val range = resolvePeriodRange(
            preset = PeriodPreset.CUSTOM,
            activePlan = null,
            customStartMs = day(10), // reversed
            customEndMs = day(2),
            todayMs = day(5)
        )

        assertTrue(range.startMs!! <= range.endMs!!)
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
        assertTrue(metrics.guidance.contains("Your plan starts in"))
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
