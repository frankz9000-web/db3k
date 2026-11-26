package com.db3k.diariodibordo.messaging

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.network.ApiService
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Periodic worker to refresh FCM token and keep it registered with backend
 */
@AndroidEntryPoint
class TokenRefreshWorker @Inject constructor(
    context: Context,
    params: WorkerParameters,
    private val userPreferences: UserPreferences,
    private val apiService: ApiService
) : CoroutineWorker(context, params) {

    private val TAG = "TokenRefreshWorker"

    override suspend fun doWork(): Result {
        return try {
            // Get fresh token from FCM
            val token = FirebaseMessaging.getInstance().token.await()

            // Register with backend
            val request = RegisterTokenRequest(token = token)
            val response = apiService.registerPushToken(request).await()

            if (response.isSuccessful && response.body()?.ok == true) {
                // Update local token
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