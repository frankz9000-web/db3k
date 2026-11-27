package com.db3k.diariodibordo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.db3k.diariodibordo.data.model.Notification

/**
 * DAO for Notification entity
 */
@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<Notification>)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestNotifications(limit: Int): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<Notification>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    suspend fun getNotificationById(id: Long): Notification?

    @Update
    suspend fun updateNotification(notification: Notification): Int

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long): Int

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead(): Int

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: Long): Int

    @Query("DELETE FROM notifications WHERE timestamp < :cutoffTime")
    suspend fun deleteOldNotifications(cutoffTime: Long): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    suspend fun getUnreadCount(): Int

    @Delete
    suspend fun deleteNotification(notification: Notification)
}