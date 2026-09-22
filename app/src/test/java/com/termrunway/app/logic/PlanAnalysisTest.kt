package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import org.junit.Assert.assertEquals
import org.junit.Test

class PlanAnalysisTest {

    @Test
    fun selectedPeriodUsesOnlyTransactionsInsideRange() {
        val start = startOfDay(1_000_000_000L)
        val end = endOfDay(start + 89L * DAY_MILLIS)

        val plan = TermPlan(
            startDateMillis = start,
            endDateMillis = end,
            startingFundsCents = 20_000_00L,
            expectedIncomeCents = 10_000_00L,
            planningMode = "Selected period",
            plannedExpenses = mapOf("Food" to 18_000_00L)
        )

        val selected = DateRange(start, endOfDay(start + 29L * DAY_MILLIS))

        val inside = Expense(
            amountCents = 500_00L,
            category = "Food",
            dateMillis = start + 5L * DAY_MILLIS
        )
        val outside = Expense(
            amountCents = 9_000_00L,
            category = "Food",
            dateMillis = start + 60L * DAY_MILLIS
        )

        val result = calculatePlanAnalysis(
            plan = plan,
            expenses = listOf(inside, outside),
            incomes = emptyList(),
            requestedRange = selected
        )

        assertEquals(500_00L, result.actualSpendingForPeriodCents)
        assertEquals(6_000_00L, result.plannedSpendingForPeriodCents)
    }

    @Test
    fun actualIncomeChangesActualPositionButDoesNotChangePlanValues() {
        val start = startOfDay(2_000_000_000L)
        val end = endOfDay(start + 89L * DAY_MILLIS)

        val plan = TermPlan(
            startDateMillis = start,
            endDateMillis = end,
            startingFundsCents = 20_000_00L,
            expectedIncomeCents = 10_000_00L,
            planningMode = "Selected period",
            plannedExpenses = mapOf("Food" to 18_000_00L)
        )

        val income = Income(
            amountCents = 5_000_00L,
            source = "Parents",
            dateMillis = start + 10L * DAY_MILLIS
        )

        val result = calculatePlanAnalysis(
            plan = plan,
            expenses = emptyList(),
            incomes = listOf(income),
            requestedRange = DateRange(start, end)
        )

        assertEquals(20_000_00L, plan.startingFundsCents)
        assertEquals(10_000_00L, plan.expectedIncomeCents)
        assertEquals(5_000_00L, result.actualIncomeForPeriodCents)
        assertEquals(
            25_000_00L,
            result.actualPositionCents
        )
    }
}
