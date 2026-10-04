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
        val actualIncome = rangeIncome(transactions, plan.startMs, plan.endMs)
        val actualExpense = rangeExpense(transactions, plan.startMs, plan.endMs)

        val expectedRemaining = plan.startingMoneyPaise + expectedIncome - plannedExpense
        val actualRemaining = plan.startingMoneyPaise + actualIncome - actualExpense

        val expectedSpendToDate = if (totalDays <= 0) {
            plannedExpense
        } else {
            (plannedExpense.toDouble() * elapsedDays.toDouble() / totalDays.toDouble()).roundToLong()
        }

        val variance = actualExpense - expectedSpendToDate
        val averageSpend = if (elapsedDays > 0) actualExpense / elapsedDays else 0L
        val availablePerDay = if (remainingDays > 0) actualRemaining / remainingDays else 0L

        val threshold = max(50_000L, expectedSpendToDate / 5L)
        val status = when {
            todayMs < plan.startMs -> "Upcoming"
            todayMs > plan.endMs -> "Completed"
            actualRemaining < 0 -> "Overdrawn"
            variance > threshold -> "Above plan"
            variance < -threshold -> "Below plan"
            else -> "On track"
        }

        val guidance = when {
            todayMs < plan.startMs ->
                "Your plan starts on ${formatDay(plan.startMs)}. Add expected income and expenses before it begins."
            todayMs > plan.endMs ->
                "This plan has ended. Review actual spending against what you originally expected."
            actualRemaining < 0 ->
                "Recorded plan spending is above the money currently available in this plan."
            remainingDays == 0 ->
                "The plan ends today. Your remaining amount is the amount to carry forward or review."
            variance > threshold ->
                "Spending is currently ahead of the pace set by your plan. Your remaining daily amount is ${money(availablePerDay)}."
            variance < -threshold ->
                "Spending is currently below the pace set by your plan. Your remaining daily amount is ${money(availablePerDay)}."
            else ->
                "Your spending pace is close to the plan. You currently have about ${money(availablePerDay)} available per remaining day."
        }

        return PlanMetrics(
            totalExpectedIncomePaise = expectedIncome,
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

    private fun startOfDay(ms: Long): Long =
        Calendar.getInstance().apply {
            timeInMillis = ms
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun endOfDay(ms: Long): Long = startOfDay(ms) + DAY - 1

    private fun daysInclusive(startMs: Long, endMs: Long): Int {
        if (endMs < startMs) return 0
        return (((startOfDay(endMs) - startOfDay(startMs)) / DAY) + 1).toInt()
    }

    private fun sameDay(a: Long, b: Long): Boolean = startOfDay(a) == startOfDay(b)

    private fun formatDay(ms: Long): String =
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date(ms))

    private fun money(paise: Long): String =
        "₹" + String.format(java.util.Locale.getDefault(), "%,.0f", paise / 100.0)

    private const val DAY = 86_400_000L
}
