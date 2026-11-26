package com.db3k.diariodibordo.data.repository

import com.db3k.diariodibordo.data.db.NotificationDao
import com.db3k.diariodibordo.data.model.Notification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repository for notifications
 */
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao
) {

    /** Get all notifications */
    fun getAllNotifications(): Flow<List<Notification>> = notificationDao.getAllNotifications()

    /** Get latest notifications for home screen */
    fun getLatestNotifications(limit: Int = 20): Flow<List<Notification>> = notificationDao.getLatestNotifications(limit)

    /** Get unread notifications */
    fun getUnreadNotifications(): Flow<List<Notification>> = notificationDao.getUnreadNotifications()

    /** Get unread count */
    suspend fun getUnreadCount(): Int = notificationDao.getUnreadCount()

    /** Save notification from push */
    suspend fun saveNotification(notification: Notification): Long = notificationDao.insertNotification(notification)

    /** Mark notification as read */
    suspend fun markAsRead(id: Long) = notificationDao.markAsRead(id)

    /** Mark all as read */
    suspend fun markAllAsRead() = notificationDao.markAllAsRead()

    /** Delete notification */
    suspend fun deleteNotification(id: Long) = notificationDao.deleteNotification(id)

    /** Clean old notifications (older than 30 days) */
    suspend fun cleanOldNotifications(): Int {
        val cutoffTime = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return notificationDao.deleteOldNotifications(cutoffTime)
    }
}