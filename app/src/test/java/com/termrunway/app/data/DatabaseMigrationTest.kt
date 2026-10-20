package com.termrunway.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

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
}
