package com.termrunway.app.ui.mode

import org.junit.Assert.assertEquals
import org.junit.Test

class TrackingModeTest {
    @Test
    fun storageValue_roundTripsDailyMode() {
        assertEquals(TrackingMode.DAILY, TrackingMode.fromStorageValue("daily"))
    }

    @Test
    fun storageValue_roundTripsPlanMode() {
        assertEquals(TrackingMode.PLAN, TrackingMode.fromStorageValue("plan"))
    }

    @Test
    fun unknownStorageValue_defaultsToDailyMode() {
        assertEquals(TrackingMode.DAILY, TrackingMode.fromStorageValue("unknown"))
        assertEquals(TrackingMode.DAILY, TrackingMode.fromStorageValue(null))
    }
}
