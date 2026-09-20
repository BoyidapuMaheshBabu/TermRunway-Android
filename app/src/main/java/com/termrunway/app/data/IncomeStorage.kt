package com.termrunway.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class IncomeStorage(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    fun loadIncomes(): List<Income> {
        if (!file.exists()) return emptyList()

        return runCatching {
            val json = JSONArray(file.readText())
            buildList {
                for (index in 0 until json.length()) {
                    val item = json.optJSONObject(index) ?: continue
                    val id = item.optString("id")
                    if (id.isBlank()) continue

                    val amountCents = item.optLong("amountCents", -1L)
                    val source = item.optString("source").trim()
                    val dateMillis = item.optLong("dateMillis", -1L)
                    val note = item.optString("note")

                    if (amountCents > 0L && source.isNotBlank() && dateMillis > 0L) {
                        add(
                            Income(
                                id = id,
                                amountCents = amountCents,
                                source = source,
                                dateMillis = dateMillis,
                                note = note
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    fun saveIncomes(incomes: List<Income>) {
        val json = JSONArray()

        incomes.forEach { income ->
            json.put(
                JSONObject().apply {
                    put("id", income.id)
                    put("amountCents", income.amountCents)
                    put("source", income.source)
                    put("dateMillis", income.dateMillis)
                    put("note", income.note)
                }
            )
        }

        file.writeText(json.toString())
    }

    private companion object {
        const val FILE_NAME = "incomes.json"
    }
}
