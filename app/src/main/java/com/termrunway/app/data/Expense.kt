package com.termrunway.app.data

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val note: String = ""
)

object ExpenseAmount {
    fun parseToCents(input: String): Long? {
        val value = input.trim()
        if (value.isEmpty()) return null

        return runCatching {
            BigDecimal(value)
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        }.getOrNull()?.takeIf { it > 0L }
    }

    fun format(cents: Long): String =
        BigDecimal.valueOf(cents, 2).toPlainString()
}
