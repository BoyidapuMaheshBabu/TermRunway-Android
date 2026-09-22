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
    val transactionCount: Int
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

    return TrackingSummary(
        range = range,
        totalIncomeCents = income,
        totalExpenseCents = expense,
        netChangeCents = income - expense,
        averageDailyExpenseCents = expense / days,
        categoryTotalsCents = periodExpenses
            .groupBy { it.category }
            .mapValues { (_, values) -> values.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second }
            .toMap(),
        transactionCount = periodIncomes.size + periodExpenses.size
    )
}

fun trackingInsight(summary: TrackingSummary, previous: TrackingSummary?): String? {
    val topCategory = summary.categoryTotalsCents.maxByOrNull { it.value } ?: return null
    val current = "Your largest expense category is " +
        topCategory.key +
        " at ₹" +
        formatRupees(topCategory.value) +
        " for this period."

    if (previous != null && previous.totalExpenseCents > 0L) {
        val difference = summary.totalExpenseCents - previous.totalExpenseCents
        val comparison = when {
            difference > 0L ->
                "That is ₹" + formatRupees(difference) + " more spending than the previous comparison period."
            difference < 0L ->
                "That is ₹" + formatRupees(-difference) + " less spending than the previous comparison period."
            else ->
                "Your total spending is about the same as the previous comparison period."
        }
        return current + " " + comparison
    }

    return current
}

fun formatRupees(cents: Long): String =
    BigDecimal.valueOf(cents, 2).toPlainString()
