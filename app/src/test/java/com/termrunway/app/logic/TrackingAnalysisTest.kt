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
        assertEquals(200_00L, result.averageDailyExpenseCents)
    }
}
