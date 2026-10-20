package com.termrunway.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.termrunway.app.data.TermRunwayRepository
import com.termrunway.app.domain.FinancialCalculator
import java.time.LocalDate

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TermRunwayRepository(applicationContext)
        val today = LocalDate.now()
        val todayTransactions = repository.listTransactions().filter {
            FinancialCalculator.sameDay(it.date, today)
        }

        if (todayTransactions.isEmpty()) {
            NotificationHelper.showNotification(
                context = applicationContext,
                notificationId = NotificationHelper.DAILY_REMINDER_ID,
                title = "Daily Money Reminder",
                message = "Take a moment to record today's transactions."
            )
        }
        return Result.success()
    }
}

class WeeklyReviewWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            context = applicationContext,
            notificationId = NotificationHelper.WEEKLY_REVIEW_ID,
            title = "Weekly Money Review",
            message = "Review your spending and income for the past week in TermRunway."
        )
        return Result.success()
    }
}

class MonthlyReviewWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationHelper.showNotification(
            context = applicationContext,
            notificationId = NotificationHelper.MONTHLY_REVIEW_ID,
            title = "Monthly Money Review",
            message = "Your monthly cash flow summary is ready in TermRunway."
        )
        NotificationScheduler.syncFromPreferences(applicationContext)
        return Result.success()
    }
}

class PlanProgressWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TermRunwayRepository(applicationContext)
        val plans = repository.listPlans()
        val today = LocalDate.now()
        val prefs = applicationContext.getSharedPreferences("termrunway_milestones", Context.MODE_PRIVATE)

        val targetPlan = PlanProgressCalculator.selectPlanForProgress(
            plans = plans,
            isPlanCompleted = { planId -> prefs.getBoolean("plan_${planId}_100", false) },
            today = today
        ) ?: return Result.success()

        val planId = targetPlan.id
        val progressPercent = PlanProgressCalculator.calculateProgressPercent(
            startDate = targetPlan.startDate,
            endDate = targetPlan.endDate,
            today = today
        )

        val isInitialized = prefs.getBoolean("plan_${planId}_init", false)
        if (!isInitialized) {
            val editor = prefs.edit().putBoolean("plan_${planId}_init", true)
            // Mark intermediate passed milestones as baseline without notifying
            PlanProgressCalculator.MILESTONES.forEach { milestone ->
                if (progressPercent >= milestone.percentage && milestone.percentage < 100) {
                    editor.putBoolean("plan_${planId}_${milestone.percentage}", true)
                }
            }
            editor.apply()
        }

        val milestoneToNotify = PlanProgressCalculator.evaluateMilestoneToNotify(
            progressPercent = progressPercent,
            isNotified = { percent -> prefs.getBoolean("plan_${planId}_$percent", false) }
        )

        if (milestoneToNotify != null) {
            NotificationHelper.showNotification(
                context = applicationContext,
                notificationId = NotificationHelper.PLAN_PROGRESS_ID,
                title = milestoneToNotify.title,
                message = milestoneToNotify.message
            )
            val editor = prefs.edit()
            PlanProgressCalculator.MILESTONES.forEach { m ->
                if (m.percentage <= milestoneToNotify.percentage) {
                    editor.putBoolean("plan_${planId}_${m.percentage}", true)
                }
            }
            editor.apply()
        }

        return Result.success()
    }
}
