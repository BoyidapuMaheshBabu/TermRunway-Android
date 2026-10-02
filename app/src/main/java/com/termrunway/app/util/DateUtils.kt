package com.termrunway.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

fun addDays(dayStartMillis: Long, days: Int): Long =
    Calendar.getInstance().apply {
        timeInMillis = dayStartMillis
        add(Calendar.DAY_OF_YEAR, days)
    }.timeInMillis

fun isToday(dayStartMillis: Long): Boolean =
    startOfDay(System.currentTimeMillis()) == dayStartMillis

fun formatDay(millis: Long): String =
    SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH).format(Date(millis))

fun formatTime(millis: Long): String =
    SimpleDateFormat("h:mm a", Locale.ENGLISH).format(Date(millis))

fun dayOnlyWithTime(dayStartMillis: Long, preferCurrentTime: Boolean): Long {
    if (preferCurrentTime) return System.currentTimeMillis()
    return Calendar.getInstance().apply {
        timeInMillis = dayStartMillis
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

fun mergeDateKeepingTime(newDayStartMillis: Long, oldDateMillis: Long): Long {
    val old = Calendar.getInstance().apply { timeInMillis = oldDateMillis }
    return Calendar.getInstance().apply {
        timeInMillis = newDayStartMillis
        set(Calendar.HOUR_OF_DAY, old.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, old.get(Calendar.MINUTE))
        set(Calendar.SECOND, old.get(Calendar.SECOND))
        set(Calendar.MILLISECOND, old.get(Calendar.MILLISECOND))
    }.timeInMillis
}
