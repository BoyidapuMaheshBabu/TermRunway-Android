package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import java.math.BigDecimal
import java.math.RoundingMode

data class PlanPeriodAnalysis(
    val range: DateRange,
    val planDays: Int,
    val selectedDays: Int,
    val healthyDailySpendingCents: Long,
    val plannedSpendingForPeriodCents: Long,
    val actualSpendingForPeriodCents: Long,
    val spendingVarianceCents: Long,
    val actualIncomeForPeriodCents: Long,
    val plannedPositionCents: Long,
    val actualPositionCents: Long,
    val positionGapCents: Long,
    val healthPercent: Int,
    val topCategoryVariance: CategoryVariance?
)

data class CategoryVariance(
    val category: String,
    val plannedCents: Long,
    val actualCents: Long,
    val varianceCents: Long
)

data class PlanChartPoint(
    val dayMillis: Long,
    val plannedCumulativeSpendCents: Long,
    val actualCumulativeSpendCents: Long
)

data class PlanIncomeEvent(
    val dateMillis: Long,
    val amountCents: Long,
    val source: String
)

fun calculatePlanAnalysis(
    plan: TermPlan,
    expenses: List<Expense>,
    incomes: List<Income>,
    requestedRange: DateRange
): PlanPeriodAnalysis {
    val planRange = DateRange(startOfDay(plan.startDateMillis), endOfDay(plan.endDateMillis))
    val selected = intersectRanges(requestedRange, planRange)
        ?: DateRange(planRange.startMillis, planRange.startMillis)

    val planDays = daysBetweenInclusive(planRange.startMillis, planRange.endMillis).coerceAtLeast(1)
    val selectedDays = daysBetweenInclusive(selected.startMillis, selected.endMillis).coerceAtLeast(1)

    val totalPlannedExpenses = plan.plannedExpenses.values.sumOf { it }
    val healthyDaily = totalPlannedExpenses / planDays
    val plannedForSelected = proportionalAmount(totalPlannedExpenses, selectedDays, planDays)

    val actualForSelected = expenses
        .filter { it.dateMillis.isWithin(selected) }
        .sumOf { it.amountCents }

    val actualIncomeForSelected = incomes
        .filter { it.dateMillis.isWithin(selected) }
        .sumOf { it.amountCents }

    val elapsedEnd = minOf(selected.endMillis, planRange.endMillis)
    val elapsedDays = daysBetweenInclusive(planRange.startMillis, elapsedEnd)
        .coerceIn(1, planDays)

    val plannedSpendToDate = proportionalAmount(totalPlannedExpenses, elapsedDays, planDays)
    val actualSpendToDate = expenses
        .filter { it.dateMillis in planRange.startMillis..elapsedEnd }
        .sumOf { it.amountCents }
    val actualIncomeToDate = incomes
        .filter { it.dateMillis in planRange.startMillis..elapsedEnd }
        .sumOf { it.amountCents }

    val plannedPosition = plan.startingFundsCents +
        plan.expectedIncomeCents -
        plannedSpendToDate

    val actualPosition = plan.startingFundsCents +
        actualIncomeToDate -
        actualSpendToDate

    val categoryVariance = plan.plannedExpenses.keys
        .map { category ->
            val planned = plan.plannedExpenses[category] ?: 0L
            val plannedSelected = proportionalAmount(planned, selectedDays, planDays)
            val actual = expenses
                .filter { it.dateMillis.isWithin(selected) && it.category == category }
                .sumOf { it.amountCents }

            CategoryVariance(
                category = category,
                plannedCents = plannedSelected,
                actualCents = actual,
                varianceCents = actual - plannedSelected
            )
        }
        .filter { it.plannedCents > 0L || it.actualCents > 0L }
        .maxByOrNull { kotlin.math.abs(it.varianceCents) }

    return PlanPeriodAnalysis(
        range = selected,
        planDays = planDays,
        selectedDays = selectedDays,
        healthyDailySpendingCents = healthyDaily,
        plannedSpendingForPeriodCents = plannedForSelected,
        actualSpendingForPeriodCents = actualForSelected,
        spendingVarianceCents = actualForSelected - plannedForSelected,
        actualIncomeForPeriodCents = actualIncomeForSelected,
        plannedPositionCents = plannedPosition,
        actualPositionCents = actualPosition,
        positionGapCents = actualPosition - plannedPosition,
        healthPercent = calculateHealthPercent(plannedPosition, actualPosition),
        topCategoryVariance = categoryVariance
    )
}

fun buildPlanChart(
    plan: TermPlan,
    expenses: List<Expense>,
    range: DateRange
): List<PlanChartPoint> {
    val planRange = DateRange(startOfDay(plan.startDateMillis), endOfDay(plan.endDateMillis))
    val selected = intersectRanges(range, planRange) ?: return emptyList()

    val totalPlanDays = daysBetweenInclusive(planRange.startMillis, planRange.endMillis).coerceAtLeast(1)
    val totalPlannedExpenses = plan.plannedExpenses.values.sumOf { it }

    val expenseByDay = expenses
        .filter { it.dateMillis.isWithin(selected) }
        .groupBy { startOfDay(it.dateMillis) }
        .mapValues { (_, values) -> values.sumOf { it.amountCents } }

    var actualCumulative = 0L
    val points = mutableListOf<PlanChartPoint>()
    var day = startOfDay(selected.startMillis)

    while (day <= startOfDay(selected.endMillis)) {
        actualCumulative += expenseByDay[day] ?: 0L
        val selectedDays = daysBetweenInclusive(selected.startMillis, day)

        points += PlanChartPoint(
            dayMillis = day,
            plannedCumulativeSpendCents = proportionalAmount(
                totalPlannedExpenses,
                selectedDays,
                totalPlanDays
            ),
            actualCumulativeSpendCents = actualCumulative
        )

        day += DAY_MILLIS
    }

    return points
}

fun planIncomeEvents(
    incomes: List<Income>,
    range: DateRange
): List<PlanIncomeEvent> =
    incomes
        .filter { it.dateMillis.isWithin(range) }
        .sortedBy { it.dateMillis }
        .map { PlanIncomeEvent(it.dateMillis, it.amountCents, it.source) }

private fun proportionalAmount(amount: Long, selectedDays: Int, totalDays: Int): Long {
    if (amount == 0L || selectedDays <= 0 || totalDays <= 0) return 0L

    return BigDecimal.valueOf(amount)
        .multiply(BigDecimal.valueOf(selectedDays.toLong()))
        .divide(BigDecimal.valueOf(totalDays.toLong()), 0, RoundingMode.HALF_UP)
        .longValue()
}

private fun calculateHealthPercent(
    plannedPositionCents: Long,
    actualPositionCents: Long
): Int {
    if (plannedPositionCents <= 0L) {
        return if (actualPositionCents >= 0L) 100 else 0
    }

    val ratio = actualPositionCents.toDouble() / plannedPositionCents.toDouble()
    return (ratio * 100.0).coerceIn(0.0, 100.0).toInt()
}
