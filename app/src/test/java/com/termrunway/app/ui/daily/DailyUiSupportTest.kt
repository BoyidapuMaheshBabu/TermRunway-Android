package com.termrunway.app.ui.daily

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DailyUiSupportTest {
    @Test
    fun parsesValidRupeeAmount() {
        assertEquals(12550L, parseAmountCents("125.50"))
        assertEquals(100000L, parseAmountCents("1,000"))
    }

    @Test
    fun rejectsInvalidAmounts() {
        assertNull(parseAmountCents(""))
        assertNull(parseAmountCents("0"))
        assertNull(parseAmountCents("-10"))
        assertNull(parseAmountCents("10.123"))
    }

    @Test
    fun spendingIndicationNeedsMeaningfulDifference() {
        assertNull(dailySpendingMessage(12000L, 10000L))
        assertEquals(
            "Today is higher than your recent spending average.",
            dailySpendingMessage(18000L, 10000L)
        )
        assertEquals(
            "Today is lower than your recent spending average.",
            dailySpendingMessage(4000L, 10000L)
        )
    }
}
