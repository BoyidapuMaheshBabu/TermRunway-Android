package com.termrunway.app.logic

data class DateRange(
    val startMillis: Long,
    val endMillis: Long
)

enum class TrackingPeriod {
    SEVEN_DAYS,
    ONE_MONTH,
    THREE_MONTHS,
    CUSTOM
}

fun startOfDay(millis: Long): Long =
    java.util.Calendar.getInstance().apply {
        timeInMillis = millis
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis

fun endOfDay(millis: Long): Long =
    java.util.Calendar.getInstance().apply {
        timeInMillis = millis
        set(java.util.Calendar.HOUR_OF_DAY, 23)
        set(java.util.Calendar.MINUTE, 59)
        set(java.util.Calendar.SECOND, 59)
        set(java.util.Calendar.MILLISECOND, 999)
    }.timeInMillis

fun daysBetweenInclusive(startMillis: Long, endMillis: Long): Int {
    if (endMillis < startMillis) return 0
    val start = startOfDay(startMillis)
    val end = startOfDay(endMillis)
    return ((end - start) / DAY_MILLIS).toInt() + 1
}

fun trackingRange(
    period: TrackingPeriod,
    todayMillis: Long = System.currentTimeMillis(),
    customRange: DateRange? = null
): DateRange =
    when (period) {
        TrackingPeriod.SEVEN_DAYS -> DateRange(
            startOfDay(todayMillis - 6L * DAY_MILLIS),
            endOfDay(todayMillis)
        )
        TrackingPeriod.ONE_MONTH -> DateRange(
            startOfDay(todayMillis - 29L * DAY_MILLIS),
            endOfDay(todayMillis)
        )
        TrackingPeriod.THREE_MONTHS -> DateRange(
            startOfDay(todayMillis - 89L * DAY_MILLIS),
            endOfDay(todayMillis)
        )
        TrackingPeriod.CUSTOM -> customRange ?: DateRange(
            startOfDay(todayMillis - 29L * DAY_MILLIS),
            endOfDay(todayMillis)
        )
    }

fun intersectRanges(first: DateRange, second: DateRange): DateRange? {
    val start = maxOf(first.startMillis, second.startMillis)
    val end = minOf(first.endMillis, second.endMillis)
    return if (start <= end) DateRange(start, end) else null
}

fun Long.isWithin(range: DateRange): Boolean =
    this in range.startMillis..range.endMillis

const val DAY_MILLIS = 24L * 60L * 60L * 1000L
