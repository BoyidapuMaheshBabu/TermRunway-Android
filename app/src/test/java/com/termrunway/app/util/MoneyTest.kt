package com.termrunway.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun formats_rupees_without_unnecessary_decimals() {
        assertEquals("₹1200", formatRupees(120000L))
        assertEquals("₹1200.50", formatRupees(120050L))
        assertEquals("+₹300", formatSignedRupees(30000L))
        assertEquals("-₹300", formatSignedRupees(-30000L))
    }

    @Test
    fun rejects_invalid_amounts() {
        assertNull(parseMoneyToCents(""))
        assertNull(parseMoneyToCents("abc"))
        assertNull(parseMoneyToCents("-10"))
    }
}
