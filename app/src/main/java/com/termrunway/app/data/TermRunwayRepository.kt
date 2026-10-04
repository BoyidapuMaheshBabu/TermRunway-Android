package com.termrunway.app.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TermRunwayRepository(context: Context) {
    private val database = TermRunwayDatabase(context.applicationContext)

    suspend fun listTransactions(): List<Transaction> = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT * FROM transactions ORDER BY date_ms DESC, id DESC", null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toTransaction())
            }
        }
    }

    suspend fun insertTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        TermRunwayDatabase.insertTransaction(database.writableDatabase, transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) = withContext(Dispatchers.IO) {
        TermRunwayDatabase.updateTransaction(database.writableDatabase, transaction)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        database.writableDatabase.delete("transactions", "id=?", arrayOf(id.toString()))
    }

    suspend fun listPlans(): List<FinancialPlan> = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT * FROM plans ORDER BY start_ms DESC, id DESC", null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toPlan())
            }
        }
    }

    suspend fun createPlan(
        plan: FinancialPlan,
        plannedIncome: List<PlannedIncome>,
        plannedExpenses: List<PlannedExpense>
    ): Long = withContext(Dispatchers.IO) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            val planId = db.insertOrThrow("plans", null, planValues(plan))
            plannedIncome.forEach {
                db.insertOrThrow("planned_income", null, incomeValues(it.copy(planId = planId)))
            }
            plannedExpenses.forEach {
                db.insertOrThrow("planned_expense", null, expenseValues(it.copy(planId = planId)))
            }
            db.setTransactionSuccessful()
            planId
        } finally {
            db.endTransaction()
        }
    }

    suspend fun replacePlan(
        plan: FinancialPlan,
        plannedIncome: List<PlannedIncome>,
        plannedExpenses: List<PlannedExpense>
    ) = withContext(Dispatchers.IO) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.update("plans", planValues(plan), "id=?", arrayOf(plan.id.toString()))
            db.delete("planned_income", "plan_id=?", arrayOf(plan.id.toString()))
            db.delete("planned_expense", "plan_id=?", arrayOf(plan.id.toString()))
            plannedIncome.forEach {
                db.insertOrThrow("planned_income", null, incomeValues(it.copy(id = 0, planId = plan.id)))
            }
            plannedExpenses.forEach {
                db.insertOrThrow("planned_expense", null, expenseValues(it.copy(id = 0, planId = plan.id)))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun deletePlan(planId: Long) = withContext(Dispatchers.IO) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("planned_income", "plan_id=?", arrayOf(planId.toString()))
            db.delete("planned_expense", "plan_id=?", arrayOf(planId.toString()))
            db.delete("plans", "id=?", arrayOf(planId.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun planIncome(planId: Long): List<PlannedIncome> = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT * FROM planned_income WHERE plan_id=? ORDER BY COALESCE(expected_date_ms, 0), id",
            arrayOf(planId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toPlannedIncome())
            }
        }
    }

    suspend fun planExpenses(planId: Long): List<PlannedExpense> = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT * FROM planned_expense WHERE plan_id=? ORDER BY COALESCE(expected_date_ms, 0), id",
            arrayOf(planId.toString())
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toPlannedExpense())
            }
        }
    }

    suspend fun listCategories(): List<Category> = withContext(Dispatchers.IO) {
        database.readableDatabase.rawQuery(
            "SELECT * FROM categories ORDER BY type, is_default DESC, name COLLATE NOCASE", null
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.toCategory())
            }
        }
    }

    suspend fun addCategory(name: String, type: CategoryType) = withContext(Dispatchers.IO) {
        val clean = name.trim().take(32)
        require(clean.length in 2..32) { "Category name must be between 2 and 32 characters." }
        val values = ContentValues().apply {
            put("name", clean)
            put("type", type.name)
            put("icon_key", "custom")
            put("is_default", 0)
        }
        val result = database.writableDatabase.insertWithOnConflict(
            "categories",
            null,
            values,
            android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE
        )
        require(result != -1L) { "A category named '$clean' already exists for ${type.name.lowercase()}s." }
    }

    suspend fun deleteCustomCategory(id: Long) = withContext(Dispatchers.IO) {
        database.writableDatabase.delete(
            "categories",
            "id=? AND is_default=0",
            arrayOf(id.toString())
        )
    }

    suspend fun snapshot(name: String, themeMode: String): BackupSnapshot = withContext(Dispatchers.IO) {
        val plans = listPlans()
        BackupSnapshot(
            schemaVersion = 1,
            name = name,
            themeMode = themeMode,
            transactions = listTransactions(),
            plans = plans,
            plannedIncomes = plans.flatMap { planIncome(it.id) },
            plannedExpenses = plans.flatMap { planExpenses(it.id) },
            categories = listCategories()
        )
    }

    suspend fun restore(snapshot: BackupSnapshot) = withContext(Dispatchers.IO) {
        require(snapshot.schemaVersion == 1) { "Unsupported backup version" }
        require(snapshot.name.length <= 40) { "Invalid user name" }
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("planned_income", null, null)
            db.delete("planned_expense", null, null)
            db.delete("transactions", null, null)
            db.delete("plans", null, null)
            db.delete("categories", null, null)

            snapshot.categories.forEach { category ->
                val values = ContentValues().apply {
                    put("name", category.name)
                    put("type", category.type.name)
                    put("icon_key", category.iconKey)
                    put("is_default", if (category.isDefault) 1 else 0)
                }
                db.insertWithOnConflict("categories", null, values, android.database.sqlite.SQLiteDatabase.CONFLICT_IGNORE)
            }

            snapshot.transactions.forEach { TermRunwayDatabase.insertTransaction(db, it) }

            val planIdMap = mutableMapOf<Long, Long>()
            snapshot.plans.forEach { plan ->
                val newId = db.insertOrThrow("plans", null, planValues(plan))
                planIdMap[plan.id] = newId
            }
            snapshot.plannedIncomes.forEach { item ->
                val newPlanId = planIdMap[item.planId] ?: return@forEach
                db.insertOrThrow(
                    "planned_income",
                    null,
                    incomeValues(item.copy(id = 0, planId = newPlanId))
                )
            }
            snapshot.plannedExpenses.forEach { item ->
                val newPlanId = planIdMap[item.planId] ?: return@forEach
                db.insertOrThrow(
                    "planned_expense",
                    null,
                    expenseValues(item.copy(id = 0, planId = newPlanId))
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        val db = database.writableDatabase
        db.beginTransaction()
        try {
            db.delete("planned_income", null, null)
            db.delete("planned_expense", null, null)
            db.delete("transactions", null, null)
            db.delete("plans", null, null)
            db.delete("categories", null, null)
            seedCategories(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun seedCategories(db: android.database.sqlite.SQLiteDatabase) {
        DefaultCategories.all.forEach { category ->
            db.insert("categories", null, ContentValues().apply {
                put("name", category.name)
                put("type", category.type.name)
                put("icon_key", category.iconKey)
                put("is_default", 1)
            })
        }
    }

    private fun planValues(plan: FinancialPlan) = ContentValues().apply {
        put("name", plan.name)
        put("start_ms", plan.startMs)
        put("end_ms", plan.endMs)
        put("starting_money_paise", plan.startingMoneyPaise)
        put("created_at_ms", plan.createdAtMs)
    }

    private fun incomeValues(item: PlannedIncome) = ContentValues().apply {
        put("plan_id", item.planId)
        put("source", item.source)
        put("amount_paise", item.amountPaise)
        if (item.expectedDateMs == null) putNull("expected_date_ms") else put("expected_date_ms", item.expectedDateMs)
    }

    private fun expenseValues(item: PlannedExpense) = ContentValues().apply {
        put("plan_id", item.planId)
        put("category", item.category)
        put("amount_paise", item.amountPaise)
        if (item.expectedDateMs == null) putNull("expected_date_ms") else put("expected_date_ms", item.expectedDateMs)
        put("frequency", item.frequency)
    }

    private fun Cursor.toTransaction() = Transaction(
        id = getLong(getColumnIndexOrThrow("id")),
        type = TransactionType.valueOf(getString(getColumnIndexOrThrow("type"))),
        amountPaise = getLong(getColumnIndexOrThrow("amount_paise")),
        category = getString(getColumnIndexOrThrow("category")),
        description = getString(getColumnIndexOrThrow("description")),
        dateMs = getLong(getColumnIndexOrThrow("date_ms")),
        createdAtMs = getLong(getColumnIndexOrThrow("created_at_ms"))
    )

    private fun Cursor.toPlan() = FinancialPlan(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        startMs = getLong(getColumnIndexOrThrow("start_ms")),
        endMs = getLong(getColumnIndexOrThrow("end_ms")),
        startingMoneyPaise = getLong(getColumnIndexOrThrow("starting_money_paise")),
        createdAtMs = getLong(getColumnIndexOrThrow("created_at_ms"))
    )

    private fun Cursor.toPlannedIncome() = PlannedIncome(
        id = getLong(getColumnIndexOrThrow("id")),
        planId = getLong(getColumnIndexOrThrow("plan_id")),
        source = getString(getColumnIndexOrThrow("source")),
        amountPaise = getLong(getColumnIndexOrThrow("amount_paise")),
        expectedDateMs = if (isNull(getColumnIndexOrThrow("expected_date_ms"))) null else getLong(getColumnIndexOrThrow("expected_date_ms"))
    )

    private fun Cursor.toPlannedExpense() = PlannedExpense(
        id = getLong(getColumnIndexOrThrow("id")),
        planId = getLong(getColumnIndexOrThrow("plan_id")),
        category = getString(getColumnIndexOrThrow("category")),
        amountPaise = getLong(getColumnIndexOrThrow("amount_paise")),
        expectedDateMs = if (isNull(getColumnIndexOrThrow("expected_date_ms"))) null else getLong(getColumnIndexOrThrow("expected_date_ms")),
        frequency = getString(getColumnIndexOrThrow("frequency"))
    )

    private fun Cursor.toCategory() = Category(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        type = CategoryType.valueOf(getString(getColumnIndexOrThrow("type"))),
        iconKey = getString(getColumnIndexOrThrow("icon_key")),
        isDefault = getInt(getColumnIndexOrThrow("is_default")) == 1
    )
}
