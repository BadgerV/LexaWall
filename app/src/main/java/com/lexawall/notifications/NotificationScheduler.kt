package com.lexawall.notifications

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lexawall.BuildConfig
import com.lexawall.data.local.PrefsManager
import com.lexawall.domain.engine.BonusChallengeEngine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object NotificationScheduler {
    fun schedulePostQuizNotification(
        context: Context,
        word: String,
        notificationLine1: String,
        notificationLine2: String,
        quizCompletedHour: Int
    ) {
        val delayValue: Long
        val delayUnit: TimeUnit
        if (BuildConfig.DEBUG) {
            delayValue = 15L
            delayUnit = TimeUnit.SECONDS
        } else {
            delayValue = if (quizCompletedHour < 21) {
                Random.nextLong(4, 7)
            } else {
                (32 - quizCompletedHour).toLong()
            }
            delayUnit = TimeUnit.HOURS
        }

        val inputData = Data.Builder()
            .putString(LexaWallNotificationWorker.KEY_WORD, word)
            .putString(LexaWallNotificationWorker.KEY_NOTIFICATION_LINE_1, notificationLine1)
            .putString(LexaWallNotificationWorker.KEY_NOTIFICATION_LINE_2, notificationLine2)
            .build()

        val request = OneTimeWorkRequestBuilder<LexaWallNotificationWorker>()
            .setInitialDelay(delayValue, delayUnit)
            .setInputData(inputData)
            .addTag(WORK_TAG)
            .build()

        try {
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_TAG,
                ExistingWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to schedule post-word-drop notification", e)
        }
    }

    fun cancelPendingNotification(context: Context) {
        try {
            WorkManager.getInstance(context.applicationContext).cancelAllWorkByTag(WORK_TAG)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to cancel post-word-drop notification work", e)
        }
    }

    fun scheduleBonusChallengeNotification(
        context: Context,
        challengeType: BonusChallengeEngine.ChallengeType
    ) {
        val appContext = context.applicationContext
        val hasPendingSlot2 = hasPendingPostWordDropNotification(appContext)
        val initialDelayMinutes = if (hasPendingSlot2) 30L else 0L
        val notificationId = buildChallengeNotificationId(challengeType)

        val inputData = Data.Builder()
            .putString(BonusChallengeWorker.KEY_CHALLENGE_TYPE, challengeType.name)
            .putInt(BonusChallengeWorker.KEY_NOTIFICATION_ID, notificationId)
            .build()

        val request = OneTimeWorkRequestBuilder<BonusChallengeWorker>()
            .setInputData(inputData)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .addTag(BONUS_CHALLENGE_WORK_TAG)
            .build()

        try {
            WorkManager.getInstance(appContext).enqueueUniqueWork(
                BONUS_CHALLENGE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to schedule bonus challenge notification", e)
        }
    }

    fun scheduleHabitStackNotification(context: Context) {
        val hour = PrefsManager.getHabitStackHour(context.applicationContext) ?: return
        val now = LocalDateTime.now()
        var next = now
            .withHour(hour.coerceIn(0, 23))
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
        if (!next.isAfter(now)) {
            next = next.plusDays(1)
        }
        val initialDelayMinutes = ChronoUnit.MINUTES.between(now, next).coerceAtLeast(0)

        val inputData = Data.Builder()
            .putInt(HabitStackWorker.KEY_HABIT_HOUR, hour.coerceIn(0, 23))
            .build()

        val request = PeriodicWorkRequestBuilder<HabitStackWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(HABIT_STACK_WORK_NAME)
            .build()

        try {
            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                HABIT_STACK_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to schedule habit stack notification", e)
        }
    }

    fun scheduleHabitStackNotification(context: Context, hour: Int) {
        PrefsManager.saveHabitStackHour(context.applicationContext, hour.coerceIn(0, 23))
        scheduleHabitStackNotification(context.applicationContext)
    }

    fun cancelHabitStackNotification(context: Context) {
        try {
            WorkManager.getInstance(context.applicationContext).cancelUniqueWork(HABIT_STACK_WORK_NAME)
        } catch (e: Exception) {
            Log.e(TAG, "Unable to cancel habit stack notification work", e)
        }
    }

    private fun hasPendingPostWordDropNotification(context: Context): Boolean {
        return try {
            val infos = WorkManager.getInstance(context).getWorkInfosByTag(WORK_TAG).get()
            infos.any { info ->
                info.state == WorkInfo.State.ENQUEUED ||
                    info.state == WorkInfo.State.RUNNING ||
                    info.state == WorkInfo.State.BLOCKED
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun buildChallengeNotificationId(challengeType: BonusChallengeEngine.ChallengeType): Int {
        val daySalt = (LocalDate.now().toEpochDay() % 997L).toInt()
        return 2000 + (challengeType.ordinal * 1000) + daySalt
    }

    private const val TAG = "NotificationScheduler"
    private const val WORK_TAG = "post_word_drop_notification"
    private const val HABIT_STACK_WORK_NAME = "habit_stack_notification"
    private const val BONUS_CHALLENGE_WORK_NAME = "bonus_challenge_notification"
    private const val BONUS_CHALLENGE_WORK_TAG = "bonus_challenge_notification"
}
