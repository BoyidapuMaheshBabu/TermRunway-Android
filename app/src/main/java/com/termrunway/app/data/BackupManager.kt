package com.termrunway.app.data

import android.content.ContentResolver
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object BackupManager {
    fun write(snapshot: BackupSnapshot, resolver: ContentResolver, uri: Uri) {
        val output = resolver.openOutputStream(uri) ?: throw IOException("Unable to open backup destination")
        output.use { it.write(toJson(snapshot).toString(2).toByteArray(Charsets.UTF_8)) }
    }

    fun read(resolver: ContentResolver, uri: Uri): BackupSnapshot {
        val input = resolver.openInputStream(uri) ?: throw IOException("Unable to open backup file")
        val text = input.bufferedReader(Charsets.UTF_8).use { it.readText() }
        return fromJsonString(text)
    }

    fun fromJsonString(jsonText: String): BackupSnapshot {
        return fromJson(JSONObject(jsonText))
    }

    private fun toJson(snapshot: BackupSnapshot): JSONObject {
        return JSONObject()
            .put("schemaVersion", snapshot.schemaVersion)
            .put("app", "TermRunway")
            .put("name", snapshot.name)
            .put("themeMode", snapshot.themeMode)
            .put("transactions", JSONArray().apply {
                snapshot.transactions.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("type", item.type.name)
                        .put("amountPaise", item.amountPaise)
                        .put("category", item.category)
                        .put("description", item.description)
                        .put("dateMs", item.dateMs)
                        .put("createdAtMs", item.createdAtMs))
                }
            })
            .put("plans", JSONArray().apply {
                snapshot.plans.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("name", item.name)
                        .put("startMs", item.startMs)
                        .put("endMs", item.endMs)
                        .put("startingMoneyPaise", item.startingMoneyPaise)
                        .put("createdAtMs", item.createdAtMs))
                }
            })
            .put("plannedIncomes", JSONArray().apply {
                snapshot.plannedIncomes.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("planId", item.planId)
                        .put("source", item.source)
                        .put("amountPaise", item.amountPaise)
                        .put("expectedDateMs", item.expectedDateMs ?: JSONObject.NULL))
                }
            })
            .put("plannedExpenses", JSONArray().apply {
                snapshot.plannedExpenses.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("planId", item.planId)
                        .put("category", item.category)
                        .put("amountPaise", item.amountPaise)
                        .put("expectedDateMs", item.expectedDateMs ?: JSONObject.NULL)
                        .put("frequency", item.frequency))
                }
            })
            .put("categories", JSONArray().apply {
                snapshot.categories.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("name", item.name)
                        .put("type", item.type.name)
                        .put("iconKey", item.iconKey)
                        .put("isDefault", item.isDefault))
                }
            })
    }

    private fun fromJson(root: JSONObject): BackupSnapshot {
        require(root.optString("app") == "TermRunway") { "This file is not a TermRunway backup" }
        val version = root.optInt("schemaVersion", -1)
        require(version == 1) { "Unsupported backup version: $version" }

        val transactions = mutableListOf<Transaction>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val o = txArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid transaction amount" }
            transactions += Transaction(
                id = o.optLong("id"),
                type = TransactionType.valueOf(o.getString("type")),
                amountPaise = amount,
                category = o.getString("category").trim().also { require(it.isNotEmpty()) },
                description = o.optString("description"),
                dateMs = o.getLong("dateMs"),
                createdAtMs = o.optLong("createdAtMs", System.currentTimeMillis())
            )
        }

        val plans = mutableListOf<FinancialPlan>()
        val planArray = root.optJSONArray("plans") ?: JSONArray()
        for (i in 0 until planArray.length()) {
            val o = planArray.getJSONObject(i)
            require(o.getLong("endMs") >= o.getLong("startMs")) { "Invalid plan dates" }
            plans += FinancialPlan(
                id = o.optLong("id"),
                name = o.getString("name").trim().also { require(it.isNotEmpty()) },
                startMs = o.getLong("startMs"),
                endMs = o.getLong("endMs"),
                startingMoneyPaise = o.optLong("startingMoneyPaise"),
                createdAtMs = o.optLong("createdAtMs", System.currentTimeMillis())
            )
        }

        val incomes = mutableListOf<PlannedIncome>()
        val incomeArray = root.optJSONArray("plannedIncomes") ?: JSONArray()
        for (i in 0 until incomeArray.length()) {
            val o = incomeArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid planned income amount" }
            incomes += PlannedIncome(
                id = o.optLong("id"),
                planId = o.getLong("planId"),
                source = o.getString("source").trim().also { require(it.isNotEmpty()) },
                amountPaise = amount,
                expectedDateMs = if (o.isNull("expectedDateMs")) null else o.getLong("expectedDateMs")
            )
        }

        val expenses = mutableListOf<PlannedExpense>()
        val expenseArray = root.optJSONArray("plannedExpenses") ?: JSONArray()
        for (i in 0 until expenseArray.length()) {
            val o = expenseArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid planned expense amount" }
            expenses += PlannedExpense(
                id = o.optLong("id"),
                planId = o.getLong("planId"),
                category = o.getString("category").trim().also { require(it.isNotEmpty()) },
                amountPaise = amount,
                expectedDateMs = if (o.isNull("expectedDateMs")) null else o.getLong("expectedDateMs"),
                frequency = o.optString("frequency", "once")
            )
        }

        val categories = mutableListOf<Category>()
        val categoryArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoryArray.length()) {
            val o = categoryArray.getJSONObject(i)
            categories += Category(
                id = o.optLong("id"),
                name = o.getString("name").trim().also { require(it.isNotEmpty()) },
                type = CategoryType.valueOf(o.getString("type")),
                iconKey = o.optString("iconKey", "custom"),
                isDefault = o.optBoolean("isDefault", false)
            )
        }

        val name = root.optString("name").trim()
        require(name.length <= 60) { "Invalid user name" }

        return BackupSnapshot(
            schemaVersion = version,
            name = name,
            themeMode = root.optString("themeMode", "system"),
            transactions = transactions,
            plans = plans,
            plannedIncomes = incomes,
            plannedExpenses = expenses,
            categories = categories.ifEmpty { DefaultCategories.all }
        )
    }
}
