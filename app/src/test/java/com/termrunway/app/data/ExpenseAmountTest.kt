package com.termrunway.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ExpenseAmountTest {
    @Test
    fun parsesWholeAndDecimalAmounts() {
        assertEquals(10000L, ExpenseAmount.parseToCents("100"))
        assertEquals(1050L, ExpenseAmount.parseToCents("10.50"))
        assertEquals(999L, ExpenseAmount.parseToCents("9.99"))
    }

    @Test
    fun rejectsInvalidAmounts() {
        assertNull(ExpenseAmount.parseToCents(""))
        assertNull(ExpenseAmount.parseToCents("0"))
        assertNull(ExpenseAmount.parseToCents("-10"))
        assertNull(ExpenseAmount.parseToCents("10.999"))
        assertNull(ExpenseAmount.parseToCents("abc"))
    }

    @Test
    fun formatsCentsAsAmount() {
        assertEquals("123.45", ExpenseAmount.format(12345L))
        assertEquals("10.00", ExpenseAmount.format(1000L))
    }
}
