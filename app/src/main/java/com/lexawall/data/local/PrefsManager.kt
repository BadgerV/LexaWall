package com.lexawall.data.local

import android.content.Context

object PrefsManager {
    private const val PREFS_NAME = "lexawall_prefs"
    private const val KEY_USER_CEFR_LEVEL = "user_cefr_level"
    private const val KEY_DB_SEEDED = "db_seeded"
    private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    private const val KEY_CURRENT_WORD_ID = "current_word_id"
    private const val KEY_QUIZ_IN_PROGRESS = "quiz_in_progress"
    private const val KEY_ROTATION_SPEED = "rotation_speed"
    private const val KEY_WORDS_V2_MIGRATED = "words_v2_migrated"
    private const val KEY_FLASHCARD_SESSIONS_TODAY = "flashcard_sessions_today"
    private const val KEY_FLASHCARD_SESSION_DATE = "flashcard_session_date"
    private const val KEY_PENDING_GRADUATION_WORD_ID = "pending_graduation_word_id"
    private const val KEY_PENDING_QUIZ_END_SCORE = "pending_quiz_end_score"
    private const val KEY_PENDING_QUIZ_END_TOTAL = "pending_quiz_end_total"
    private const val KEY_PENDING_QUIZ_END_STREAK = "pending_quiz_end_streak"
    private const val KEY_PENDING_QUIZ_END_XP = "pending_quiz_end_xp"
    private const val KEY_UNLOCKED_WORD_TIER = "unlocked_word_tier"
    private const val KEY_UNLOCKED_THEME = "unlocked_theme"
    private const val KEY_ACTIVE_THEME = "active_theme"
    private const val KEY_WALLPAPER_SET = "wallpaper_set"
    private const val KEY_COMPETITIVE_MODE = "competitive_mode"
    private const val KEY_WALLPAPER_OPTED_OUT = "wallpaper_opted_out"
    private const val KEY_WANDER_DATE = "wander_date"
    private const val KEY_WANDER_WORD_IDS = "wander_word_ids"
    private const val KEY_WANDER_SEEN_ALL_IDS = "wander_seen_all_ids"
    private const val KEY_DAILY_POOL_DATE = "daily_pool_date"
    private const val KEY_DAILY_POOL_WORD_IDS = "daily_pool_word_ids"

    fun saveUserLevel(context: Context, level: String) {
        prefs(context).edit().putString(KEY_USER_CEFR_LEVEL, level).apply()
    }

    fun getUserLevel(context: Context): String {
        return prefs(context).getString(KEY_USER_CEFR_LEVEL, "B2") ?: "B2"
    }

    fun saveDbSeeded(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DB_SEEDED, value).apply()
    }

    fun isDbSeeded(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_DB_SEEDED, false)
    }

    fun saveOnboardingComplete(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()
    }

    fun isOnboardingComplete(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ONBOARDING_COMPLETE, false)
    }

    fun saveCurrentWordId(context: Context, id: String) {
        prefs(context).edit().putString(KEY_CURRENT_WORD_ID, id).apply()
    }

    fun getCurrentWordId(context: Context): String? {
        return prefs(context).getString(KEY_CURRENT_WORD_ID, null)
    }

    fun saveQuizInProgress(context: Context, inProgress: Boolean) {
        prefs(context).edit().putBoolean(KEY_QUIZ_IN_PROGRESS, inProgress).apply()
    }

    fun getQuizInProgress(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_QUIZ_IN_PROGRESS, false)
    }

    fun saveRotationSpeed(context: Context, speed: String) {
        prefs(context).edit().putString(KEY_ROTATION_SPEED, speed).apply()
    }

    fun getRotationSpeed(context: Context): String {
        return prefs(context).getString(KEY_ROTATION_SPEED, "10min") ?: "10min"
    }

    fun saveWordsMigrated(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_WORDS_V2_MIGRATED, value).apply()
    }

    fun isWordsMigrated(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_WORDS_V2_MIGRATED, false)
    }

    fun saveFlashcardSessionsToday(context: Context, count: Int) {
        prefs(context).edit().putInt(KEY_FLASHCARD_SESSIONS_TODAY, count).apply()
    }

    fun getFlashcardSessionsToday(context: Context): Int {
        return prefs(context).getInt(KEY_FLASHCARD_SESSIONS_TODAY, 0)
    }

    fun saveFlashcardSessionDate(context: Context, date: String) {
        prefs(context).edit().putString(KEY_FLASHCARD_SESSION_DATE, date).apply()
    }

    fun getFlashcardSessionDate(context: Context): String {
        return prefs(context).getString(KEY_FLASHCARD_SESSION_DATE, "") ?: ""
    }

    fun saveGraduationWordId(context: Context, id: String?) {
        prefs(context).edit().putString(KEY_PENDING_GRADUATION_WORD_ID, id).apply()
    }

    fun getGraduationWordId(context: Context): String? {
        return prefs(context).getString(KEY_PENDING_GRADUATION_WORD_ID, null)
    }

    fun savePendingQuizEnd(context: Context, score: Int, total: Int, streak: Int, xp: Int) {
        prefs(context).edit()
            .putInt(KEY_PENDING_QUIZ_END_SCORE, score)
            .putInt(KEY_PENDING_QUIZ_END_TOTAL, total)
            .putInt(KEY_PENDING_QUIZ_END_STREAK, streak)
            .putInt(KEY_PENDING_QUIZ_END_XP, xp)
            .apply()
    }

    fun getPendingQuizEnd(context: Context): Quadruple<Int, Int, Int, Int>? {
        val score = prefs(context).getInt(KEY_PENDING_QUIZ_END_SCORE, -1)
        val total = prefs(context).getInt(KEY_PENDING_QUIZ_END_TOTAL, -1)
        val streak = prefs(context).getInt(KEY_PENDING_QUIZ_END_STREAK, -1)
        val xp = prefs(context).getInt(KEY_PENDING_QUIZ_END_XP, -1)
        if (score < 0 || total <= 0 || streak < 0 || xp < 0) return null
        return Quadruple(score, total, streak, xp)
    }

    fun clearPendingQuizEnd(context: Context) {
        prefs(context).edit()
            .remove(KEY_PENDING_QUIZ_END_SCORE)
            .remove(KEY_PENDING_QUIZ_END_TOTAL)
            .remove(KEY_PENDING_QUIZ_END_STREAK)
            .remove(KEY_PENDING_QUIZ_END_XP)
            .apply()
    }

    fun saveUnlockedWordTier(context: Context, tier: String?) {
        prefs(context).edit().putString(KEY_UNLOCKED_WORD_TIER, tier).apply()
    }

    fun getUnlockedWordTier(context: Context): String? {
        return prefs(context).getString(KEY_UNLOCKED_WORD_TIER, null)
    }

    fun saveUnlockedTheme(context: Context, theme: String?) {
        prefs(context).edit().putString(KEY_UNLOCKED_THEME, theme).apply()
    }

    fun getUnlockedTheme(context: Context): String? {
        return prefs(context).getString(KEY_UNLOCKED_THEME, null)
    }

    fun saveActiveTheme(context: Context, theme: String) {
        prefs(context).edit().putString(KEY_ACTIVE_THEME, theme).apply()
    }

    fun getActiveTheme(context: Context): String {
        return prefs(context).getString(KEY_ACTIVE_THEME, "midnight") ?: "midnight"
    }

    fun saveWallpaperSet(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_WALLPAPER_SET, value).apply()
    }

    fun isWallpaperSet(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_WALLPAPER_SET, false)
    }

    fun saveWallpaperOptedOut(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_WALLPAPER_OPTED_OUT, value).apply()
    }

    fun isWallpaperOptedOut(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_WALLPAPER_OPTED_OUT, false)
    }

    fun saveCompetitiveModeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_COMPETITIVE_MODE, enabled).apply()
    }

    fun isCompetitiveModeEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_COMPETITIVE_MODE, false)
    }

    fun saveWanderDate(context: Context, date: String) {
        prefs(context).edit().putString(KEY_WANDER_DATE, date).apply()
    }

    fun getWanderDate(context: Context): String {
        return prefs(context).getString(KEY_WANDER_DATE, "") ?: ""
    }

    fun saveWanderWordIds(context: Context, ids: String) {
        prefs(context).edit().putString(KEY_WANDER_WORD_IDS, ids).apply()
    }

    fun getWanderWordIds(context: Context): String {
        return prefs(context).getString(KEY_WANDER_WORD_IDS, "") ?: ""
    }

    fun saveWanderSeenAllIds(context: Context, ids: String) {
        prefs(context).edit().putString(KEY_WANDER_SEEN_ALL_IDS, ids).apply()
    }

    fun getWanderSeenAllIds(context: Context): String {
        return prefs(context).getString(KEY_WANDER_SEEN_ALL_IDS, "") ?: ""
    }

    fun saveDailyPoolDate(context: Context, date: String) {
        prefs(context).edit().putString(KEY_DAILY_POOL_DATE, date).apply()
    }

    fun getDailyPoolDate(context: Context): String {
        return prefs(context).getString(KEY_DAILY_POOL_DATE, "") ?: ""
    }

    fun saveDailyPoolWordIds(context: Context, ids: String) {
        prefs(context).edit().putString(KEY_DAILY_POOL_WORD_IDS, ids).apply()
    }

    fun getDailyPoolWordIds(context: Context): String {
        return prefs(context).getString(KEY_DAILY_POOL_WORD_IDS, "") ?: ""
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
