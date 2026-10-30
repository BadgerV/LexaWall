package com.lexawall.notifications

import android.Manifest
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
import com.lexawall.R
import com.lexawall.domain.engine.BonusChallengeEngine

class BonusChallengeWorker(
    appContext: android.content.Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationChannels.ensureBaselineChannels(applicationContext)

        val challengeType = BonusChallengeEngine.ChallengeType.fromName(
            inputData.getString(KEY_CHALLENGE_TYPE)
        ) ?: return Result.success()
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID)

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_BONUS_CHALLENGE
            putExtra(MainActivity.EXTRA_CHALLENGE_TYPE, challengeType.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.ENGAGEMENT_HIGH_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_lexa_hummingbird)
            .setContentTitle(challengeType.title)
            .setContentText("Complete a challenge for +${challengeType.rewardXp} XP bonus today")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Complete a challenge for +${challengeType.rewardXp} XP bonus today"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                R.drawable.ic_lexa_hummingbird,
                "Try Challenge",
                pendingIntent
            )
            .build()

        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return Result.success()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            manager.notify(notificationId, notification)
        }

        return Result.success()
    }

    companion object {
        const val KEY_CHALLENGE_TYPE = "bonus_challenge_type"
        const val KEY_NOTIFICATION_ID = "bonus_challenge_notification_id"
        private const val DEFAULT_NOTIFICATION_ID = 2002
        private const val REQUEST_CODE = 3010
    }
}
