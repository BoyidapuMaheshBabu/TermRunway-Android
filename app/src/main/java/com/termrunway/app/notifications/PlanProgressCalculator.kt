package com.termrunway.app.notifications

import com.termrunway.app.data.FinancialPlan
import com.termrunway.app.domain.FinancialCalculator
import java.time.LocalDate

data class MilestoneContent(
    val percentage: Int,
    val title: String,
    val message: String
)

object PlanProgressCalculator {

    val MILESTONES = listOf(
        MilestoneContent(50, "Your plan is halfway through", "50% of your planned period has passed. Check how your plan is going."),
        MilestoneContent(75, "Your plan is 75% complete", "Your plan is moving toward completion. Review your Activity and Insights to see how things are going."),
        MilestoneContent(90, "Your plan is 90% complete", "Your plan is in its final stretch. Take a look at your Activity and Insights."),
        MilestoneContent(95, "Your plan is almost complete", "95% of your planned period has passed. Review your plan before it ends."),
        MilestoneContent(100, "Your plan is complete", "Your planned period has ended. Check your Insights and Activity to understand how the plan went.")
    )

    fun calculateProgressPercent(startDate: LocalDate, endDate: LocalDate, today: LocalDate): Double {
        if (endDate.isBefore(startDate)) return 0.0
        val totalDays = FinancialCalculator.daysInclusive(startDate, endDate)
        if (totalDays <= 0) return 0.0
        if (today.isBefore(startDate)) return 0.0
        if (today.isAfter(endDate)) return 100.0
        val elapsedDays = FinancialCalculator.daysInclusive(startDate, today)
        return (elapsedDays.toDouble() / totalDays.toDouble() * 100.0)
    }

    fun selectPlanForProgress(
        plans: List<FinancialPlan>,
        isPlanCompleted: (Long) -> Boolean,
        today: LocalDate
    ): FinancialPlan? {
        val sorted = plans.sortedWith(compareByDescending<FinancialPlan> { it.startDate }.thenByDescending { it.id })
        // Active plan that is not completed yet
        val active = sorted.firstOrNull {
            !today.isBefore(it.startDate) && !today.isAfter(it.endDate) && !isPlanCompleted(it.id)
        }
        if (active != null) return active

        // Recently ended plan whose completion milestone has not been delivered yet
        return sorted.firstOrNull {
            !today.isBefore(it.startDate) && today.isAfter(it.endDate) && !isPlanCompleted(it.id)
        }
    }

    fun evaluateMilestoneToNotify(
        progressPercent: Double,
        isNotified: (Int) -> Boolean
    ): MilestoneContent? {
        val eligible = MILESTONES.filter { progressPercent >= it.percentage }
        val unnotified = eligible.filter { !isNotified(it.percentage) }
        return unnotified.maxByOrNull { it.percentage }
    }
}
