package com.termrunway.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    private const val DAILY_WORK_TAG = "termrunway_daily_reminder"
    private const val WEEKLY_WORK_TAG = "termrunway_weekly_review"
    private const val MONTHLY_WORK_TAG = "termrunway_monthly_review"
    private const val PLAN_ENDING_WORK_TAG = "termrunway_plan_ending"

    fun syncReminders(
        context: Context,
        masterEnabled: Boolean,
        dailyEnabled: Boolean,
        weeklyEnabled: Boolean,
        monthlyEnabled: Boolean,
        planEndingEnabled: Boolean,
        targetHour: Int = 20,
        targetMinute: Int = 0
    ) {
        val workManager = WorkManager.getInstance(context)

        if (!masterEnabled || !NotificationHelper.hasNotificationPermission(context)) {
            workManager.cancelAllWorkByTag(DAILY_WORK_TAG)
            workManager.cancelAllWorkByTag(WEEKLY_WORK_TAG)
            workManager.cancelAllWorkByTag(MONTHLY_WORK_TAG)
            workManager.cancelAllWorkByTag(PLAN_ENDING_WORK_TAG)
            return
        }

        // Daily Reminder
        if (dailyEnabled) {
            val initialDelay = calculateInitialDelay(targetHour, targetMinute)
            val dailyWork = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(DAILY_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                DAILY_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                dailyWork
            )
        } else {
            workManager.cancelAllWorkByTag(DAILY_WORK_TAG)
        }

        // Weekly Review (Every 7 days, Sunday evening)
        if (weeklyEnabled) {
            val initialDelay = calculateWeeklyDelay(targetHour, targetMinute)
            val weeklyWork = PeriodicWorkRequestBuilder<WeeklyReviewWorker>(7, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(WEEKLY_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WEEKLY_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                weeklyWork
            )
        } else {
            workManager.cancelAllWorkByTag(WEEKLY_WORK_TAG)
        }

        // Monthly Review (Every 30 days)
        if (monthlyEnabled) {
            val initialDelay = calculateMonthlyDelay(targetHour, targetMinute)
            val monthlyWork = PeriodicWorkRequestBuilder<MonthlyReviewWorker>(30, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(MONTHLY_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                MONTHLY_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                monthlyWork
            )
        } else {
            workManager.cancelAllWorkByTag(MONTHLY_WORK_TAG)
        }

        // Plan Ending Alerts
        if (planEndingEnabled) {
            val planWork = PeriodicWorkRequestBuilder<PlanEndingWorker>(24, TimeUnit.HOURS)
                .addTag(PLAN_ENDING_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                PLAN_ENDING_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                planWork
            )
        } else {
            workManager.cancelAllWorkByTag(PLAN_ENDING_WORK_TAG)
        }
    }

    private fun calculateInitialDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun calculateWeeklyDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.WEEK_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }

    private fun calculateMonthlyDelay(targetHour: Int, targetMinute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
