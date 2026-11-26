package com.db3k.diariodibordo.messaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager

/**
 * Broadcast receiver to handle boot completion and reschedule periodic work
 */
class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BootReceiver: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            // Reschedule token refresh worker
            TokenRefreshWorker.scheduleWork()
            Log.d(TAG, "Token refresh worker scheduled")
        }
    }
}