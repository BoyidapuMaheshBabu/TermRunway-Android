package com.termrunway.app.notifications

import java.util.Calendar

object MonthlyReminderCalculator {
    fun calculateNextMonthlyReminder(
        now: Calendar,
        targetHour: Int,
        targetMinute: Int
    ): Calendar {
        val target = (now.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, targetHour)
            set(Calendar.MINUTE, targetMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (!target.after(now)) {
            target.add(Calendar.MONTH, 1)
            target.set(Calendar.DAY_OF_MONTH, target.getActualMaximum(Calendar.DAY_OF_MONTH))
            target.set(Calendar.HOUR_OF_DAY, targetHour)
            target.set(Calendar.MINUTE, targetMinute)
            target.set(Calendar.SECOND, 0)
            target.set(Calendar.MILLISECOND, 0)
        }
        return target
    }

    fun calculateMonthlyDelayMs(
        nowMs: Long,
        targetHour: Int,
        targetMinute: Int
    ): Long {
        val now = Calendar.getInstance().apply { timeInMillis = nowMs }
        val target = calculateNextMonthlyReminder(now, targetHour, targetMinute)
        return target.timeInMillis - nowMs
    }
}
