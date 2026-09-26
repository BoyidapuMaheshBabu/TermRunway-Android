package com.termrunway.app.domain

import com.termrunway.app.data.AppData
import com.termrunway.app.data.TermPlan
import java.util.Calendar
import kotlin.math.max

data class TermSummary(
    val totalDays: Int,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val currentBalanceCents: Long,
    val actualIncomeCents: Long,
    val actualExpenseCents: Long,
    val expectedIncomeCents: Long,
    val plannedExpenseCents: Long,
    val remainingExpectedIncomeCents: Long,
    val remainingPlannedExpenseCents: Long,
    val projectedEndBalanceCents: Long,
    val plannedDailyPaceCents: Long,
    val actualDailyPaceCents: Long,
    val safeDailyPaceCents: Long,
    val plannedToDateCents: Long,
    val varianceToPlanCents: Long,
    val progressRatio: Float,
    val status: TermStatus
)

enum class TermStatus {
    NO_PLAN,
    HEALTHY,
    ABOVE_PLAN,
    INSUFFICIENT
}

data class CategoryComparison(
    val category: String,
    val plannedCents: Long,
    val actualCents: Long
) {
    val varianceCents: Long get() = actualCents - plannedCents
}

fun termSummary(data: AppData, plan: TermPlan, nowMillis: Long = System.currentTimeMillis()): TermSummary {
    val start = startOfDay(plan.startDateMillis)
    val end = startOfDay(plan.endDateMillis)
    val now = startOfDay(nowMillis)

    val totalDays = daysBetweenInclusive(start, end).coerceAtLeast(1)
    val daysElapsed = if (now <= start) 0 else {
        daysBetweenInclusive(start, now.coerceAtMost(end))
    }.coerceIn(0, totalDays)
    val daysRemaining = (totalDays - daysElapsed).coerceAtLeast(0)

    val actualExpenses = data.expenses
        .filter { it.dateMillis in start..endOfDay(end) }
        .sumOf { it.amountCents }
    val actualIncome = data.incomes
        .filter { it.dateMillis in start..endOfDay(end) }
        .sumOf { it.amountCents }

    val expectedIncome = plan.expectedIncome.sumOf { it.amountCents }
    val plannedExpenses = plan.plannedExpenses.sumOf { it.amountCents }

    val currentBalance = plan.availableMoneyCents + actualIncome - actualExpenses
    val remainingExpectedIncome = max(expectedIncome - actualIncome, 0L)
    val remainingPlannedExpense = max(plannedExpenses - actualExpenses, 0L)
    val projectedEnd = currentBalance + remainingExpectedIncome - remainingPlannedExpense

    val plannedDaily = if (totalDays == 0) 0L else plannedExpenses / totalDays
    val actualDaily = if (daysElapsed == 0) 0L else actualExpenses / daysElapsed
    val safeDaily = if (daysRemaining == 0) 0L else {
        max(currentBalance + remainingExpectedIncome, 0L) / daysRemaining
    }
    val plannedToDate = (plannedExpenses.toDouble() * daysElapsed / totalDays).toLong()
    val variance = actualExpenses - plannedToDate
    val progress = daysElapsed.toFloat() / totalDays.toFloat()

    val status = when {
        projectedEnd < 0L -> TermStatus.INSUFFICIENT
        plannedToDate > 0L && actualExpenses > (plannedToDate * 1.15).toLong() -> TermStatus.ABOVE_PLAN
        else -> TermStatus.HEALTHY
    }

    return TermSummary(
        totalDays = totalDays,
        daysElapsed = daysElapsed,
        daysRemaining = daysRemaining,
        currentBalanceCents = currentBalance,
        actualIncomeCents = actualIncome,
        actualExpenseCents = actualExpenses,
        expectedIncomeCents = expectedIncome,
        plannedExpenseCents = plannedExpenses,
        remainingExpectedIncomeCents = remainingExpectedIncome,
        remainingPlannedExpenseCents = remainingPlannedExpense,
        projectedEndBalanceCents = projectedEnd,
        plannedDailyPaceCents = plannedDaily,
        actualDailyPaceCents = actualDaily,
        safeDailyPaceCents = safeDaily,
        plannedToDateCents = plannedToDate,
        varianceToPlanCents = variance,
        progressRatio = progress.coerceIn(0f, 1f),
        status = status
    )
}

fun categoryComparisons(data: AppData, plan: TermPlan, nowMillis: Long = System.currentTimeMillis()): List<CategoryComparison> {
    val start = startOfDay(plan.startDateMillis)
    val end = startOfDay(plan.endDateMillis)
    val actuals = data.expenses
        .filter { it.dateMillis in start..endOfDay(end) }
        .groupBy { it.category }
        .mapValues { (_, list) -> list.sumOf { it.amountCents } }

    return plan.plannedExpenses.map {
        CategoryComparison(
            category = it.category,
            plannedCents = it.amountCents,
            actualCents = actuals[it.category] ?: 0L
        )
    }
}

private fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun endOfDay(millis: Long): Long = startOfDay(millis) + DAY_MILLIS - 1L

private fun daysBetweenInclusive(startMillis: Long, endMillis: Long): Int {
    if (endMillis < startMillis) return 0
    return ((startOfDay(endMillis) - startOfDay(startMillis)) / DAY_MILLIS).toInt() + 1
}

private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
