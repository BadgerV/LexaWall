package com.lexawall.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val ENGAGEMENT_HIGH_CHANNEL_ID = "lexawall_engagement_high"

    fun ensureBaselineChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val engagementChannel = NotificationChannel(
            ENGAGEMENT_HIGH_CHANNEL_ID,
            "Lexa Challenges",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Habit stacking and bonus challenge alerts"
        }

        manager.createNotificationChannel(engagementChannel)
    }
}
