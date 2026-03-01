package com.lexawall.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lexawall.MainActivity

class LexaWallNotificationWorker(
    appContext: android.content.Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val line1Input = inputData.getString(KEY_NOTIFICATION_LINE_1).orEmpty()
        val line2Input = inputData.getString(KEY_NOTIFICATION_LINE_2).orEmpty()

        createNotificationChannel()

        val line1 = line1Input.ifBlank { "Your word is waiting." }
        val line2 = line2Input.ifBlank { "You know what to do." }
        val body = "$line1\n$line2"
        val title = "Lexa 🐦"
        val owlIconRes = applicationContext.resources.getIdentifier(
            "ic_lexa_hummingbird",
            "drawable",
            applicationContext.packageName
        )
        val smallIconRes = if (owlIconRes != 0) owlIconRes else android.R.drawable.ic_dialog_info

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(smallIconRes)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        if (!notificationManager.areNotificationsEnabled()) {
            return Result.success()
        }

        val hasPostNotificationsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPostNotificationsPermission) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }

        return Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = applicationContext.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val KEY_WORD = "WORD"
        const val KEY_NOTIFICATION_LINE_1 = "NOTIFICATION_LINE_1"
        const val KEY_NOTIFICATION_LINE_2 = "NOTIFICATION_LINE_2"

        private const val CHANNEL_ID = "lexawall_praise"
        private const val CHANNEL_NAME = "Lexa"
        private const val CHANNEL_DESCRIPTION = "Daily word reminders from Lexa"
        private const val NOTIFICATION_ID = 1001
    }
}
