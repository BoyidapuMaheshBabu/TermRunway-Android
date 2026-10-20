package com.termrunway.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.Instant
import java.time.ZoneId

class TermRunwayDatabase(context: Context) :
    SQLiteOpenHelper(context, "termrunway.db", null, 3) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                category TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                date_str TEXT NOT NULL,
                created_at_ms INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                start_date_str TEXT NOT NULL,
                end_date_str TEXT NOT NULL,
                starting_money_paise INTEGER NOT NULL DEFAULT 0,
                created_at_ms INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE planned_income (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_id INTEGER NOT NULL,
                source TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                expected_date_str TEXT,
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE planned_expense (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                expected_date_str TEXT,
                frequency TEXT NOT NULL DEFAULT 'once',
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                icon_key TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0,
                UNIQUE(type, name)
            )
        """.trimIndent())

        seedCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE categories_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    type TEXT NOT NULL,
                    icon_key TEXT NOT NULL,
                    is_default INTEGER NOT NULL DEFAULT 0,
                    UNIQUE(type, name)
                )
            """.trimIndent())

            db.execSQL("""
                INSERT OR IGNORE INTO categories_new (id, name, type, icon_key, is_default)
                SELECT id, name, type, icon_key, is_default FROM categories
            """.trimIndent())

            db.execSQL("DROP TABLE categories")
            db.execSQL("ALTER TABLE categories_new RENAME TO categories")

            seedCategories(db)
        }

        if (oldVersion < 3) {
            migrateToVersion3(db)
        }
    }

    private fun migrateToVersion3(db: SQLiteDatabase) {
        val zone = ZoneId.systemDefault()

        // 1. Transactions migration
        db.execSQL("""
            CREATE TABLE transactions_v3 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                category TEXT NOT NULL,
                description TEXT NOT NULL DEFAULT '',
                date_str TEXT NOT NULL,
                created_at_ms INTEGER NOT NULL
            )
        """.trimIndent())

        db.rawQuery("SELECT id, type, amount_paise, category, description, date_ms, created_at_ms FROM transactions", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val type = cursor.getString(1)
                val amountPaise = cursor.getLong(2)
                val category = cursor.getString(3)
                val description = cursor.getString(4)
                val dateMs = cursor.getLong(5)
                val createdAtMs = cursor.getLong(6)
                val dateStr = Instant.ofEpochMilli(dateMs).atZone(zone).toLocalDate().toString()

                val values = ContentValues().apply {
                    put("id", id)
                    put("type", type)
                    put("amount_paise", amountPaise)
                    put("category", category)
                    put("description", description)
                    put("date_str", dateStr)
                    put("created_at_ms", createdAtMs)
                }
                db.insertOrThrow("transactions_v3", null, values)
            }
        }
        db.execSQL("DROP TABLE transactions")
        db.execSQL("ALTER TABLE transactions_v3 RENAME TO transactions")

        // 2. Plans migration
        db.execSQL("""
            CREATE TABLE plans_v3 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                start_date_str TEXT NOT NULL,
                end_date_str TEXT NOT NULL,
                starting_money_paise INTEGER NOT NULL DEFAULT 0,
                created_at_ms INTEGER NOT NULL
            )
        """.trimIndent())

        db.rawQuery("SELECT id, name, start_ms, end_ms, starting_money_paise, created_at_ms FROM plans", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val name = cursor.getString(1)
                val startMs = cursor.getLong(2)
                val endMs = cursor.getLong(3)
                val startingMoneyPaise = cursor.getLong(4)
                val createdAtMs = cursor.getLong(5)

                val startDateStr = Instant.ofEpochMilli(startMs).atZone(zone).toLocalDate().toString()
                val endDateStr = Instant.ofEpochMilli(endMs).atZone(zone).toLocalDate().toString()

                val values = ContentValues().apply {
                    put("id", id)
                    put("name", name)
                    put("start_date_str", startDateStr)
                    put("end_date_str", endDateStr)
                    put("starting_money_paise", startingMoneyPaise)
                    put("created_at_ms", createdAtMs)
                }
                db.insertOrThrow("plans_v3", null, values)
            }
        }
        db.execSQL("DROP TABLE plans")
        db.execSQL("ALTER TABLE plans_v3 RENAME TO plans")

        // 3. Planned Income migration
        db.execSQL("""
            CREATE TABLE planned_income_v3 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_id INTEGER NOT NULL,
                source TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                expected_date_str TEXT,
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.rawQuery("SELECT id, plan_id, source, amount_paise, expected_date_ms FROM planned_income", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val planId = cursor.getLong(1)
                val source = cursor.getString(2)
                val amountPaise = cursor.getLong(3)
                val expectedDateMs = if (cursor.isNull(4)) null else cursor.getLong(4)
                val expectedDateStr = expectedDateMs?.let {
                    Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toString()
                }

                val values = ContentValues().apply {
                    put("id", id)
                    put("plan_id", planId)
                    put("source", source)
                    put("amount_paise", amountPaise)
                    if (expectedDateStr == null) putNull("expected_date_str") else put("expected_date_str", expectedDateStr)
                }
                db.insertOrThrow("planned_income_v3", null, values)
            }
        }
        db.execSQL("DROP TABLE planned_income")
        db.execSQL("ALTER TABLE planned_income_v3 RENAME TO planned_income")

        // 4. Planned Expense migration
        db.execSQL("""
            CREATE TABLE planned_expense_v3 (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                expected_date_str TEXT,
                frequency TEXT NOT NULL DEFAULT 'once',
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.rawQuery("SELECT id, plan_id, category, amount_paise, expected_date_ms, frequency FROM planned_expense", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getLong(0)
                val planId = cursor.getLong(1)
                val category = cursor.getString(2)
                val amountPaise = cursor.getLong(3)
                val expectedDateMs = if (cursor.isNull(4)) null else cursor.getLong(4)
                val frequency = cursor.getString(5)
                val expectedDateStr = expectedDateMs?.let {
                    Instant.ofEpochMilli(it).atZone(zone).toLocalDate().toString()
                }

                val values = ContentValues().apply {
                    put("id", id)
                    put("plan_id", planId)
                    put("category", category)
                    put("amount_paise", amountPaise)
                    if (expectedDateStr == null) putNull("expected_date_str") else put("expected_date_str", expectedDateStr)
                    put("frequency", frequency)
                }
                db.insertOrThrow("planned_expense_v3", null, values)
            }
        }
        db.execSQL("DROP TABLE planned_expense")
        db.execSQL("ALTER TABLE planned_expense_v3 RENAME TO planned_expense")
    }

    private fun seedCategories(db: SQLiteDatabase) {
        DefaultCategories.all.forEach { category ->
            val values = ContentValues().apply {
                put("name", category.name)
                put("type", category.type.name)
                put("icon_key", category.iconKey)
                put("is_default", 1)
            }
            db.insertWithOnConflict("categories", null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    companion object {
        fun insertTransaction(db: SQLiteDatabase, transaction: Transaction): Long {
            val values = ContentValues().apply {
                put("type", transaction.type.name)
                put("amount_paise", transaction.amountPaise)
                put("category", transaction.category)
                put("description", transaction.description)
                put("date_str", transaction.date.toString())
                put("created_at_ms", transaction.createdAtMs)
            }
            return db.insertOrThrow("transactions", null, values)
        }

        fun updateTransaction(db: SQLiteDatabase, transaction: Transaction) {
            val values = ContentValues().apply {
                put("type", transaction.type.name)
                put("amount_paise", transaction.amountPaise)
                put("category", transaction.category)
                put("description", transaction.description)
                put("date_str", transaction.date.toString())
            }
            db.update("transactions", values, "id=?", arrayOf(transaction.id.toString()))
        }
    }
}
