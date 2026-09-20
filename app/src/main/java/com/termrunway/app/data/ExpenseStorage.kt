package com.termrunway.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ExpenseStorage(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    fun loadExpenses(): List<Expense> {
        if (!file.exists()) return emptyList()

        return runCatching {
            val json = JSONArray(file.readText())
            buildList {
                for (index in 0 until json.length()) {
                    val item = json.optJSONObject(index) ?: continue
                    val id = item.optString("id").ifBlank { continue }
                    val amountCents = item.optLong("amountCents", -1L)
                    val category = item.optString("category").trim()
                    val dateMillis = item.optLong("dateMillis", -1L)
                    val note = item.optString("note")

                    if (amountCents > 0L && category.isNotBlank() && dateMillis > 0L) {
                        add(
                            Expense(
                                id = id,
                                amountCents = amountCents,
                                category = category,
                                dateMillis = dateMillis,
                                note = note
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveExpenses(expenses: List<Expense>) {
        val json = JSONArray()

        expenses.forEach { expense ->
            json.put(
                JSONObject().apply {
                    put("id", expense.id)
                    put("amountCents", expense.amountCents)
                    put("category", expense.category)
                    put("dateMillis", expense.dateMillis)
                    put("note", expense.note)
                }
            )
        }

        file.writeText(json.toString())
    }

    private companion object {
        const val FILE_NAME = "expenses.json"
    }
}
