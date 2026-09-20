package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income

data class FinancialSummary(
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val availableBalanceCents: Long
)

fun calculateFinancialSummary(
    incomes: List<Income>,
    expenses: List<Expense>
): FinancialSummary {
    val totalIncome = incomes.sumOf { it.amountCents }
    val totalExpenses = expenses.sumOf { it.amountCents }

    return FinancialSummary(
        totalIncomeCents = totalIncome,
        totalExpenseCents = totalExpenses,
        availableBalanceCents = totalIncome - totalExpenses
    )
}
