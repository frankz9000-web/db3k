package com.db3k.diariodibordo.messaging

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.network.ApiService
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Lifecycle observer to handle FCM token registration
 */
class FCMLifecycleObserver @Inject constructor(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val apiService: ApiService
) : DefaultLifecycleObserver {

    private val TAG = "FCMLifecycleObserver"

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        // Check and register FCM token when app comes to foreground
        checkAndRegisterToken()
    }

    /**
     * Check if we have a valid FCM token and register it with backend
     */
    private fun checkAndRegisterToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get current token from preferences
                val savedToken = userPreferences.fcmToken.firstOrNull()

                // Get fresh token from FCM
                val currentToken = FirebaseMessaging.getInstance().token.await()

                // Register if token changed or not registered yet
                if (savedToken != currentToken) {
                    registerTokenWithBackend(currentToken)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking FCM token", e)
            }
        }
    }

    /**
     * Register FCM token with backend API
     */
    private fun registerTokenWithBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RegisterTokenRequest(token = token)
                val response = apiService.registerPushToken(request).await()

                if (response.isSuccessful && response.body()?.ok == true) {
                    // Save token locally
                    userPreferences.setFcmToken(token)
                    Log.d(TAG, "FCM token registered successfully")
                } else {
                    Log.w(TAG, "Failed to register FCM token: ${response.body()?.error ?: response.message()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }
}