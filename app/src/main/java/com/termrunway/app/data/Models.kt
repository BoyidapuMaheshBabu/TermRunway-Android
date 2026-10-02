package com.termrunway.app.data

import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amountCents: Long,
    val category: String,
    val dateMillis: Long,
    val note: String = ""
)

data class Income(
    val id: String = UUID.randomUUID().toString(),
    val amountCents: Long,
    val source: String,
    val dateMillis: Long,
    val note: String = ""
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppData(
    val username: String = "",
    val dailyLimitCents: Long = 0L,
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val expenses: List<Expense> = emptyList(),
    val incomes: List<Income> = emptyList()
)

object ExpenseCategories {
    val ALL = listOf(
        "Tuition & Fees",
        "Rent & Housing",
        "Food & Groceries",
        "Utilities & Internet",
        "Transport & Travel",
        "Books & Academic Supplies",
        "Personal & Lifestyle",
        "Miscellaneous / Contingency"
    )
}

data class BackupPreview(
    val username: String,
    val expenseCount: Int,
    val incomeCount: Int,
    val createdAtMillis: Long
)
