package com.termrunway.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupManagerTest {

    @Test
    fun backupSnapshotDataStructureValidation() {
        val snapshot = BackupSnapshot(
            schemaVersion = 1,
            name = "Mahesh",
            themeMode = "system",
            transactions = listOf(
                Transaction(id = 1, type = TransactionType.INCOME, amountPaise = 5000_00, category = "Parents", description = "Monthly allowance", dateMs = 1000000L)
            ),
            plans = listOf(
                FinancialPlan(id = 1, name = "Fall Term", startMs = 1000000L, endMs = 2000000L, startingMoneyPaise = 10000_00)
            ),
            plannedIncomes = listOf(
                PlannedIncome(id = 1, planId = 1, source = "Scholarship", amountPaise = 20000_00)
            ),
            plannedExpenses = listOf(
                PlannedExpense(id = 1, planId = 1, category = "Food", amountPaise = 10000_00)
            ),
            categories = DefaultCategories.all
        )

        assertEquals("Mahesh", snapshot.name)
        assertEquals(1, snapshot.schemaVersion)
        assertEquals(1, snapshot.transactions.size)
        assertEquals(1, snapshot.plans.size)
        assertEquals(1, snapshot.plannedIncomes.size)
        assertEquals(1, snapshot.plannedExpenses.size)
        assertTrue(snapshot.categories.isNotEmpty())
    }
}
