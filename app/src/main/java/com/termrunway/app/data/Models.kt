package com.termrunway.app.data

enum class TransactionType { INCOME, EXPENSE }

data class Transaction(
    val id: Long = 0L,
    val type: TransactionType,
    val amountPaise: Long,
    val category: String,
    val description: String,
    val dateMs: Long,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class FinancialPlan(
    val id: Long = 0L,
    val name: String,
    val startMs: Long,
    val endMs: Long,
    val startingMoneyPaise: Long = 0L,
    val createdAtMs: Long = System.currentTimeMillis()
)

data class PlannedIncome(
    val id: Long = 0L,
    val planId: Long,
    val source: String,
    val amountPaise: Long,
    val expectedDateMs: Long? = null
)

data class PlannedExpense(
    val id: Long = 0L,
    val planId: Long,
    val category: String,
    val amountPaise: Long,
    val expectedDateMs: Long? = null,
    val frequency: String = "once"
)

enum class CategoryType { INCOME, EXPENSE }

data class Category(
    val id: Long = 0L,
    val name: String,
    val type: CategoryType,
    val iconKey: String,
    val isDefault: Boolean = true
)

data class BackupSnapshot(
    val schemaVersion: Int,
    val name: String,
    val themeMode: String,
    val transactions: List<Transaction>,
    val plans: List<FinancialPlan>,
    val plannedIncomes: List<PlannedIncome>,
    val plannedExpenses: List<PlannedExpense>,
    val categories: List<Category>
)

data class PlanMetrics(
    val totalExpectedIncomePaise: Long,
    val expectedIncomeToDatePaise: Long = totalExpectedIncomePaise,
    val totalPlannedExpensePaise: Long,
    val actualIncomePaise: Long,
    val actualExpensePaise: Long,
    val expectedRemainingPaise: Long,
    val actualRemainingPaise: Long,
    val daysTotal: Int,
    val daysElapsed: Int,
    val daysRemaining: Int,
    val availablePerDayPaise: Long,
    val actualAverageDailySpendPaise: Long,
    val expectedSpendToDatePaise: Long,
    val spendVariancePaise: Long,
    val status: String,
    val guidance: String
) {
    val safeToSpendTodayPaise: Long get() = availablePerDayPaise.coerceAtLeast(0L)
}

object DefaultCategories {
    val income = listOf(
        Category(name = "Parents / Allowance", type = CategoryType.INCOME, iconKey = "family"),
        Category(name = "Scholarship", type = CategoryType.INCOME, iconKey = "school"),
        Category(name = "Part-time", type = CategoryType.INCOME, iconKey = "work"),
        Category(name = "Freelance", type = CategoryType.INCOME, iconKey = "laptop"),
        Category(name = "Gift", type = CategoryType.INCOME, iconKey = "gift"),
        Category(name = "Interest", type = CategoryType.INCOME, iconKey = "chart"),
        Category(name = "Other", type = CategoryType.INCOME, iconKey = "wallet")
    )

    val expense = listOf(
        Category(name = "Food", type = CategoryType.EXPENSE, iconKey = "food"),
        Category(name = "Transport", type = CategoryType.EXPENSE, iconKey = "transport"),
        Category(name = "Education", type = CategoryType.EXPENSE, iconKey = "education"),
        Category(name = "Bills", type = CategoryType.EXPENSE, iconKey = "bills"),
        Category(name = "Shopping", type = CategoryType.EXPENSE, iconKey = "shopping"),
        Category(name = "Entertainment", type = CategoryType.EXPENSE, iconKey = "entertainment"),
        Category(name = "Subscriptions", type = CategoryType.EXPENSE, iconKey = "subscriptions"),
        Category(name = "Personal", type = CategoryType.EXPENSE, iconKey = "personal"),
        Category(name = "Other", type = CategoryType.EXPENSE, iconKey = "other")
    )

    val all get() = income + expense
}
