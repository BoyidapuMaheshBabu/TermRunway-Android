package com.termrunway.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationHelper.createNotificationChannel(context)
            // Default sync on boot (VM / app startup also syncs based on preferences)
            NotificationScheduler.syncReminders(
                context = context,
                masterEnabled = true,
                dailyEnabled = true,
                weeklyEnabled = true,
                monthlyEnabled = true,
                planEndingEnabled = true
            )
        }
    }
}
