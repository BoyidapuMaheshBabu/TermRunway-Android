package com.termrunway.app.ui.util

import android.app.DatePickerDialog
import android.content.Context
import com.termrunway.app.data.FinancialPlan
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun pickDate(
    context: Context,
    current: LocalDate,
    maxDate: LocalDate? = null,
    onPicked: (LocalDate) -> Unit
) {
    val dialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            onPicked(LocalDate.of(year, month + 1, dayOfMonth))
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth
    )
    maxDate?.let {
        dialog.datePicker.maxDate = it.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    dialog.show()
}

fun addDays(date: LocalDate, count: Int): LocalDate = date.plusDays(count.toLong())

fun parseMoneyOrNull(value: String): Long? =
    runCatching {
        val normalized = value.trim().replace(",", "")
        if (normalized.isEmpty()) return null
        val bd = java.math.BigDecimal(normalized)
        if (bd < java.math.BigDecimal.ZERO) return null
        bd.setScale(2, java.math.RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()

fun parseMoney(value: String): Long =
    parseMoneyOrNull(value) ?: 0L

fun moneyInput(paise: Long): String =
    String.format(Locale.getDefault(), "%.2f", paise / 100.0)

fun fileDate(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault()))

fun sanitizeFilename(userName: String, extension: String = "json"): String {
    val date = fileDate()
    val cleanName = userName
        .replace(Regex("[/\\\\:*?\"<>|]"), " ")
        .replace(Regex("\\s+"), "_")
        .trim('_')
        .take(40)
        .ifEmpty { "User" }
    return "TermRunway_${cleanName}_$date.$extension"
}

enum class PeriodPreset(val label: String) {
    DAYS_7("7 days"),
    DAYS_30("30 days"),
    DAYS_90("90 days"),
    ACTIVE_PLAN("Active plan"),
    ALL_TIME("All time"),
    CUSTOM("Custom range")
}

data class DatePeriodRange(
    val start: LocalDate?,
    val end: LocalDate?
)

fun resolvePeriodRange(
    preset: PeriodPreset,
    activePlan: FinancialPlan?,
    customStart: LocalDate,
    customEnd: LocalDate,
    today: LocalDate = LocalDate.now()
): DatePeriodRange {
    return when (preset) {
        PeriodPreset.DAYS_7 -> DatePeriodRange(today.minusDays(6), today)
        PeriodPreset.DAYS_30 -> DatePeriodRange(today.minusDays(29), today)
        PeriodPreset.DAYS_90 -> DatePeriodRange(today.minusDays(89), today)
        PeriodPreset.ACTIVE_PLAN -> {
            if (activePlan != null) {
                DatePeriodRange(activePlan.startDate, activePlan.endDate)
            } else {
                DatePeriodRange(today.minusDays(29), today)
            }
        }
        PeriodPreset.ALL_TIME -> DatePeriodRange(null, null)
        PeriodPreset.CUSTOM -> {
            val minDate = if (customStart.isBefore(customEnd)) customStart else customEnd
            val maxDate = if (customEnd.isAfter(customStart)) customEnd else customStart
            DatePeriodRange(minDate, maxDate)
        }
    }
}
