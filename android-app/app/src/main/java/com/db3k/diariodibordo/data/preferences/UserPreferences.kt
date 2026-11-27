package com.db3k.diariodibordo.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * User preferences using SharedPreferences
 */
class UserPreferences private constructor(
    private val prefs: SharedPreferences
) {
    companion object {
        // Keys
        private val PUSH_ENABLED = "push_enabled"
        private val PUSH_NEW_POSTS = "push_new_posts"
        private val PUSH_RACE_RESULTS = "push_race_results"
        private val PUSH_EVENTS = "push_events"
        private val PUSH_SOUND = "push_sound"
        private val PUSH_VIBRATION = "push_vibration"
        private val FCM_TOKEN = "fcm_token"
        private val LAST_SYNC = "last_sync"
        private val OFFLINE_MODE = "offline_mode"
        private val THEME_MODE = "theme_mode" // 0=system, 1=light, 2=dark
        private val FIRST_LAUNCH = "first_launch"

        fun create(context: Context): UserPreferences {
            val prefs = context.getSharedPreferences("db3k_prefs", Context.MODE_PRIVATE)
            return UserPreferences(prefs)
        }
    }

    // Push Notifications
    val pushEnabled: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_ENABLED, true)) }

    fun setPushEnabled(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_ENABLED, enabled)
        editor.apply()
    }

    val pushNewPosts: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_NEW_POSTS, true)) }

    fun setPushNewPosts(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_NEW_POSTS, enabled)
        editor.apply()
    }

    val pushRaceResults: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_RACE_RESULTS, true)) }

    fun setPushRaceResults(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_RACE_RESULTS, enabled)
        editor.apply()
    }

    val pushEvents: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_EVENTS, true)) }

    fun setPushEvents(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_EVENTS, enabled)
        editor.apply()
    }

    val pushSound: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_SOUND, true)) }

    fun setPushSound(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_SOUND, enabled)
        editor.apply()
    }

    val pushVibration: Flow<Boolean> = flow { emit(prefs.getBoolean(PUSH_VIBRATION, true)) }

    fun setPushVibration(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(PUSH_VIBRATION, enabled)
        editor.apply()
    }

    // FCM Token
    val fcmToken: Flow<String?> = flow { emit(prefs.getString(FCM_TOKEN, null)) }

    suspend fun setFcmToken(token: String?) {
        val editor = prefs.edit()
        editor.putString(FCM_TOKEN, token)
        editor.apply()
    }

    // Sync
    val lastSync: Flow<Long> = flow { emit(prefs.getLong(LAST_SYNC, 0L)) }

    suspend fun setLastSync(timestamp: Long) {
        val editor = prefs.edit()
        editor.putLong(LAST_SYNC, timestamp)
        editor.apply()
    }

    // Offline Mode
    val offlineMode: Flow<Boolean> = flow { emit(prefs.getBoolean(OFFLINE_MODE, false)) }

    fun setOfflineMode(enabled: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(OFFLINE_MODE, enabled)
        editor.apply()
    }

    // Theme
    val themeMode: Flow<Int> = flow { emit(prefs.getInt(THEME_MODE, 0)) }

    fun setThemeMode(mode: Int) {
        val editor = prefs.edit()
        editor.putInt(THEME_MODE, mode)
        editor.apply()
    }

    // First Launch
    val isFirstLaunch: Flow<Boolean> = flow { emit(prefs.getBoolean(FIRST_LAUNCH, true)) }

    fun setFirstLaunchComplete() {
        val editor = prefs.edit()
        editor.putBoolean(FIRST_LAUNCH, false)
        editor.apply()
    }

    // App Version
    fun getAppVersion(): String = "1.0.0"
}