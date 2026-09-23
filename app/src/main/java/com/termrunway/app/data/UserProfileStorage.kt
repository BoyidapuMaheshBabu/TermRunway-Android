package com.termrunway.app.data

import android.content.Context

class UserProfileStorage(context: Context) {
    private val prefs = context.getSharedPreferences("termrunway_user", Context.MODE_PRIVATE)

    fun username(): String = prefs.getString(KEY_USERNAME, "")?.trim().orEmpty()

    fun setUsername(value: String) {
        prefs.edit().putString(KEY_USERNAME, value.trim()).apply()
    }

    fun dailyLimitCents(): Long = prefs.getLong(KEY_DAILY_LIMIT, 0L)

    fun setDailyLimitCents(value: Long) {
        prefs.edit().putLong(KEY_DAILY_LIMIT, value.coerceAtLeast(0L)).apply()
    }

    fun theme(): String = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"

    fun setTheme(value: String) {
        prefs.edit().putString(KEY_THEME, value).apply()
    }

    fun lastBackupName(): String = prefs.getString(KEY_LAST_BACKUP, "").orEmpty()

    fun setLastBackupName(value: String) {
        prefs.edit().putString(KEY_LAST_BACKUP, value).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USERNAME = "username"
        const val KEY_DAILY_LIMIT = "daily_limit_cents"
        const val KEY_THEME = "theme"
        const val KEY_LAST_BACKUP = "last_backup_name"
    }
}
