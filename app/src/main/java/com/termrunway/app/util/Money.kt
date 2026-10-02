package com.termrunway.app.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun parseMoneyToCents(input: String): Long? {
    val value = input.trim()
    if (value.isBlank()) return null
    return runCatching {
        BigDecimal(value)
            .setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()?.takeIf { it > 0L }
}

fun formatRupees(cents: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.ENGLISH).apply {
        minimumFractionDigits = if (cents % 100L == 0L) 0 else 2
        maximumFractionDigits = 2
    }
    return "₹" + formatter.format(cents.toDouble() / 100.0)
}

fun formatSignedRupees(cents: Long): String =
    when {
        cents > 0L -> "+" + formatRupees(cents)
        cents < 0L -> "-" + formatRupees(-cents)
        else -> formatRupees(0L)
    }
