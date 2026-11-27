package com.db3k.diariodibordo.messaging

import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.db3k.diariodibordo.data.preferences.UserPreferences
import com.db3k.diariodibordo.network.ApiService
import com.db3k.diariodibordo.network.RegisterTokenRequest
import com.db3k.diariodibordo.network.RegisterTokenResponse
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
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
        checkAndRegisterToken()
    }

    private fun checkAndRegisterToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val savedToken = userPreferences.fcmToken.first()

                FirebaseMessaging.getInstance().token.addOnSuccessListener { currentToken ->
                    CoroutineScope(Dispatchers.IO).launch {
                        if (savedToken != currentToken) {
                            registerTokenWithBackend(currentToken)
                        }
                    }
                }.addOnFailureListener { e ->
                    Log.e(TAG, "Error getting FCM token", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking FCM token", e)
            }
        }
    }

    private fun registerTokenWithBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = RegisterTokenRequest(token = token)
                val response = apiService.registerPushToken(request)

                if (response.isSuccessful && response.body()?.ok == true) {
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