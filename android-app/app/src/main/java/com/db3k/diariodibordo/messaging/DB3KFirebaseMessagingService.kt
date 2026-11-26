package com.db3k.diariodibordo.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.db3k.diariodibordo.R
import com.db3k.diariodibordo.data.model.Notification
import com.db3k.diariodibordo.data.model.NotificationType
import com.db3k.diariodibordo.data.repository.NotificationRepository
import com.db3k.diariodibordo.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Firebase Messaging Service for handling push notifications
 * Ducati-style notifications with custom channels, colors, and actions
 */
@AndroidEntryPoint
class DB3KFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "DB3KFirebaseMsg"

    @Inject
    lateinit var notificationRepository: NotificationRepository
    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        // Save token and register with backend
        userPreferences.setFcmToken(token)
        // Register with backend will be handled by FCMLifecycleObserver
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message received: ${remoteMessage.from}")

        // Check if push is enabled
        val pushEnabled = userPreferences.pushEnabled.firstOrNull() ?: true
        if (!pushEnabled) {
            Log.d(TAG, "Push notifications disabled, ignoring message")
            return
        }

        // Handle notification payload
        val notification = remoteMessage.notification
        val data = remoteMessage.data

        if (notification != null) {
            val title = notification.title ?: "DB3K Blog"
            val body = notification.body ?: ""
            val url = data["url"] ?: data["link"] ?: ""
            val typeStr = data["type"] ?: "generic"

            // Parse notification type
            val type = when (typeStr) {
                "new_post" -> NotificationType.NEW_POST
                "race_results" -> NotificationType.RACE_RESULTS
                "event" -> NotificationType.EVENT
                else -> NotificationType.GENERIC
            }

            // Check if this type is enabled
            val typeEnabled = when (type) {
                NotificationType.NEW_POST -> userPreferences.pushNewPosts.firstOrNull() ?: true
                NotificationType.RACE_RESULTS -> userPreferences.pushRaceResults.firstOrNull() ?: true
                NotificationType.EVENT -> userPreferences.pushEvents.firstOrNull() ?: true
                else -> true
            }

            if (!typeEnabled) {
                Log.d(TAG, "Notification type $type disabled, ignoring")
                return
            }

            // Save to local database
            val notif = Notification(
                title = title,
                body = body,
                url = url,
                type = type
            )
            notificationRepository.saveNotification(notif)

            // Show system notification
            showNotification(title, body, url, type)
        }

        // Handle data-only messages
        if (data.isNotEmpty()) {
            Log.d(TAG, "Data message: $data")
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "Deleted messages on server")
    }

    /**
     * Show a Ducati-style system notification
     */
    private fun showNotification(
        title: String,
        body: String,
        url: String,
        type: NotificationType
    ) {
        val channelId = getChannelId(type)
        createNotificationChannel(channelId, type)

        // Create intent for notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("notification_url", url)
            putExtra("notification_type", type.value)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification with Ducati styling
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification_ducati)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, type.getAccentColorRes()))
            .setColorized(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        // Add action buttons for Ducati style
        addNotificationActions(builder, url, type)

        // Sound and vibration
        val soundEnabled = userPreferences.pushSound.firstOrNull() ?: true
        val vibrateEnabled = userPreferences.pushVibration.firstOrNull() ?: true

        if (soundEnabled) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(soundUri)
        }

        if (vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 200, 100, 200)) // Ducati heartbeat pattern
        }

        // Large icon (Ducati shield)
        builder.setLargeIcon(
            androidx.core.graphics.drawable.IconCompat.createWithResource(this, R.drawable.ic_notification_ducati)
                .toBitmap(this)
        )

        // Show notification
        val notificationId = (System.currentTimeMillis() % Integer.MAX_VALUE).toInt()
        NotificationManagerCompat.from(this).notify(notificationId, builder.build())

        Log.d(TAG, "Notification shown: $title")
    }

    /**
     * Get notification channel ID for type
     */
    private fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.NEW_POST -> "db3k_new_post"
            NotificationType.RACE_RESULTS -> "db3k_race_results"
            NotificationType.EVENT -> "db3k_event"
            else -> "db3k_generic"
        }
    }

    /**
     * Create notification channel with Ducati styling
     */
    private fun createNotificationChannel(channelId: String, type: NotificationType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(channelId) == null) {
                val name = when (type) {
                    NotificationType.NEW_POST -> "Nuovi Post"
                    NotificationType.RACE_RESULTS -> "Risultati Gare"
                    NotificationType.EVENT -> "Eventi"
                    else -> "Generale"
                }

                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(channelId, name, importance).apply {
                    description = "Notifiche $name stile Ducati"
                    enableLights(true)
                    lightColor = ContextCompat.getColor(this@DB3KFirebaseMessagingService, type.getAccentColorRes())
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 200, 100, 200)
                    setShowBadge(true)
                    lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                }

                manager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Add action buttons to notification
     */
    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        url: String,
        type: NotificationType
    ) {
        // Open action
        val openIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra("notification_url", url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this, 1, openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_action_open,
                "Apri",
                openPendingIntent
            ).build()
        )

        // Share action
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$url\n\nDa DB3K Blog")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val sharePendingIntent = PendingIntent.getActivity(
            this, 2, Intent.createChooser(shareIntent, "Condividi"),
            PendingIntent.FLAG_IMMUTABLE
        )

        builder.addAction(
            NotificationCompat.Action.Builder(
                R.drawable.ic_action_share,
                "Condividi",
                sharePendingIntent
            ).build()
        )
    }
}

/**
 * Extension to get accent color resource for notification type
 */
private fun NotificationType.getAccentColorRes(): Int {
    return when (this) {
        NotificationType.NEW_POST -> R.color.notification_channel_new_post
        NotificationType.RACE_RESULTS -> R.color.notification_channel_race
        NotificationType.EVENT -> R.color.notification_channel_event
        else -> R.color.ducati_red
    }
}