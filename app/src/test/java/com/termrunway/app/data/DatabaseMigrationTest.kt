package com.termrunway.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DatabaseMigrationTest {

    @Test
    fun legacyEpochMillisMigrationToLocalDateConversionIsDeterministic() {
        val zone = ZoneId.of("UTC")

        // 1700000000000L is 2023-11-14T22:13:20Z in UTC
        val millis = 1700000000000L
        val convertedDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

        assertEquals(LocalDate.of(2023, 11, 14), convertedDate)
        assertEquals("2023-11-14", convertedDate.toString())
    }

    @Test
    fun iso8601StringParsingPreservesCalendarDateIndependentlyOfTimeZone() {
        val isoDateStr = "2026-10-18"
        val parsedDate = LocalDate.parse(isoDateStr)

        assertEquals(2026, parsedDate.year)
        assertEquals(10, parsedDate.monthValue)
        assertEquals(18, parsedDate.dayOfMonth)

        // String representation is invariant across time zones
        assertEquals("2026-10-18", parsedDate.toString())
    }

    @Test
    fun version2To3MigrationPreservesDataAndPassesForeignKeyCheck() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbHelper = TermRunwayDatabase(context)
        val db = SQLiteDatabase.create(null)
        db.setForeignKeyConstraintsEnabled(true)

        // 1. Setup Version 2 Schema
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

        // 2. Populate Version 2 data
        db.insertOrThrow("categories", null, ContentValues().apply {
            put("id", 100)
            put("name", "Gym")
            put("type", "EXPENSE")
            put("icon_key", "custom")
            put("is_default", 0)
        })

        db.insertOrThrow("transactions", null, ContentValues().apply {
            put("id", 1)
            put("type", "EXPENSE")
            put("amount_paise", 25000)
            put("category", "Food")
            put("description", "Lunch")
            put("date_ms", 1700000000000L)
            put("created_at_ms", 1700000000000L)
        })

        val planId = db.insertOrThrow("plans", null, ContentValues().apply {
            put("id", 10)
            put("name", "Fall 2026")
            put("start_ms", 1700000000000L)
            put("end_ms", 1710000000000L)
            put("starting_money_paise", 500000)
            put("created_at_ms", 1700000000000L)
        })

        db.insertOrThrow("planned_income", null, ContentValues().apply {
            put("id", 101)
            put("plan_id", planId)
            put("source", "Scholarship")
            put("amount_paise", 100000)
            put("expected_date_ms", 1701000000000L)
        })

        db.insertOrThrow("planned_expense", null, ContentValues().apply {
            put("id", 201)
            put("plan_id", planId)
            put("category", "Books")
            put("amount_paise", 50000)
            put("expected_date_ms", 1702000000000L)
            put("frequency", "once")
        })

        // 3. Execute Migration from Version 2 to Version 3
        dbHelper.onUpgrade(db, 2, 3)

        // 4. Verify Foreign Key Integrity
        db.rawQuery("PRAGMA foreign_key_check", null).use { cursor ->
            assertFalse("PRAGMA foreign_key_check reported violations after migration!", cursor.moveToNext())
        }

        // 5. Verify Transactions
        db.rawQuery("SELECT id, type, amount_paise, category, date_str FROM transactions WHERE id=1", null).use { c ->
            assertTrue(c.moveToNext())
            assertEquals(1L, c.getLong(0))
            assertEquals("EXPENSE", c.getString(1))
            assertEquals(25000L, c.getLong(2))
            assertEquals("Food", c.getString(3))
            assertTrue(c.getString(4).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }

        // 6. Verify Plans
        db.rawQuery("SELECT id, name, start_date_str, end_date_str, starting_money_paise FROM plans WHERE id=10", null).use { c ->
            assertTrue(c.moveToNext())
            assertEquals(10L, c.getLong(0))
            assertEquals("Fall 2026", c.getString(1))
            assertTrue(c.getString(2).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            assertTrue(c.getString(3).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            assertEquals(500000L, c.getLong(4))
        }

        // 7. Verify Planned Income
        db.rawQuery("SELECT id, plan_id, source, amount_paise, expected_date_str FROM planned_income WHERE id=101", null).use { c ->
            assertTrue(c.moveToNext())
            assertEquals(101L, c.getLong(0))
            assertEquals(10L, c.getLong(1))
            assertEquals("Scholarship", c.getString(2))
            assertEquals(100000L, c.getLong(3))
            assertTrue(c.getString(4).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
        }

        // 8. Verify Planned Expense
        db.rawQuery("SELECT id, plan_id, category, amount_paise, expected_date_str, frequency FROM planned_expense WHERE id=201", null).use { c ->
            assertTrue(c.moveToNext())
            assertEquals(201L, c.getLong(0))
            assertEquals(10L, c.getLong(1))
            assertEquals("Books", c.getString(2))
            assertEquals(50000L, c.getLong(3))
            assertTrue(c.getString(4).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
            assertEquals("once", c.getString(5))
        }

        // 9. Verify Categories
        db.rawQuery("SELECT name FROM categories WHERE name='Gym'", null).use { c ->
            assertTrue(c.moveToNext())
            assertEquals("Gym", c.getString(0))
        }

        db.close()
    }
}
