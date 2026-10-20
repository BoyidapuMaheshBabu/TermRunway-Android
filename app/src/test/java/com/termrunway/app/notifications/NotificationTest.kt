package com.termrunway.app.notifications

import com.termrunway.app.ui.AppUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationTest {

    @Test
    fun defaultNotificationPreferencesAreEnabled() {
        val state = AppUiState()
        assertTrue(state.notificationsEnabled)
        assertTrue(state.dailyReminderEnabled)
        assertTrue(state.weeklyReviewEnabled)
        assertTrue(state.monthlyReviewEnabled)
        assertTrue(state.planEndingEnabled)
        assertEquals(20, state.reminderHour)
        assertEquals(0, state.reminderMinute)
    }

    @Test
    fun notificationConstantsAreValid() {
        assertEquals("termrunway_reminders", NotificationHelper.CHANNEL_ID)
        assertEquals(1001, NotificationHelper.DAILY_REMINDER_ID)
        assertEquals(1002, NotificationHelper.WEEKLY_REVIEW_ID)
        assertEquals(1003, NotificationHelper.MONTHLY_REVIEW_ID)
        assertEquals(1004, NotificationHelper.PLAN_PROGRESS_ID)
    }

    @Test
    fun customReminderTimeUpdatesHourAndMinuteInState() {
        val state = AppUiState().copy(reminderHour = 20, reminderMinute = 35)
        assertEquals(20, state.reminderHour)
        assertEquals(35, state.reminderMinute)
    }

    @Test
    fun presetReminderTimeResetsMinuteToZero() {
        val customState = AppUiState().copy(reminderHour = 20, reminderMinute = 35)
        val presetState = customState.copy(reminderHour = 19, reminderMinute = 0)
        assertEquals(19, presetState.reminderHour)
        assertEquals(0, presetState.reminderMinute)
    }
}
