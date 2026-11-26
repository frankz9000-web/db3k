package com.db3k.diariodibordo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/**
 * Push notification model for local storage
 */
@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @Json(name = "title")
    val title: String,

    @Json(name = "body")
    val body: String,

    @Json(name = "url")
    val url: String?,

    @Json(name = "type")
    val type: NotificationType = NotificationType.GENERIC,

    @Json(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @Json(name = "isRead")
    val isRead: Boolean = false,

    @Json(name = "postSlug")
    val postSlug: String? = null
) {
    /** Get formatted timestamp */
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60000 -> "Ora"
            diff < 3600000 -> "${diff / 60000} min fa"
            diff < 86400000 -> "${diff / 3600000} ore fa"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.ITALIAN)
                sdf.format(date)
            }
        }
    }

    /** Get icon for notification type */
    fun getIconRes(): Int {
        return when (type) {
            NotificationType.NEW_POST -> R.drawable.ic_notification_new_post
            NotificationType.RACE_RESULTS -> R.drawable.ic_notification_race
            NotificationType.EVENT -> R.drawable.ic_notification_event
            else -> R.drawable.ic_notification_generic
        }
    }

    /** Get accent color for notification type */
    fun getAccentColor(): Int {
        return when (type) {
            NotificationType.NEW_POST -> R.color.notification_channel_new_post
            NotificationType.RACE_RESULTS -> R.color.notification_channel_race
            NotificationType.EVENT -> R.color.notification_channel_event
            else -> R.color.ducati_red
        }
    }
}

/** Notification types matching backend templates */
enum class NotificationType(@Json(name = "value") val value: String) {
    @Json(name = "new_post") NEW_POST("new_post"),
    @Json(name = "race_results") RACE_RESULTS("race_results"),
    @Json(name = "event") EVENT("event"),
    @Json(name = "generic") GENERIC("generic")
}