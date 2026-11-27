package com.db3k.diariodibordo

import android.app.Application
import android.content.Context
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.network.ApiService
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Main Application class with Hilt integration
 */
@HiltAndroidApp
class DB3KApplication : Application() {

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize preferences - set defaults on first launch
        runBlocking {
            val isFirst = userPreferences.isFirstLaunch.first()
            if (isFirst) {
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

        fun getContext(): Context = instance.applicationContext

        fun getUserPreferences(): UserPreferences = instance.userPreferences

        fun getApiService(): ApiService = instance.apiService
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        instance = this
    }
}