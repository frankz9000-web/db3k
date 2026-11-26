package com.db3k.diariodibordo.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.rx.preferencesDataStore
import androidx.datastore.rx.RxDataStore
import com.db3k.diariodibordo.BuildConfig
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * User preferences using DataStore
 */
class UserPreferences private constructor(
    private val dataStore: RxDataStore<Preferences>
) {
    companion object {
        // Keys
        private val PUSH_ENABLED = booleanPreferencesKey("push_enabled")
        private val PUSH_NEW_POSTS = booleanPreferencesKey("push_new_posts")
        private val PUSH_RACE_RESULTS = booleanPreferencesKey("push_race_results")
        private val PUSH_EVENTS = booleanPreferencesKey("push_events")
        private val PUSH_SOUND = booleanPreferencesKey("push_sound")
        private val PUSH_VIBRATION = booleanPreferencesKey("push_vibration")
        private val FCM_TOKEN = stringPreferencesKey("fcm_token")
        private val LAST_SYNC = longPreferencesKey("last_sync")
        private val OFFLINE_MODE = booleanPreferencesKey("offline_mode")
        private val THEME_MODE = intPreferencesKey("theme_mode") // 0=system, 1=light, 2=dark
        private val FIRST_LAUNCH = booleanPreferencesKey("first_launch")

        @Suppress("UNUSED_PARAMETER")
        private fun <T> Preferences.Key<T> = PreferencesKeys.key<T>()

        fun create(context: Context): UserPreferences {
            val dataStore = context.preferencesDataStore(name = "db3k_prefs")
            return UserPreferences(dataStore)
        }
    }

    // Push Notifications
    val pushEnabled: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_ENABLED] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushEnabled(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_ENABLED] = enabled }

    val pushNewPosts: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_NEW_POSTS] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushNewPosts(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_NEW_POSTS] = enabled }

    val pushRaceResults: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_RACE_RESULTS] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushRaceResults(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_RACE_RESULTS] = enabled }

    val pushEvents: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_EVENTS] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushEvents(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_EVENTS] = enabled }

    val pushSound: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_SOUND] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushSound(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_SOUND] = enabled }

    val pushVibration: Flowable<Boolean> = dataStore.data
        .map { it[PUSH_VIBRATION] ?: true }
        .subscribeOn(Schedulers.io())

    fun setPushVibration(enabled: Boolean) = dataStore.updateDataAsync { it[PUSH_VIBRATION] = enabled }

    // FCM Token
    val fcmToken: Flowable<String?> = dataStore.data
        .map { it[FCM_TOKEN] }
        .subscribeOn(Schedulers.io())

    suspend fun setFcmToken(token: String?) = dataStore.updateData { it[FCM_TOKEN] = token }

    // Sync
    val lastSync: Flowable<Long> = dataStore.data
        .map { it[LAST_SYNC] ?: 0L }
        .subscribeOn(Schedulers.io())

    suspend fun setLastSync(timestamp: Long) = dataStore.updateData { it[LAST_SYNC] = timestamp }

    // Offline Mode
    val offlineMode: Flowable<Boolean> = dataStore.data
        .map { it[OFFLINE_MODE] ?: false }
        .subscribeOn(Schedulers.io())

    fun setOfflineMode(enabled: Boolean) = dataStore.updateDataAsync { it[OFFLINE_MODE] = enabled }

    // Theme
    val themeMode: Flowable<Int> = dataStore.data
        .map { it[THEME_MODE] ?: 0 }
        .subscribeOn(Schedulers.io())

    fun setThemeMode(mode: Int) = dataStore.updateDataAsync { it[THEME_MODE] = mode }

    // First Launch
    val isFirstLaunch: Flowable<Boolean> = dataStore.data
        .map { it[FIRST_LAUNCH] ?: true }
        .subscribeOn(Schedulers.io())

    fun setFirstLaunchComplete() = dataStore.updateDataAsync { it[FIRST_LAUNCH] = false }

    // App Version
    fun getAppVersion(): String = BuildConfig.VERSION_NAME
}