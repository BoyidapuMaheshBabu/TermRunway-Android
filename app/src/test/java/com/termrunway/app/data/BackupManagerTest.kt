package com.termrunway.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BackupManagerTest {

    @Test
    fun legacyVersion1RoundTripTest() {
        val jsonString = """
            {
              "schemaVersion": 1,
              "app": "TermRunway",
              "name": "Mahesh Babu",
              "themeMode": "dark",
              "trackingMode": "semester",
              "notificationsEnabled": true,
              "dailyReminderEnabled": false,
              "weeklyReviewEnabled": true,
              "monthlyReviewEnabled": true,
              "planEndingEnabled": true,
              "reminderHour": 21,
              "reminderMinute": 30,
              "transactions": [
                {
                  "id": 1,
                  "type": "INCOME",
                  "amountPaise": 5000000,
                  "category": "Parents / Allowance",
                  "description": "Allowance",
                  "dateMs": 1700000000000,
                  "createdAtMs": 1700000000000
                },
                {
                  "id": 2,
                  "type": "EXPENSE",
                  "amountPaise": 15000,
                  "category": "Food",
                  "description": "Coffee",
                  "dateMs": 1700000500000,
                  "createdAtMs": 1700000500000
                }
              ],
              "plans": [
                {
                  "id": 10,
                  "name": "Fall 2026",
                  "startMs": 1700000000000,
                  "endMs": 1710000000000,
                  "startingMoneyPaise": 200000,
                  "createdAtMs": 1700000000000
                }
              ],
              "plannedIncomes": [],
              "plannedExpenses": [],
              "categories": []
            }
        """.trimIndent()

        val snapshot = BackupManager.fromJsonString(jsonString)

        assertEquals("Mahesh Babu", snapshot.name)
        assertEquals("dark", snapshot.themeMode)
        assertEquals(1, snapshot.schemaVersion)
        assertEquals(2, snapshot.transactions.size)
        assertEquals(1, snapshot.plans.size)
    }

    @Test
    fun schemaVersion2RoundTripTest() {
        val originalSnapshot = BackupSnapshot(
            schemaVersion = 2,
            name = "Mahesh Babu",
            themeMode = "dark",
            trackingMode = "daily",
            notificationsEnabled = true,
            dailyReminderEnabled = true,
            weeklyReviewEnabled = true,
            monthlyReviewEnabled = true,
            planEndingEnabled = true,
            reminderHour = 20,
            reminderMinute = 0,
            transactions = listOf(
                Transaction(
                    id = 1,
                    type = TransactionType.EXPENSE,
                    amountPaise = 25000,
                    category = "Food",
                    description = "Lunch",
                    date = LocalDate.of(2026, 10, 18),
                    createdAtMs = 1700000000000
                )
            ),
            plans = listOf(
                FinancialPlan(
                    id = 10,
                    name = "Fall 2026",
                    startDate = LocalDate.of(2026, 9, 1),
                    endDate = LocalDate.of(2026, 12, 31),
                    startingMoneyPaise = 500000,
                    createdAtMs = 1700000000000
                )
            ),
            plannedIncomes = listOf(
                PlannedIncome(
                    id = 101,
                    planId = 10,
                    source = "Scholarship",
                    amountPaise = 100000,
                    expectedDate = LocalDate.of(2026, 9, 15)
                )
            ),
            plannedExpenses = listOf(
                PlannedExpense(
                    id = 201,
                    planId = 10,
                    category = "Books",
                    amountPaise = 50000,
                    expectedDate = LocalDate.of(2026, 9, 20)
                )
            ),
            categories = DefaultCategories.all
        )

        val json = BackupManager.toJson(originalSnapshot)
        val restored = BackupManager.fromJsonString(json.toString())

        assertEquals(2, restored.schemaVersion)
        assertEquals("Mahesh Babu", restored.name)
        assertEquals(1, restored.transactions.size)
        assertEquals(LocalDate.of(2026, 10, 18), restored.transactions[0].date)
        assertEquals(LocalDate.of(2026, 9, 1), restored.plans[0].startDate)
        assertEquals(LocalDate.of(2026, 12, 31), restored.plans[0].endDate)
        assertEquals(LocalDate.of(2026, 9, 15), restored.plannedIncomes[0].expectedDate)
        assertEquals(LocalDate.of(2026, 9, 20), restored.plannedExpenses[0].expectedDate)
    }

    @Test
    fun profileAndCategoryOnlyDataWhenNoTransactionsTest() {
        val jsonString = """
            {
              "app": "TermRunway",
              "schemaVersion": 2,
              "name": "New Profile Only",
              "themeMode": "light",
              "transactions": [],
              "plans": [],
              "plannedIncomes": [],
              "plannedExpenses": [],
              "categories": [
                {
                  "id": 200,
                  "name": "Gym",
                  "type": "EXPENSE",
                  "iconKey": "custom",
                  "isDefault": false
                }
              ]
            }
        """.trimIndent()

        val restored = BackupManager.fromJsonString(jsonString)
        assertEquals("New Profile Only", restored.name)
        assertTrue(restored.transactions.isEmpty())
        assertTrue(restored.plans.isEmpty())
        assertTrue(restored.categories.any { it.name == "Gym" })
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidAppIdentifierRejectsBackup() {
        val invalidJson = """{"app": "WrongApp", "schemaVersion": 2}"""
        BackupManager.fromJsonString(invalidJson)
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedSchemaVersionRejectsBackup() {
        val invalidJson = """{"app": "TermRunway", "schemaVersion": 99}"""
        BackupManager.fromJsonString(invalidJson)
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeTransactionAmountRejectsBackup() {
        val invalidJson = """
            {
              "app": "TermRunway",
              "schemaVersion": 2,
              "name": "User",
              "transactions": [
                {"id": 1, "type": "EXPENSE", "amountPaise": -500, "category": "Food", "date": "2026-10-18"}
              ]
            }
        """.trimIndent()
        BackupManager.fromJsonString(invalidJson)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingParentPlanReferenceInPlannedIncomeRejectsBackup() {
        val invalidJson = """
            {
              "app": "TermRunway",
              "schemaVersion": 2,
              "name": "User",
              "plans": [
                {"id": 1, "name": "Plan 1", "startDate": "2026-09-01", "endDate": "2026-12-31"}
              ],
              "plannedIncomes": [
                {"id": 10, "planId": 999, "source": "Grant", "amountPaise": 1000}
              ]
            }
        """.trimIndent()
        BackupManager.fromJsonString(invalidJson)
    }

    @Test(expected = IllegalArgumentException::class)
    fun missingParentPlanReferenceInPlannedExpenseRejectsBackup() {
        val invalidJson = """
            {
              "app": "TermRunway",
              "schemaVersion": 2,
              "name": "User",
              "plans": [
                {"id": 1, "name": "Plan 1", "startDate": "2026-09-01", "endDate": "2026-12-31"}
              ],
              "plannedExpenses": [
                {"id": 20, "planId": 888, "category": "Food", "amountPaise": 500}
              ]
            }
        """.trimIndent()
        BackupManager.fromJsonString(invalidJson)
    }

    @Test
    fun legacyVersion1BackupWithoutNewPreferencesLoadsSafely() {
        val legacyJson = """
            {
              "app": "TermRunway",
              "schemaVersion": 1,
              "name": "Legacy User",
              "themeMode": "light",
              "transactions": [],
              "plans": []
            }
        """.trimIndent()

        val parsed = BackupManager.fromJsonString(legacyJson)
        assertEquals("Legacy User", parsed.name)
        assertEquals("daily", parsed.trackingMode)
        assertTrue(parsed.notificationsEnabled)
        assertEquals(20, parsed.reminderHour)
        assertEquals(0, parsed.reminderMinute)
    }
}
