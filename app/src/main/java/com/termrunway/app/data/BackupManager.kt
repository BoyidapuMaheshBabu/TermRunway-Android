package com.termrunway.app.data

import android.content.ContentResolver
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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

    fun toJson(snapshot: BackupSnapshot): JSONObject {
        return JSONObject()
            .put("schemaVersion", 2)
            .put("app", "TermRunway")
            .put("name", snapshot.name)
            .put("themeMode", snapshot.themeMode)
            .put("trackingMode", snapshot.trackingMode)
            .put("notificationsEnabled", snapshot.notificationsEnabled)
            .put("dailyReminderEnabled", snapshot.dailyReminderEnabled)
            .put("weeklyReviewEnabled", snapshot.weeklyReviewEnabled)
            .put("monthlyReviewEnabled", snapshot.monthlyReviewEnabled)
            .put("planEndingEnabled", snapshot.planEndingEnabled)
            .put("reminderHour", snapshot.reminderHour)
            .put("reminderMinute", snapshot.reminderMinute)
            .put("transactions", JSONArray().apply {
                snapshot.transactions.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("type", item.type.name)
                        .put("amountPaise", item.amountPaise)
                        .put("category", item.category)
                        .put("description", item.description)
                        .put("date", item.date.toString())
                        .put("createdAtMs", item.createdAtMs))
                }
            })
            .put("plans", JSONArray().apply {
                snapshot.plans.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("name", item.name)
                        .put("startDate", item.startDate.toString())
                        .put("endDate", item.endDate.toString())
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
                        .put("expectedDate", item.expectedDate?.toString() ?: JSONObject.NULL))
                }
            })
            .put("plannedExpenses", JSONArray().apply {
                snapshot.plannedExpenses.forEach { item ->
                    put(JSONObject()
                        .put("id", item.id)
                        .put("planId", item.planId)
                        .put("category", item.category)
                        .put("amountPaise", item.amountPaise)
                        .put("expectedDate", item.expectedDate?.toString() ?: JSONObject.NULL)
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
        require(version == 1 || version == 2) { "Unsupported backup version: $version" }

        val name = root.optString("name").trim()
        require(name.length <= 60) { "Invalid user name" }

        val zone = ZoneId.systemDefault()

        val plans = mutableListOf<FinancialPlan>()
        val planIds = mutableSetOf<Long>()
        val planArray = root.optJSONArray("plans") ?: JSONArray()
        for (i in 0 until planArray.length()) {
            val o = planArray.getJSONObject(i)
            val id = o.optLong("id")
            val planName = o.getString("name").trim().also { require(it.isNotEmpty()) { "Plan name cannot be empty" } }

            val startDate = parseLocalDate(o, "startDate", "startMs", zone)
                ?: throw IllegalArgumentException("Missing plan start date")
            val endDate = parseLocalDate(o, "endDate", "endMs", zone)
                ?: throw IllegalArgumentException("Missing plan end date")

            require(!endDate.isBefore(startDate)) { "Invalid plan dates: end date must be on or after start date" }
            plans += FinancialPlan(
                id = id,
                name = planName,
                startDate = startDate,
                endDate = endDate,
                startingMoneyPaise = o.optLong("startingMoneyPaise"),
                createdAtMs = o.optLong("createdAtMs", System.currentTimeMillis())
            )
            if (id != 0L) {
                planIds.add(id)
            }
        }

        val transactions = mutableListOf<Transaction>()
        val txArray = root.optJSONArray("transactions") ?: JSONArray()
        for (i in 0 until txArray.length()) {
            val o = txArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid transaction amount" }
            val typeStr = o.getString("type")
            val type = runCatching { TransactionType.valueOf(typeStr) }.getOrElse {
                throw IllegalArgumentException("Invalid transaction type: $typeStr")
            }
            val category = o.getString("category").trim()
            require(category.isNotEmpty()) { "Transaction category cannot be empty" }

            val txDate = parseLocalDate(o, "date", "dateMs", zone)
                ?: throw IllegalArgumentException("Missing transaction date")

            transactions += Transaction(
                id = o.optLong("id"),
                type = type,
                amountPaise = amount,
                category = category,
                description = o.optString("description"),
                date = txDate,
                createdAtMs = o.optLong("createdAtMs", System.currentTimeMillis())
            )
        }

        val incomes = mutableListOf<PlannedIncome>()
        val incomeArray = root.optJSONArray("plannedIncomes") ?: JSONArray()
        for (i in 0 until incomeArray.length()) {
            val o = incomeArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid planned income amount" }
            val planId = o.getLong("planId")
            require(planIds.isEmpty() || planIds.contains(planId)) {
                "Backup contains planned income referencing non-existent plan ID: $planId"
            }
            val source = o.getString("source").trim()
            require(source.isNotEmpty()) { "Planned income source cannot be empty" }

            val expDate = parseLocalDate(o, "expectedDate", "expectedDateMs", zone)

            incomes += PlannedIncome(
                id = o.optLong("id"),
                planId = planId,
                source = source,
                amountPaise = amount,
                expectedDate = expDate
            )
        }

        val expenses = mutableListOf<PlannedExpense>()
        val expenseArray = root.optJSONArray("plannedExpenses") ?: JSONArray()
        for (i in 0 until expenseArray.length()) {
            val o = expenseArray.getJSONObject(i)
            val amount = o.getLong("amountPaise")
            require(amount >= 0) { "Invalid planned expense amount" }
            val planId = o.getLong("planId")
            require(planIds.isEmpty() || planIds.contains(planId)) {
                "Backup contains planned expense referencing non-existent plan ID: $planId"
            }
            val category = o.getString("category").trim()
            require(category.isNotEmpty()) { "Planned expense category cannot be empty" }

            val expDate = parseLocalDate(o, "expectedDate", "expectedDateMs", zone)

            expenses += PlannedExpense(
                id = o.optLong("id"),
                planId = planId,
                category = category,
                amountPaise = amount,
                expectedDate = expDate,
                frequency = o.optString("frequency", "once")
            )
        }

        val categories = mutableListOf<Category>()
        val categoryArray = root.optJSONArray("categories") ?: JSONArray()
        for (i in 0 until categoryArray.length()) {
            val o = categoryArray.getJSONObject(i)
            val catName = o.getString("name").trim()
            require(catName.isNotEmpty()) { "Category name cannot be empty" }
            val typeStr = o.getString("type")
            val type = runCatching { CategoryType.valueOf(typeStr) }.getOrElse {
                throw IllegalArgumentException("Invalid category type: $typeStr")
            }
            categories += Category(
                id = o.optLong("id"),
                name = catName,
                type = type,
                iconKey = o.optString("iconKey", "custom"),
                isDefault = o.optBoolean("isDefault", false)
            )
        }

        return BackupSnapshot(
            schemaVersion = version,
            name = name,
            themeMode = root.optString("themeMode", "system"),
            trackingMode = root.optString("trackingMode", "daily"),
            notificationsEnabled = root.optBoolean("notificationsEnabled", true),
            dailyReminderEnabled = root.optBoolean("dailyReminderEnabled", true),
            weeklyReviewEnabled = root.optBoolean("weeklyReviewEnabled", true),
            monthlyReviewEnabled = root.optBoolean("monthlyReviewEnabled", true),
            planEndingEnabled = root.optBoolean("planEndingEnabled", true),
            reminderHour = root.optInt("reminderHour", 20),
            reminderMinute = root.optInt("reminderMinute", 0),
            transactions = transactions,
            plans = plans,
            plannedIncomes = incomes,
            plannedExpenses = expenses,
            categories = categories.ifEmpty { DefaultCategories.all }
        )
    }

    private fun parseLocalDate(
        obj: JSONObject,
        strKey: String,
        msKey: String,
        zone: ZoneId
    ): LocalDate? {
        if (!obj.isNull(strKey)) {
            val str = obj.optString(strKey)
            if (str.isNotBlank()) {
                return runCatching { LocalDate.parse(str) }.getOrNull()
            }
        }
        if (!obj.isNull(msKey)) {
            val ms = obj.optLong(msKey)
            return Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
        }
        return null
    }
}
