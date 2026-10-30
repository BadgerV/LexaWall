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
import com.lexawall.data.local.DatabaseModule
import java.time.LocalDate

class HabitStackWorker(
    appContext: android.content.Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        NotificationChannels.ensureBaselineChannels(applicationContext)

        val hour = inputData.getInt(KEY_HABIT_HOUR, 8)
        val body = messageForHour(hour)

        val progressLine = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            runCatching { progressSummaryLine() }.getOrNull()
        } else {
            null
        }

        val content = if (!progressLine.isNullOrBlank()) {
            "$body\n\n$progressLine"
        } else {
            body
        }

        val openIntent = Intent(applicationContext, MainActivity::class.java).apply {
            action = MainActivity.ACTION_OPEN_WORD_DROP
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1002,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.ENGAGEMENT_HIGH_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_lexa_hummingbird)
            .setContentTitle("Learning time!")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return Result.success()

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            manager.notify(HABIT_NOTIFICATION_ID, notification)
        }

        return Result.success()
    }

    private suspend fun progressSummaryLine(): String {
        val db = DatabaseModule.getInstance(applicationContext)
        val today = LocalDate.now().toString()
        val words = db.userProgressDao().getProgressByLastAnsweredDate(today).size
        val xp = db.quizSessionDao().getSessionsForDate(today).sumOf { it.xpEarned }
        val streak = db.streakDao().getStreak().currentStreak
        return "📚 Today's Progress:\n$words words learned | $xp XP earned | ${streak}-day streak 🔥"
    }

    private fun messageForHour(hour: Int): String {
        return when (hour) {
            7 -> "🌅 Good morning! Let's learn a new word."
            8 -> "🍽️ Time to learn over breakfast"
            9 -> "🚗 Learn something new on your commute"
            12 -> "🍴 Lunch break? Let's expand your vocabulary"
            15 -> "☕ Afternoon refresh — time for a word"
            18 -> "🌆 Evening learning — your word awaits"
            21 -> "🌙 Before bed, master today's word"
            else -> "Time for your word!"
        }
    }

    companion object {
        const val KEY_HABIT_HOUR = "habit_hour"
        private const val HABIT_NOTIFICATION_ID = 1102
    }
}
