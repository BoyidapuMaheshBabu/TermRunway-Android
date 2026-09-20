package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import org.junit.Assert.assertEquals
import org.junit.Test

class FinancialSummaryTest {
    @Test
    fun calculatesAvailableBalanceFromIncomeAndExpenses() {
        val incomes = listOf(
            Income(amountCents = 50000L, source = "Allowance", dateMillis = 1L),
            Income(amountCents = 25000L, source = "Scholarship", dateMillis = 2L)
        )
        val expenses = listOf(
            Expense(amountCents = 10000L, category = "Food", dateMillis = 3L),
            Expense(amountCents = 15000L, category = "Transport", dateMillis = 4L)
        )

        val result = calculateFinancialSummary(incomes, expenses)

        assertEquals(75000L, result.totalIncomeCents)
        assertEquals(25000L, result.totalExpenseCents)
        assertEquals(50000L, result.availableBalanceCents)
    }

    @Test
    fun balanceCanBeNegativeWhenExpensesExceedIncome() {
        val result = calculateFinancialSummary(
            incomes = listOf(
                Income(amountCents = 10000L, source = "Allowance", dateMillis = 1L)
            ),
            expenses = listOf(
                Expense(amountCents = 15000L, category = "Food", dateMillis = 2L)
            )
        )

        assertEquals(-5000L, result.availableBalanceCents)
    }

    @Test
    fun emptyDataProducesZeroSummary() {
        val result = calculateFinancialSummary(emptyList(), emptyList())

        assertEquals(0L, result.totalIncomeCents)
        assertEquals(0L, result.totalExpenseCents)
        assertEquals(0L, result.availableBalanceCents)
    }
