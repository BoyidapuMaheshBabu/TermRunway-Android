package com.termrunway.app.domain

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FinanceEngineTest {
    @Test
    fun parses_valid_money_to_cents() {
        assertEquals(125050L, parseMoneyToCents("1250.50"))
        assertEquals(9900L, parseMoneyToCents("99"))
        assertNull(parseMoneyToCents("0"))
        assertNull(parseMoneyToCents("12.345"))
    }

    @Test
    fun daily_totals_keep_income_and_spending_separate() {
        val day = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis

        val expenses = listOf(
            Expense(amountCents = 1200L, category = "Food & Groceries", dateMillis = day + 60000L),
            Expense(amountCents = 800L, category = "Transport & Travel", dateMillis = day + 120000L)
        )
        val incomes = listOf(
            Income(amountCents = 5000L, source = "Pocket money", dateMillis = day + 180000L)
        )

        val totals = dailyTotals(expenses, incomes, day)
        assertEquals(2000L, totals.spentCents)
        assertEquals(5000L, totals.incomeCents)
        assertEquals(3000L, totals.netCents)
    }
}
