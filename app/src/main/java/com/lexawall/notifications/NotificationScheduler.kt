package com.lexawall.notifications

import android.content.Context
import android.util.Log
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.lexawall.BuildConfig
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

    private const val TAG = "NotificationScheduler"
    private const val WORK_TAG = "post_word_drop_notification"
}
