package com.termrunway.app.domain

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar

data class DailyTotals(
    val spentCents: Long,
    val incomeCents: Long
) {
    val netCents: Long get() = incomeCents - spentCents
}

data class TrackingTotals(
    val totalSpentCents: Long,
    val totalIncomeCents: Long,
    val netCents: Long,
    val averageActiveDaySpentCents: Long,
    val highestSpendingDayMillis: Long?,
    val highestSpendingDayCents: Long,
    val categoryTotals: List<Pair<String, Long>>,
    val activeDays: Int,
    val transactionCount: Int
)

fun dailyTotals(expenses: List<Expense>, incomes: List<Income>, dayStartMillis: Long): DailyTotals {
    val dayEnd = dayStartMillis + DAY_MILLIS
    val spent = expenses.filter { it.dateMillis in dayStartMillis until dayEnd }.sumOf { it.amountCents }
    val income = incomes.filter { it.dateMillis in dayStartMillis until dayEnd }.sumOf { it.amountCents }
    return DailyTotals(spent, income)
}

fun trackingTotals(
    expenses: List<Expense>,
    incomes: List<Income>,
    startMillis: Long,
    endMillisInclusive: Long
): TrackingTotals {
    val endExclusive = endMillisInclusive + DAY_MILLIS
    val periodExpenses = expenses.filter { it.dateMillis in startMillis until endExclusive }
    val periodIncome = incomes.filter { it.dateMillis in startMillis until endExclusive }
    val byDay = periodExpenses
        .groupBy { startOfDay(it.dateMillis) }
        .mapValues { (_, list) -> list.sumOf { it.amountCents } }

    val totalSpent = periodExpenses.sumOf { it.amountCents }
    val totalIncome = periodIncome.sumOf { it.amountCents }
    val activeDays = byDay.count { it.value > 0L }

    return TrackingTotals(
        totalSpentCents = totalSpent,
        totalIncomeCents = totalIncome,
        netCents = totalIncome - totalSpent,
        averageActiveDaySpentCents = if (activeDays == 0) 0L else totalSpent / activeDays,
        highestSpendingDayMillis = byDay.maxByOrNull { it.value }?.key,
        highestSpendingDayCents = byDay.maxOfOrNull { it.value } ?: 0L,
        categoryTotals = periodExpenses
            .groupBy { it.category }
            .mapValues { (_, list) -> list.sumOf { it.amountCents } }
            .toList()
            .sortedByDescending { it.second },
        activeDays = activeDays,
        transactionCount = periodExpenses.size + periodIncome.size
    )
}

fun parseMoneyToCents(input: String): Long? {
    val normalized = input.trim()
    if (normalized.isEmpty()) return null
    return runCatching {
        BigDecimal(normalized)
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()?.takeIf { it > 0L }
}

private fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
