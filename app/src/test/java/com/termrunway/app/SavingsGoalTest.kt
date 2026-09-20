package com.termrunway.app

import com.termrunway.app.data.SavingsGoal
import org.junit.Assert.assertEquals
import org.junit.Test

class SavingsGoalTest {

    @Test
    fun testSavingsGoalProgress() {
        val goal = SavingsGoal(name = "Laptop", targetAmountCents = 20000_00, savedAmountCents = 8000_00)
        val remaining = (goal.targetAmountCents - goal.savedAmountCents).coerceAtLeast(0L)
        val progress = if (goal.targetAmountCents > 0) {
            (goal.savedAmountCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

        assertEquals(12000_00L, remaining)
        assertEquals(0.4f, progress, 0.01f)
    }

    @Test
    fun testSavingsGoalCompleted() {
        val goal = SavingsGoal(name = "Laptop", targetAmountCents = 20000_00, savedAmountCents = 25000_00)
        val remaining = (goal.targetAmountCents - goal.savedAmountCents).coerceAtLeast(0L)
        val progress = if (goal.targetAmountCents > 0) {
            (goal.savedAmountCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

        assertEquals(0L, remaining)
        assertEquals(1.0f, progress, 0.01f)
    }

    @Test
    fun testSavingsGoalZeroTarget() {
        val goal = SavingsGoal(name = "Laptop", targetAmountCents = 0L, savedAmountCents = 8000_00)
        val remaining = (goal.targetAmountCents - goal.savedAmountCents).coerceAtLeast(0L)
        val progress = if (goal.targetAmountCents > 0) {
            (goal.savedAmountCents.toFloat() / goal.targetAmountCents.toFloat()).coerceIn(0f, 1f)
        } else 0f

        assertEquals(0L, remaining)
        assertEquals(0f, progress, 0.01f)
    }
}
