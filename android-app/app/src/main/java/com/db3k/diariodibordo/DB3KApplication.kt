package com.db3k.diariodibordo

import android.app.Application
import android.content.Context
import androidx.lifecycle.ProcessLifecycleOwner
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.messaging.FCMLifecycleObserver
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Main Application class with Hilt integration
 */
@HiltAndroidApp
class DB3KApplication : Application() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Register FCM lifecycle observer for token management
        ProcessLifecycleOwner.get().lifecycle.addObserver(FCMLifecycleObserver(this))

        // Initialize preferences
        userPreferences.isFirstLaunch.subscribe { isFirst ->
            if (isFirst) {
                // Set defaults for first launch
                userPreferences.setPushEnabled(true)
                userPreferences.setPushNewPosts(true)
                userPreferences.setPushRaceResults(true)
                userPreferences.setPushEvents(true)
                userPreferences.setPushSound(true)
                userPreferences.setPushVibration(true)
                userPreferences.setFirstLaunchComplete()
            }
        }
    }

    companion object {
        lateinit var instance: DB3KApplication
            private set

        fun getInstance(): DB3KApplication = instance

        fun getContext(): Context = instance.applicationContext
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }
}