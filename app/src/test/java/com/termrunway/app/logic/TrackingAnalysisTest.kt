package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingAnalysisTest {

    @Test
    fun summaryCalculatesOnlySelectedPeriod() {
        val day = startOfDay(3_000_000_000L)
        val range = DateRange(day, endOfDay(day + 6L * DAY_MILLIS))

        val expenses = listOf(
            Expense( amountCents = 100_00L, category = "Food", dateMillis = day + DAY_MILLIS ),
            Expense( amountCents = 300_00L, category = "Transport", dateMillis = day + 3L * DAY_MILLIS ),
            Expense( amountCents = 9_000_00L, category = "Food", dateMillis = day + 20L * DAY_MILLIS )
        )
        val incomes = listOf(
            Income(amountCents = 1_000_00L, source = "Parents", dateMillis = day + 2L * DAY_MILLIS),
            Income(amountCents = 8_000_00L, source = "Later", dateMillis = day + 20L * DAY_MILLIS)
        )

        val result = calculateTrackingSummary(incomes, expenses, range)

        assertEquals(1_000_00L, result.totalIncomeCents)
        assertEquals(400_00L, result.totalExpenseCents)
        assertEquals(600_00L, result.netChangeCents)
        assertEquals(400_00L / 7L, result.averageDailyExpenseCents)
        assertEquals(3, result.transactionCount)
        assertEquals(2, result.expenseTransactionCount)
        assertEquals(1, result.incomeTransactionCount)
        
        val percentages = result.categoryPercentages
        assertEquals(25, percentages["Food"]) // 100 out of 400
        assertEquals(75, percentages["Transport"]) // 300 out of 400
    }

    @Test
    fun summaryHandlesZeroTransactions() {
        val range = DateRange(0, 7L * DAY_MILLIS)
        val result = calculateTrackingSummary(emptyList(), emptyList(), range)

        assertEquals(0L, result.totalIncomeCents)
        assertEquals(0L, result.totalExpenseCents)
        assertEquals(0L, result.netChangeCents)
        assertEquals(0L, result.averageDailyExpenseCents)
        assertEquals(0, result.transactionCount)
        assertEquals(0, result.expenseTransactionCount)
        assertEquals(0, result.incomeTransactionCount)
    }

    @Test
    fun providedExampleIsCorrect() {
        val day = startOfDay(0L)
        val range = DateRange(day, endOfDay(day + 6L * DAY_MILLIS))

        val expenses = listOf(
            Expense( amountCents = 500_00L, category = "Food", dateMillis = day ),
            Expense( amountCents = 200_00L, category = "Transport", dateMillis = day ),
            Expense( amountCents = 300_00L, category = "Food", dateMillis = day ),
            Expense( amountCents = 400_00L, category = "Entertainment", dateMillis = day )
        )
        val incomes = listOf(
            Income(amountCents = 5000_00L, source = "Parents", dateMillis = day)
        )

        val result = calculateTrackingSummary(incomes, expenses, range)
        assertEquals(5000_00L, result.totalIncomeCents)
        assertEquals(1400_00L, result.totalExpenseCents)
        assertEquals(3600_00L, result.netChangeCents)
    }

    @Test
    fun calendarMonthCalculations() {
        // trackingRange(TrackingPeriod.ONE_MONTH) uses Calendar.MONTH, -1
        val todayMillis = 1714521600000L // Wed May 01 2024
        val range = trackingRange(TrackingPeriod.ONE_MONTH, todayMillis)
        
        // One month prior to May 1 2024 is April 1 2024
        val april1st2024 = 1711929600000L
        assertEquals(startOfDay(april1st2024), range.startMillis)
        assertEquals(endOfDay(todayMillis), range.endMillis)
    }
}
