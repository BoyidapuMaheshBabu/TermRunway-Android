package com.termrunway.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.termRunwayDataStore by preferencesDataStore(name = "termrunway_settings")

object PreferencesKeys {
    val NAME_KEY = stringPreferencesKey("name")
    val THEME_KEY = stringPreferencesKey("theme")
    val TRACKING_MODE_KEY = stringPreferencesKey("tracking_mode")
    val ONBOARDING_KEY = booleanPreferencesKey("onboarding_completed")
    val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
    val DAILY_REMINDER_KEY = booleanPreferencesKey("daily_reminder_enabled")
    val WEEKLY_REVIEW_KEY = booleanPreferencesKey("weekly_review_enabled")
    val MONTHLY_REVIEW_KEY = booleanPreferencesKey("monthly_review_enabled")
    val PLAN_ENDING_KEY = booleanPreferencesKey("plan_ending_enabled")
    val REMINDER_HOUR_KEY = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE_KEY = intPreferencesKey("reminder_minute")
}
