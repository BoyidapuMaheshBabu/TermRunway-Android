package com.termrunway.app.data

import android.content.Context
import org.json.JSONObject
import java.io.File

class SavingsGoalStorage(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    fun loadGoal(): SavingsGoal? {
        if (!file.exists()) return null

        return runCatching {
            val json = JSONObject(file.readText())
            val name = json.optString("name", "")
            val targetAmountCents = json.optLong("targetAmountCents", -1L)
            val savedAmountCents = json.optLong("savedAmountCents", -1L)

            if (name.isBlank() || targetAmountCents < 0 || savedAmountCents < 0) return@runCatching null

            SavingsGoal(
                name = name,
                targetAmountCents = targetAmountCents,
                savedAmountCents = savedAmountCents
            )
        }.getOrNull()
    }

    fun saveGoal(goal: SavingsGoal) {
        val json = JSONObject().apply {
            put("name", goal.name)
            put("targetAmountCents", goal.targetAmountCents)
            put("savedAmountCents", goal.savedAmountCents)
        }
        file.writeText(json.toString())
    }

    companion object {
        private const val FILE_NAME = "savings_goal.json"
    }
}
