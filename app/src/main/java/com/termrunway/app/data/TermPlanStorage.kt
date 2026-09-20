package com.termrunway.app.data

import android.content.Context
import org.json.JSONObject
import java.io.File

class TermPlanStorage(context: Context) {
    private val file = File(context.filesDir, FILE_NAME)

    fun loadPlan(): TermPlan? {
        if (!file.exists()) return null

        return runCatching {
            val json = JSONObject(file.readText())
            val startDateMillis = json.optLong("startDateMillis", -1L)
            val endDateMillis = json.optLong("endDateMillis", -1L)
            val startingFundsCents = json.optLong("startingFundsCents", -1L)
            val expectedIncomeCents = json.optLong("expectedIncomeCents", 0L)
            val planningMode = json.optString("planningMode", "Semester")
            
            if (startDateMillis < 0 || endDateMillis < 0 || startingFundsCents < 0) return@runCatching null

            val plannedExpensesObj = json.optJSONObject("plannedExpenses") ?: JSONObject()
            val plannedExpenses = mutableMapOf<String, Long>()
            plannedExpensesObj.keys().forEach { key ->
                plannedExpenses[key] = plannedExpensesObj.optLong(key, 0L)
            }

            TermPlan(
                startDateMillis = startDateMillis,
                endDateMillis = endDateMillis,
                startingFundsCents = startingFundsCents,
                expectedIncomeCents = expectedIncomeCents,
                planningMode = planningMode,
                plannedExpenses = plannedExpenses
            )
        }.getOrNull()
    }

    fun savePlan(plan: TermPlan) {
        val json = JSONObject().apply {
            put("startDateMillis", plan.startDateMillis)
            put("endDateMillis", plan.endDateMillis)
            put("startingFundsCents", plan.startingFundsCents)
            put("expectedIncomeCents", plan.expectedIncomeCents)
            put("planningMode", plan.planningMode)
            
            val expensesJson = JSONObject()
            plan.plannedExpenses.forEach { (category, amount) ->
                expensesJson.put(category, amount)
            }
            put("plannedExpenses", expensesJson)
        }
        file.writeText(json.toString())
    }

    companion object {
        private const val FILE_NAME = "term_plan.json"
    }
}
