package com.termrunway.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.termrunway.app.data.PreferencesKeys.DAILY_REMINDER_KEY
import com.termrunway.app.data.PreferencesKeys.MONTHLY_REVIEW_KEY
import com.termrunway.app.data.PreferencesKeys.NOTIFICATIONS_ENABLED_KEY
import com.termrunway.app.data.PreferencesKeys.PLAN_ENDING_KEY
import com.termrunway.app.data.PreferencesKeys.REMINDER_HOUR_KEY
import com.termrunway.app.data.PreferencesKeys.REMINDER_MINUTE_KEY
import com.termrunway.app.data.PreferencesKeys.WEEKLY_REVIEW_KEY
import com.termrunway.app.data.termRunwayDataStore
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    const val DAILY_WORK_TAG = "termrunway_daily_reminder"
    const val WEEKLY_WORK_TAG = "termrunway_weekly_review"
    const val MONTHLY_WORK_TAG = "termrunway_monthly_review"
    const val PLAN_PROGRESS_WORK_TAG = "termrunway_plan_progress"

    suspend fun syncFromPreferences(context: Context) {
        runCatching {
            val prefs = context.termRunwayDataStore.data.first()
            syncReminders(
                context = context,
                masterEnabled = prefs[NOTIFICATIONS_ENABLED_KEY] ?: true,
                dailyEnabled = prefs[DAILY_REMINDER_KEY] ?: true,
                weeklyEnabled = prefs[WEEKLY_REVIEW_KEY] ?: true,
                monthlyEnabled = prefs[MONTHLY_REVIEW_KEY] ?: true,
                planEndingEnabled = prefs[PLAN_ENDING_KEY] ?: true,
                targetHour = prefs[REMINDER_HOUR_KEY] ?: 20,
                targetMinute = prefs[REMINDER_MINUTE_KEY] ?: 0
            )
        }
    }

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
            workManager.cancelAllWorkByTag(PLAN_PROGRESS_WORK_TAG)
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

        // Monthly Review (Schedules for the last calendar day of each month)
        workManager.cancelUniqueWork(MONTHLY_WORK_TAG)
        workManager.cancelAllWorkByTag(MONTHLY_WORK_TAG)

        if (monthlyEnabled) {
            val initialDelay = MonthlyReminderCalculator.calculateMonthlyDelayMs(
                nowMs = System.currentTimeMillis(),
                targetHour = targetHour,
                targetMinute = targetMinute
            )
            val monthlyWork = OneTimeWorkRequestBuilder<MonthlyReviewWorker>()
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(MONTHLY_WORK_TAG)
                .build()

            workManager.enqueueUniqueWork(
                MONTHLY_WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                monthlyWork
            )
        }

        // Plan Progress Updates
        if (planEndingEnabled) {
            val initialDelay = calculateInitialDelay(targetHour, targetMinute)
            val planWork = PeriodicWorkRequestBuilder<PlanProgressWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag(PLAN_PROGRESS_WORK_TAG)
                .build()

            workManager.enqueueUniquePeriodicWork(
                PLAN_PROGRESS_WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                planWork
            )
        } else {
            workManager.cancelAllWorkByTag(PLAN_PROGRESS_WORK_TAG)
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
}
