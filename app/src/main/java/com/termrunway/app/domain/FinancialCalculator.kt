package com.termrunway.app.domain

import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.data.PlanMetrics
import com.termrunway.app.data.PlannedExpense
import com.termrunway.app.data.PlannedIncome
import com.termrunway.app.data.Transaction
import com.termrunway.app.data.TransactionType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToLong

object FinancialCalculator {
    fun totalByType(transactions: List<Transaction>, type: TransactionType): Long =
        transactions.asSequence().filter { it.type == type }.sumOf { it.amountPaise }

    fun balance(transactions: List<Transaction>): Long =
        totalByType(transactions, TransactionType.INCOME) -
            totalByType(transactions, TransactionType.EXPENSE)

    fun dayIncome(transactions: List<Transaction>, day: LocalDate = LocalDate.now()): Long =
        transactions.filter { it.type == TransactionType.INCOME && sameDay(it.date, day) }
            .sumOf { it.amountPaise }

    fun dayExpense(transactions: List<Transaction>, day: LocalDate = LocalDate.now()): Long =
        transactions.filter { it.type == TransactionType.EXPENSE && sameDay(it.date, day) }
            .sumOf { it.amountPaise }

    fun rangeIncome(transactions: List<Transaction>, start: LocalDate, end: LocalDate): Long =
        transactions.filter {
            it.type == TransactionType.INCOME &&
                !it.date.isBefore(start) &&
                !it.date.isAfter(end)
        }.sumOf { it.amountPaise }

    fun rangeExpense(transactions: List<Transaction>, start: LocalDate, end: LocalDate): Long =
        transactions.filter {
            it.type == TransactionType.EXPENSE &&
                !it.date.isBefore(start) &&
                !it.date.isAfter(end)
        }.sumOf { it.amountPaise }

    fun categoryTotals(
        transactions: List<Transaction>,
        type: TransactionType,
        start: LocalDate? = null,
        end: LocalDate? = null
    ): Map<String, Long> {
        return transactions.asSequence()
            .filter { it.type == type }
            .filter {
                if (start == null || end == null) true
                else !it.date.isBefore(start) && !it.date.isAfter(end)
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
        today: LocalDate = LocalDate.now()
    ): PlanMetrics {
        val totalDays = daysInclusive(plan.startDate, plan.endDate)
        val remainingDays = when {
            today.isBefore(plan.startDate) -> totalDays
            today.isAfter(plan.endDate) -> 0
            else -> daysInclusive(today, plan.endDate)
        }
        val elapsedDays = when {
            today.isBefore(plan.startDate) -> 0
            today.isAfter(plan.endDate) -> totalDays
            else -> daysInclusive(plan.startDate, today)
        }

        val expectedIncome = plannedIncome.sumOf { it.amountPaise }
        val plannedExpense = plannedExpenses.sumOf { it.amountPaise }

        // Time-aware expected income to date
        val expectedIncomeToDate = when {
            today.isBefore(plan.startDate) -> 0L
            today.isAfter(plan.endDate) -> expectedIncome
            else -> {
                plannedIncome.sumOf { income ->
                    val incDate = income.expectedDate
                    if (incDate != null) {
                        if (!today.isBefore(incDate)) {
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
            today.isBefore(plan.startDate) -> 0L
            today.isAfter(plan.endDate) -> rangeIncome(transactions, plan.startDate, plan.endDate)
            else -> rangeIncome(transactions, plan.startDate, minOf(plan.endDate, today))
        }
        val actualExpense = when {
            today.isBefore(plan.startDate) -> 0L
            today.isAfter(plan.endDate) -> rangeExpense(transactions, plan.startDate, plan.endDate)
            else -> rangeExpense(transactions, plan.startDate, minOf(plan.endDate, today))
        }

        val expectedRemaining = plan.startingMoneyPaise + expectedIncome - plannedExpense
        val actualRemaining = plan.startingMoneyPaise + actualIncome - actualExpense

        // Time-aware expected spend to date
        val expectedSpendToDate = when {
            today.isBefore(plan.startDate) -> 0L
            today.isAfter(plan.endDate) -> plannedExpense
            else -> {
                plannedExpenses.sumOf { expense ->
                    val expDate = expense.expectedDate
                    if (expDate != null) {
                        if (!today.isBefore(expDate)) {
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
        val futureCommitments = when {
            today.isBefore(plan.startDate) -> plannedExpense
            today.isAfter(plan.endDate) -> 0L
            else -> plannedExpenses.filter { it.expectedDate != null && it.expectedDate.isAfter(today) }.sumOf { it.amountPaise }
        }
        val pastOrUndatedPlanned = when {
            today.isBefore(plan.startDate) -> 0L
            today.isAfter(plan.endDate) -> plannedExpense
            else -> plannedExpenses.filter { it.expectedDate == null || !it.expectedDate.isAfter(today) }.sumOf { it.amountPaise }
        }
        val remainingPastOrUndated = max(0L, pastOrUndatedPlanned - actualExpense)
        val totalRemainingPlannedCommitments = futureCommitments + remainingPastOrUndated
        val discretionaryRemaining = max(0L, actualRemaining - totalRemainingPlannedCommitments)
        val availablePerDay = if (remainingDays > 0) discretionaryRemaining / remainingDays else 0L

        val ratio = if (expectedSpendToDate > 0) actualExpense.toDouble() / expectedSpendToDate.toDouble() else null
        val status = when {
            today.isBefore(plan.startDate) -> "Upcoming"
            today.isAfter(plan.endDate) -> "Completed"
            actualRemaining < 0 -> "Overdrawn"
            ratio != null && ratio > 1.05 -> "Above plan"
            ratio != null && ratio < 0.95 -> "Below plan"
            expectedSpendToDate == 0L && actualExpense > 0 -> "Above plan"
            else -> "On track"
        }

        val guidance = when {
            today.isBefore(plan.startDate) ->
                "Your plan starts in ${daysInclusive(today, plan.startDate)} days (${formatDay(plan.startDate)}). Your planned income and expenses are ready for the period."
            today.isAfter(plan.endDate) ->
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

    fun daysInclusive(start: LocalDate, end: LocalDate): Int {
        if (end.isBefore(start)) return 0
        return ChronoUnit.DAYS.between(start, end).toInt() + 1
    }

    fun sameDay(a: LocalDate, b: LocalDate): Boolean = a == b

    private fun formatDay(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault()))

    private fun money(paise: Long): String =
        "₹" + String.format(Locale.getDefault(), "%,.0f", paise / 100.0)
}
