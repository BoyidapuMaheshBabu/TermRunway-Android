package com.termrunway.app.logic

import com.termrunway.app.data.Expense
import com.termrunway.app.data.Income
import com.termrunway.app.data.TermPlan
import java.util.Calendar

data class RunwayForecast(
    val availableNowCents: Long,
    val expectedFutureIncomeCents: Long,
    val totalPlannedExpensesCents: Long,
    val remainingPlannedExpensesCents: Long,
    val projectedBalanceCents: Long,
    val daysRemaining: Int,
    val dailySafeSpendingCents: Long,
    val status: RunwayStatus
)

enum class RunwayStatus {
    BUFFER,
    TIGHT,
    SHORTFALL
}

fun calculateRunway(
    plan: TermPlan?,
    expenses: List<Expense>,
    incomes: List<Income>,
    currentDateMillis: Long = System.currentTimeMillis()
): RunwayForecast? {
    if (plan == null) return null

    val termExpenses = expenses.filter { it.dateMillis >= plan.startDateMillis && it.dateMillis <= plan.endDateMillis }
    val termIncomes = incomes.filter { it.dateMillis >= plan.startDateMillis && it.dateMillis <= plan.endDateMillis }

    val actualExpensesThisTerm = termExpenses.sumOf { it.amountCents }
    val actualIncomeThisTerm = termIncomes.sumOf { it.amountCents }

    val availableNowCents = plan.startingFundsCents + actualIncomeThisTerm - actualExpensesThisTerm

    var totalPlannedExpensesCents = 0L
    var remainingPlannedExpensesCents = 0L

    plan.plannedExpenses.forEach { (category, plannedAmount) ->
        totalPlannedExpensesCents += plannedAmount
        val actualForCategory = termExpenses.filter { it.category == category }.sumOf { it.amountCents }
        val remainingForCategory = (plannedAmount - actualForCategory).coerceAtLeast(0L)
        remainingPlannedExpensesCents += remainingForCategory
    }

    val projectedBalanceCents = availableNowCents + plan.expectedIncomeCents - remainingPlannedExpensesCents

    val daysRemaining = calculateDaysBetween(currentDateMillis, plan.endDateMillis).coerceAtLeast(0)
    
    val dailySafeSpendingCents = if (daysRemaining > 0) {
        (projectedBalanceCents.coerceAtLeast(0L)) / daysRemaining
    } else {
        0L
    }

    val status = when {
        projectedBalanceCents < 0 -> RunwayStatus.SHORTFALL
        projectedBalanceCents < (totalPlannedExpensesCents * 0.1).toLong() -> RunwayStatus.TIGHT
        else -> RunwayStatus.BUFFER
    }

    return RunwayForecast(
        availableNowCents = availableNowCents,
        expectedFutureIncomeCents = plan.expectedIncomeCents,
        totalPlannedExpensesCents = totalPlannedExpensesCents,
        remainingPlannedExpensesCents = remainingPlannedExpensesCents,
        projectedBalanceCents = projectedBalanceCents,
        daysRemaining = daysRemaining,
        dailySafeSpendingCents = dailySafeSpendingCents,
        status = status
    )
}

fun calculateDaysBetween(startMillis: Long, endMillis: Long): Int {
    val start = Calendar.getInstance().apply { timeInMillis = startMillis }
    val end = Calendar.getInstance().apply { timeInMillis = endMillis }
    
    // Normalize to start of day
    start.set(Calendar.HOUR_OF_DAY, 0)
    start.set(Calendar.MINUTE, 0)
    start.set(Calendar.SECOND, 0)
    start.set(Calendar.MILLISECOND, 0)

    end.set(Calendar.HOUR_OF_DAY, 0)
    end.set(Calendar.MINUTE, 0)
    end.set(Calendar.SECOND, 0)
    end.set(Calendar.MILLISECOND, 0)

    val diff = end.timeInMillis - start.timeInMillis
    return (diff / (1000 * 60 * 60 * 24)).toInt()
}
