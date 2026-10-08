package com.termrunway.app.domain

import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import java.util.Calendar
import kotlin.math.max
import kotlin.math.roundToLong

object FinancialCalculator {
    fun totalByType(transactions: List<Transaction>, type: TransactionType): Long =
        transactions.asSequence().filter { it.type == type }.sumOf { it.amountPaise }

    fun balance(transactions: List<Transaction>): Long =
        totalByType(transactions, TransactionType.INCOME) -
            totalByType(transactions, TransactionType.EXPENSE)

    fun dayIncome(transactions: List<Transaction>, dayMs: Long): Long =
        transactions.filter { it.type == TransactionType.INCOME && sameDay(it.dateMs, dayMs) }
            .sumOf { it.amountPaise }

    fun dayExpense(transactions: List<Transaction>, dayMs: Long): Long =
        transactions.filter { it.type == TransactionType.EXPENSE && sameDay(it.dateMs, dayMs) }
            .sumOf { it.amountPaise }

    fun rangeIncome(transactions: List<Transaction>, startMs: Long, endMs: Long): Long =
        transactions.filter {
            it.type == TransactionType.INCOME &&
                it.dateMs in startOfDay(startMs)..endOfDay(endMs)
        }.sumOf { it.amountPaise }

    fun rangeExpense(transactions: List<Transaction>, startMs: Long, endMs: Long): Long =
        transactions.filter {
            it.type == TransactionType.EXPENSE &&
                it.dateMs in startOfDay(startMs)..endOfDay(endMs)
        }.sumOf { it.amountPaise }

    fun categoryTotals(
        transactions: List<Transaction>,
        type: TransactionType,
        startMs: Long? = null,
        endMs: Long? = null
    ): Map<String, Long> {
        return transactions.asSequence()
            .filter { it.type == type }
            .filter {
                if (startMs == null || endMs == null) true
                else it.dateMs in startOfDay(startMs)..endOfDay(endMs)
            }
            .groupingBy { it.category }
            .fold(0L) { acc, transaction -> acc + transaction.amountPaise }
            .toList()
            .sortedByDescending { it.second }
            .toMap(LinkedHashMap())
    }

    fun planMetrics(
        plan: FinancialPlan,
        plannedIncome: List<PlannedIncome>,
        plannedExpenses: List<PlannedExpense>,
        transactions: List<Transaction>,
        todayMs: Long = System.currentTimeMillis()
    ): PlanMetrics {
        val totalDays = daysInclusive(plan.startMs, plan.endMs)
        val remainingDays = when {
            todayMs < plan.startMs -> totalDays
            todayMs > plan.endMs -> 0
            else -> daysInclusive(startOfDay(todayMs), endOfDay(plan.endMs))
        }
        val elapsedDays = when {
            todayMs < plan.startMs -> 0
            todayMs > plan.endMs -> totalDays
            else -> daysInclusive(plan.startMs, todayMs)
        }

        val expectedIncome = plannedIncome.sumOf { it.amountPaise }
        val plannedExpense = plannedExpenses.sumOf { it.amountPaise }

        // Time-aware expected income to date
        val expectedIncomeToDate = when {
            todayMs < plan.startMs -> 0L
            todayMs > plan.endMs -> expectedIncome
            else -> {
                val todayStart = startOfDay(todayMs)
                plannedIncome.sumOf { income ->
                    val incDate = income.expectedDateMs
                    if (incDate != null) {
                        if (todayStart >= startOfDay(incDate)) {
                            income.amountPaise
                        } else {
                            0L
                        }
                    } else {
                        if (totalDays <= 0) income.amountPaise
                        else (income.amountPaise.toDouble() * elapsedDays.toDouble() / totalDays.toDouble()).roundToLong()
                    }
                }
            }
        }

        // Actual transactions up to today for active plans
        val actualIncome = when {
            todayMs < plan.startMs -> 0L
            todayMs > plan.endMs -> rangeIncome(transactions, plan.startMs, plan.endMs)
            else -> rangeIncome(transactions, plan.startMs, minOf(plan.endMs, todayMs))
        }
        val actualExpense = when {
            todayMs < plan.startMs -> 0L
            todayMs > plan.endMs -> rangeExpense(transactions, plan.startMs, plan.endMs)
            else -> rangeExpense(transactions, plan.startMs, minOf(plan.endMs, todayMs))
        }

        val expectedRemaining = plan.startingMoneyPaise + expectedIncome - plannedExpense
        val actualRemaining = plan.startingMoneyPaise + actualIncome - actualExpense

        // Time-aware expected spend to date
        val expectedSpendToDate = when {
            todayMs < plan.startMs -> 0L
            todayMs > plan.endMs -> plannedExpense
            else -> {
                val todayStart = startOfDay(todayMs)
                plannedExpenses.sumOf { expense ->
                    val expDate = expense.expectedDateMs
                    if (expDate != null) {
                        if (todayStart >= startOfDay(expDate)) {
                            expense.amountPaise
                        } else {
                            0L
                        }
                    } else {
                        if (totalDays <= 0) expense.amountPaise
                        else (expense.amountPaise.toDouble() * elapsedDays.toDouble() / totalDays.toDouble()).roundToLong()
                    }
                }
            }
        }

        val variance = actualExpense - expectedSpendToDate
        val averageSpend = if (elapsedDays > 0) actualExpense / elapsedDays else 0L

        // Discretionary safe spending considering future planned commitments
        val todayStart = startOfDay(todayMs)
        val futureCommitments = when {
            todayMs < plan.startMs -> plannedExpense
            todayMs > plan.endMs -> 0L
            else -> plannedExpenses.filter { it.expectedDateMs != null && startOfDay(it.expectedDateMs) > todayStart }.sumOf { it.amountPaise }
        }
        val pastOrUndatedPlanned = when {
            todayMs < plan.startMs -> 0L
            todayMs > plan.endMs -> plannedExpense
            else -> plannedExpenses.filter { it.expectedDateMs == null || startOfDay(it.expectedDateMs) <= todayStart }.sumOf { it.amountPaise }
        }
        val remainingPastOrUndated = max(0L, pastOrUndatedPlanned - actualExpense)
        val totalRemainingPlannedCommitments = futureCommitments + remainingPastOrUndated
        val discretionaryRemaining = max(0L, actualRemaining - totalRemainingPlannedCommitments)
        val availablePerDay = if (remainingDays > 0) discretionaryRemaining / remainingDays else 0L

        val ratio = if (expectedSpendToDate > 0) actualExpense.toDouble() / expectedSpendToDate.toDouble() else null
        val status = when {
            todayMs < plan.startMs -> "Upcoming"
            todayMs > plan.endMs -> "Completed"
            actualRemaining < 0 -> "Overdrawn"
            ratio != null && ratio > 1.05 -> "Above plan"
            ratio != null && ratio < 0.95 -> "Below plan"
            expectedSpendToDate == 0L && actualExpense > 0 -> "Above plan"
            else -> "On track"
        }

        val guidance = when {
            todayMs < plan.startMs ->
                "Your plan starts in ${daysInclusive(todayMs, plan.startMs)} days (${formatDay(plan.startMs)}). Your planned income and expenses are ready for the period."
            todayMs > plan.endMs ->
                "This plan has ended. Final actual remaining: ${money(actualRemaining)}."
            actualRemaining < 0 ->
                "Recorded plan spending is above available money. Safe to spend today: ₹0. Review your remaining money and planned expenses."
            remainingDays == 0 ->
                "The plan ends today. Remaining runway is ${money(actualRemaining)}."
            discretionaryRemaining == 0L && actualRemaining > 0L ->
                "Most of your remaining money is committed to planned expenses. Suggested safe spending today is ₹0."
            status == "Above plan" ->
                "Spending is currently above the pace expected by your plan. Suggested safe spending today is ${money(availablePerDay)}."
            status == "Below plan" ->
                "Spending is currently below the pace expected by your plan. Suggested safe spending today is ${money(availablePerDay)}."
            else ->
                "Your spending pace matches your plan. Suggested safe spending today is ${money(availablePerDay)}."
        }

        return PlanMetrics(
            totalExpectedIncomePaise = expectedIncome,
            expectedIncomeToDatePaise = expectedIncomeToDate,
            totalPlannedExpensePaise = plannedExpense,
            actualIncomePaise = actualIncome,
            actualExpensePaise = actualExpense,
            expectedRemainingPaise = expectedRemaining,
            actualRemainingPaise = actualRemaining,
            daysTotal = totalDays,
            daysElapsed = elapsedDays,
            daysRemaining = remainingDays,
            availablePerDayPaise = availablePerDay,
            actualAverageDailySpendPaise = averageSpend,
            expectedSpendToDatePaise = expectedSpendToDate,
            spendVariancePaise = variance,
            status = status,
            guidance = guidance
        )
    }

    fun startOfDay(ms: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    fun endOfDay(ms: Long): Long = startOfDay(ms) + DAY - 1

    fun daysInclusive(startMs: Long, endMs: Long): Int {
        if (endMs < startMs) return 0
        return (((startOfDay(endMs) - startOfDay(startMs)) / DAY) + 1).toInt()
    }

    fun sameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

    private fun formatDay(ms: Long): String =
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(ms))

    private fun money(paise: Long): String =
        "₹" + String.format(java.util.Locale.getDefault(), "%,.0f", paise / 100.0)

    private const val DAY = 86_400_000L
}
