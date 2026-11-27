package com.db3k.diariodibordo.messaging

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.network.ApiService
import com.db3k.diariodibordo.network.RegisterTokenRequest
import com.db3k.diariodibordo.DB3KApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Periodic worker to refresh FCM token and keep it registered with backend
 */
class TokenRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "TokenRefreshWorker"

    private val userPreferences: UserPreferences
        get() = DB3KApplication.getUserPreferences()

    private val apiService: ApiService
        get() = DB3KApplication.getApiService()

    override suspend fun doWork(): Result {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            val request = RegisterTokenRequest(token = token)
            val response = apiService.registerPushToken(request)

            if (response.isSuccessful && response.body()?.ok == true) {
                userPreferences.setFcmToken(token)
                Log.d(TAG, "Token refreshed successfully")
                Result.success()
            } else {
                Log.w(TAG, "Failed to refresh token: ${response.body()?.error ?: response.message()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "token_refresh_work"
        const val INTERVAL_HOURS = 24L

        fun scheduleWork() {
            val workRequest = PeriodicWorkRequest.Builder(
                TokenRefreshWorker::class.java,
                INTERVAL_HOURS,
                TimeUnit.HOURS
            )
                .setInitialDelay(1, TimeUnit.HOURS)
                .build()

            WorkManager.getInstance().enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelWork() {
            WorkManager.getInstance().cancelUniqueWork(WORK_NAME)
        }
    }
}