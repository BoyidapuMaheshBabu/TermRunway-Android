package com.termrunway.app.ui.mode

/**
 * Determines how TermRunway presents the user's shared financial data.
 *
 * Tracking mode is application state, not a separate navigation tree or data store.
 */
enum class TrackingMode(val storageValue: String) {
    DAILY("daily"),
    PLAN("plan");

    companion object {
        fun fromStorageValue(value: String?): TrackingMode =
            entries.firstOrNull { it.storageValue == value } ?: DAILY
    }
}
