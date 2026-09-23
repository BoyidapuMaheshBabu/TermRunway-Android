package com.termrunway.app.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class BackupPreview(
    val username: String,
    val expenseCount: Int,
    val incomeCount: Int,
    val createdAtMillis: Long
)

class BackupManager(
    private val context: Context,
    private val profile: UserProfileStorage,
    private val expenses: ExpenseStorage,
    private val incomes: IncomeStorage
) {
    fun exportJson(): String {
        val root = JSONObject()
            .put("format", "termrunway-backup")
            .put("version", 1)
            .put("createdAtMillis", System.currentTimeMillis())
            .put("profile", JSONObject().put("username", profile.username()))
            .put(
                "preferences",
                JSONObject()
                    .put("dailyLimitCents", profile.dailyLimitCents())
                    .put("theme", profile.theme())
            )

        val expenseArray = JSONArray()
        expenses.loadExpenses().forEach { expense ->
            expenseArray.put(
                JSONObject()
                    .put("id", expense.id)
                    .put("amountCents", expense.amountCents)
                    .put("category", expense.category)
                    .put("dateMillis", expense.dateMillis)
                    .put("note", expense.note)
            )
        }

        val incomeArray = JSONArray()
        incomes.loadIncomes().forEach { income ->
            incomeArray.put(
                JSONObject()
                    .put("id", income.id)
                    .put("amountCents", income.amountCents)
                    .put("source", income.source)
                    .put("dateMillis", income.dateMillis)
                    .put("note", income.note)
            )
        }

        root.put("expenses", expenseArray)
        root.put("incomes", incomeArray)
        return root.toString(2)
    }

    fun preview(uri: Uri): BackupPreview? = runCatching {
        val root = readRoot(uri)
        require(root.optString("format") == "termrunway-backup")
        require(root.optInt("version", -1) == 1)
        BackupPreview(
            username = root.optJSONObject("profile")?.optString("username").orEmpty(),
            expenseCount = root.optJSONArray("expenses")?.length() ?: 0,
            incomeCount = root.optJSONArray("incomes")?.length() ?: 0,
            createdAtMillis = root.optLong("createdAtMillis", 0L)
        )
    }.getOrNull()

    fun restore(uri: Uri) {
        val root = readRoot(uri)
        require(root.optString("format") == "termrunway-backup") { "Not a TermRunway backup." }
        require(root.optInt("version", -1) == 1) { "Unsupported backup version." }

        val profileObject = root.optJSONObject("profile")
        profile.setUsername(profileObject?.optString("username").orEmpty())

        val preferences = root.optJSONObject("preferences")
        profile.setDailyLimitCents(preferences?.optLong("dailyLimitCents", 0L) ?: 0L)
        profile.setTheme(preferences?.optString("theme", "SYSTEM") ?: "SYSTEM")

        val expenseList = mutableListOf<Expense>()
        val expenseArray = root.optJSONArray("expenses") ?: JSONArray()
        for (index in 0 until expenseArray.length()) {
            val item = expenseArray.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val amount = item.optLong("amountCents", -1L)
            val category = item.optString("category").trim()
            val date = item.optLong("dateMillis", -1L)
            if (id.isNotBlank() && amount > 0 && category.isNotBlank() && date > 0) {
                expenseList += Expense(id, amount, category, date, item.optString("note"))
            }
        }

        val incomeList = mutableListOf<Income>()
        val incomeArray = root.optJSONArray("incomes") ?: JSONArray()
        for (index in 0 until incomeArray.length()) {
            val item = incomeArray.optJSONObject(index) ?: continue
            val id = item.optString("id")
            val amount = item.optLong("amountCents", -1L)
            val source = item.optString("source").trim()
            val date = item.optLong("dateMillis", -1L)
            if (id.isNotBlank() && amount > 0 && source.isNotBlank() && date > 0) {
                incomeList += Income(id, amount, source, date, item.optString("note"))
            }
        }

        expenses.saveExpenses(expenseList)
        incomes.saveIncomes(incomeList)
    }

    fun clearAllData() {
        expenses.saveExpenses(emptyList())
        incomes.saveIncomes(emptyList())
        profile.clear()
    }

    private fun readRoot(uri: Uri): JSONObject {
        val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: error("Could not read backup file.")
        return JSONObject(text)
    }
}
