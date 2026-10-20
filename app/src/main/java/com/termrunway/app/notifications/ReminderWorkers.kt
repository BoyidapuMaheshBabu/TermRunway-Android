package com.termrunway.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.termrunway.app.data.TermRunwayRepository
import com.termrunway.app.domain.FinancialCalculator
import java.util.Calendar

class DailyReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TermRunwayRepository(applicationContext)
        val todayMs = System.currentTimeMillis()
        val todayTransactions = repository.listTransactions().filter {
            FinancialCalculator.sameDay(it.dateMs, todayMs)
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
        val todayStartMs = startOfToday()

        val active = plans.firstOrNull { it.endMs >= todayStartMs && it.startMs <= todayStartMs } ?: return Result.success()
        val totalDays = FinancialCalculator.daysInclusive(active.startMs, active.endMs).coerceAtLeast(1)
        val elapsedDays = FinancialCalculator.daysInclusive(active.startMs, todayStartMs)
        val progressPercent = (elapsedDays.toDouble() / totalDays.toDouble() * 100.0)

        val prefs = applicationContext.getSharedPreferences("termrunway_milestones", Context.MODE_PRIVATE)
        val planId = active.id
        val isInitialized = prefs.getBoolean("plan_${planId}_init", false)

        val milestones = listOf(
            50 to ("Your plan is halfway through" to "50% of your planned period has passed. Check how your plan is going."),
            75 to ("Your plan is 75% complete" to "Your plan is moving toward completion. Review your Activity and Insights to see how things are going."),
            90 to ("Your plan is 90% complete" to "Your plan is in its final stretch. Take a look at your Activity and Insights."),
            95 to ("Your plan is almost complete" to "95% of your planned period has passed. Review your plan before it ends."),
            100 to ("Your plan is complete" to "Your planned period has ended. Check your Insights and Activity to understand how the plan went.")
        )

        if (!isInitialized) {
            val editor = prefs.edit().putBoolean("plan_${planId}_init", true)
            // Mark passed milestones as baseline without notifying
            milestones.forEach { (percent, _) ->
                if (progressPercent >= percent && percent < 100) {
                    editor.putBoolean("plan_${planId}_$percent", true)
                }
            }
            editor.apply()
        }

        for ((percent, content) in milestones) {
            val key = "plan_${planId}_$percent"
            val alreadyNotified = prefs.getBoolean(key, false)
            if (progressPercent >= percent && !alreadyNotified) {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    notificationId = NotificationHelper.PLAN_PROGRESS_ID,
                    title = content.first,
                    message = content.second
                )
                prefs.edit().putBoolean(key, true).apply()
                break
            }
        }

        return Result.success()
    }

    private fun startOfToday(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
