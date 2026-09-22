package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import java.math.BigDecimal

data class TrackingSummary(
    val range: DateRange,
    val totalIncomeCents: Long,
    val totalExpenseCents: Long,
    val netChangeCents: Long,
    val averageDailyExpenseCents: Long,
    val categoryTotalsCents: Map<String, Long>,
    val categoryPercentages: Map<String, Int>,
    val transactionCount: Int,
    val expenseTransactionCount: Int,
    val incomeTransactionCount: Int
)

fun calculateTrackingSummary(
    incomes: List<Income>,
    expenses: List<Expense>,
    range: DateRange
): TrackingSummary {
    val periodIncomes = incomes.filter { it.dateMillis.isWithin(range) }
    val periodExpenses = expenses.filter { it.dateMillis.isWithin(range) }

    val income = periodIncomes.sumOf { it.amountCents }
    val expense = periodExpenses.sumOf { it.amountCents }
    val days = daysBetweenInclusive(range.startMillis, range.endMillis).coerceAtLeast(1)

    val categoryTotals = periodExpenses
        .groupBy { it.category }
        .mapValues { (_, values) -> values.sumOf { it.amountCents } }
        .toList()
        .sortedByDescending { it.second }
        .toMap()

    val categoryPercentages = categoryTotals.mapValues { (_, total) ->
        if (expense == 0L) 0 else ((total.toDouble() / expense.toDouble()) * 100).toInt()
    }

    return TrackingSummary(
        range = range,
        totalIncomeCents = income,
        totalExpenseCents = expense,
        netChangeCents = income - expense,
        averageDailyExpenseCents = expense / days,
        categoryTotalsCents = categoryTotals,
        categoryPercentages = categoryPercentages,
        transactionCount = periodIncomes.size + periodExpenses.size,
        expenseTransactionCount = periodExpenses.size,
        incomeTransactionCount = periodIncomes.size
    )
}

fun trackingInsights(summary: TrackingSummary): List<String> {
    val insights = mutableListOf<String>()

    val topCategory = summary.categoryTotalsCents.maxByOrNull { it.value }
    if (topCategory != null) {
        insights.add("${topCategory.key} was your largest expense category during this period.")
    }

    if (summary.totalExpenseCents > 0L || summary.totalIncomeCents > 0L) {
        insights.add("Your recorded income was ₹${formatRupees(summary.totalIncomeCents)} and recorded expenses were ₹${formatRupees(summary.totalExpenseCents)}.")
    }

    if (summary.transactionCount > 0) {
        insights.add("You recorded ${summary.transactionCount} transactions during this period.")
    }

    if (summary.totalExpenseCents > 0L) {
        insights.add("Your average daily expense was ₹${formatRupees(summary.averageDailyExpenseCents)}.")
    }

    return insights
}

fun formatRupees(cents: Long): String =
    BigDecimal.valueOf(cents, 2).toPlainString()
