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

class PlanEndingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = TermRunwayRepository(applicationContext)
        val plans = repository.listPlans()
        val todayStartMs = startOfToday()

        val active = plans.firstOrNull { it.endMs >= todayStartMs } ?: return Result.success()
        val daysRemaining = FinancialCalculator.daysInclusive(todayStartMs, active.endMs)

        when {
            daysRemaining == 0 || FinancialCalculator.sameDay(todayStartMs, active.endMs) -> {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    notificationId = NotificationHelper.PLAN_ENDING_ID,
                    title = "Plan Milestone",
                    message = "Your plan '${active.name}' ends today. Review your final runway."
                )
            }
            daysRemaining in 1..3 -> {
                NotificationHelper.showNotification(
                    context = applicationContext,
                    notificationId = NotificationHelper.PLAN_ENDING_ID,
                    title = "Plan Milestone",
                    message = "Your plan '${active.name}' ends in $daysRemaining days. Check your remaining runway."
                )
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
