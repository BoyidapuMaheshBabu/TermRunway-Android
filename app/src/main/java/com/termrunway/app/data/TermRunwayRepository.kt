package com.termrunway.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class TermRunwayRepository(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    @Synchronized
    fun load(): AppData {
        if (!file.exists()) return AppData()
        return runCatching { decode(JSONObject(file.readText())).first }.getOrDefault(AppData())
    }

    @Synchronized
    fun save(data: AppData) {
        validate(data)
        val temp = File(file.parentFile, "$FILE_NAME.tmp")
        temp.writeText(encode(data).toString())
        if (file.exists() && !file.delete()) {
            temp.delete()
            error("Could not replace local finance data.")
        }
        if (!temp.renameTo(file)) {
            temp.inputStream().use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            temp.delete()
        }
    }

    fun exportJson(data: AppData): String {
        validate(data)
        return encode(data).toString(2)
    }

    fun previewBackup(jsonText: String): BackupPreview {
        val root = JSONObject(jsonText)
        val appData = decode(root).first
        return BackupPreview(
            username = appData.username,
            expenseCount = appData.expenses.size,
            incomeCount = appData.incomes.size,
            createdAtMillis = root.optLong("createdAtMillis", 0L)
        )
    }

    fun parseBackup(jsonText: String): AppData = decode(JSONObject(jsonText)).first

    private fun encode(data: AppData): JSONObject {
        val expenses = JSONArray()
        data.expenses.forEach { expense ->
            expenses.put(
                JSONObject()
                    .put("id", expense.id)
                    .put("amountCents", expense.amountCents)
                    .put("category", expense.category)
                    .put("dateMillis", expense.dateMillis)
                    .put("note", expense.note)
            )
        }

        val incomes = JSONArray()
        data.incomes.forEach { income ->
            incomes.put(
                JSONObject()
                    .put("id", income.id)
                    .put("amountCents", income.amountCents)
                    .put("source", income.source)
                    .put("dateMillis", income.dateMillis)
                    .put("note", income.note)
            )
        }

        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("createdAtMillis", System.currentTimeMillis())
            .put(
                "profile",
                JSONObject()
                    .put("username", data.username)
                    .put("dailyLimitCents", data.dailyLimitCents)
                    .put("theme", data.theme.name)
            )
            .put("expenses", expenses)
            .put("incomes", incomes)
    }

    private fun decode(root: JSONObject): Pair<AppData, Long> {
        require(root.optString("format") == FORMAT) { "Not a TermRunway backup." }
        require(root.optInt("version", -1) == VERSION) { "Unsupported TermRunway backup version." }

        val profile = root.optJSONObject("profile") ?: JSONObject()
        val username = profile.optString("username").trim().take(MAX_USERNAME_LENGTH)
        val dailyLimitCents = profile.optLong("dailyLimitCents", 0L)
        require(dailyLimitCents >= 0L) { "Invalid daily limit." }

        val theme = runCatching {
            ThemeMode.valueOf(profile.optString("theme", ThemeMode.SYSTEM.name))
        }.getOrElse { ThemeMode.SYSTEM }

        val data = AppData(
            username = username,
            dailyLimitCents = dailyLimitCents,
            theme = theme,
            expenses = decodeExpenses(root.optJSONArray("expenses") ?: JSONArray()),
            incomes = decodeIncomes(root.optJSONArray("incomes") ?: JSONArray())
        )
        validate(data)
        return data to root.optLong("createdAtMillis", 0L)
    }

    private fun decodeExpenses(array: JSONArray): List<Expense> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Expense>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").trim().ifBlank { UUID.randomUUID().toString() }
            val amount = item.optLong("amountCents", -1L)
            val category = item.optString("category").trim()
            val date = item.optLong("dateMillis", -1L)
            val note = item.optString("note").trim().take(MAX_NOTE_LENGTH)
            if (amount > 0L && category.isNotBlank() && date > 0L && seen.add(id)) {
                result += Expense(id, amount, category, date, note)
            }
        }
        return result
    }

    private fun decodeIncomes(array: JSONArray): List<Income> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Income>()
        for (index in 0 until array.length()) {
            val item = array.optJSONObject(index) ?: continue
            val id = item.optString("id").trim().ifBlank { UUID.randomUUID().toString() }
            val amount = item.optLong("amountCents", -1L)
            val source = item.optString("source").trim()
            val date = item.optLong("dateMillis", -1L)
            val note = item.optString("note").trim().take(MAX_NOTE_LENGTH)
            if (amount > 0L && source.isNotBlank() && date > 0L && seen.add(id)) {
                result += Income(id, amount, source, date, note)
            }
        }
        return result
    }

    private fun validate(data: AppData) {
        require(data.dailyLimitCents >= 0L)
        require(data.username.length <= MAX_USERNAME_LENGTH)
        require(data.expenses.all { it.amountCents > 0L && it.category.isNotBlank() && it.dateMillis > 0L && it.note.length <= MAX_NOTE_LENGTH })
        require(data.incomes.all { it.amountCents > 0L && it.source.isNotBlank() && it.dateMillis > 0L && it.note.length <= MAX_NOTE_LENGTH })
        require(data.expenses.map { it.id }.distinct().size == data.expenses.size)
        require(data.incomes.map { it.id }.distinct().size == data.incomes.size)
    }

    private companion object {
        const val FILE_NAME = "termrunway_data.json"
        const val FORMAT = "termrunway-backup"
        const val VERSION = 1
        const val MAX_USERNAME_LENGTH = 32
        const val MAX_NOTE_LENGTH = 120
    }
}
