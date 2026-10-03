package com.termrunway.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TermRunwayDatabase(context: Context) :
    SQLiteOpenHelper(context, "termrunway.db", null, 1) {

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
                date_ms INTEGER NOT NULL,
                created_at_ms INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE plans (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                start_ms INTEGER NOT NULL,
                end_ms INTEGER NOT NULL,
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
                expected_date_ms INTEGER,
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE planned_expense (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                plan_id INTEGER NOT NULL,
                category TEXT NOT NULL,
                amount_paise INTEGER NOT NULL CHECK(amount_paise >= 0),
                expected_date_ms INTEGER,
                frequency TEXT NOT NULL DEFAULT 'once',
                FOREIGN KEY(plan_id) REFERENCES plans(id) ON DELETE CASCADE
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL UNIQUE,
                type TEXT NOT NULL,
                icon_key TEXT NOT NULL,
                is_default INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        seedCategories(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    private fun seedCategories(db: SQLiteDatabase) {
        DefaultCategories.all.forEach { category ->
            db.insert("categories", null, ContentValues().apply {
                put("name", category.name)
                put("type", category.type.name)
                put("icon_key", category.iconKey)
                put("is_default", 1)
            })
        }
    }

    companion object {
        fun insertTransaction(db: SQLiteDatabase, transaction: Transaction): Long {
            val values = ContentValues().apply {
                put("type", transaction.type.name)
                put("amount_paise", transaction.amountPaise)
                put("category", transaction.category)
                put("description", transaction.description)
                put("date_ms", transaction.dateMs)
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
                put("date_ms", transaction.dateMs)
            }
            db.update("transactions", values, "id=?", arrayOf(transaction.id.toString()))
        }
    }
}
