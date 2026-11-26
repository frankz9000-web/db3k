package com.db3k.diariodibordo.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.db3k.diariodibordo.data.model.Notification
import com.db3k.diariodibordo.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel for NotificationsFragment
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            notificationRepository.getAllNotifications()
                .collect { notifications ->
                    _notifications.postValue(notifications)
                    _isLoading.postValue(false)
                }
                .invokeOnCompletion { cause ->
                    if (cause != null) {
                        _error.postValue("Errore caricamento notifiche: ${cause.message}")
                        _isLoading.postValue(false)
                    }
                }

            notificationRepository.getUnreadCount()
                .collect { count ->
                    _unreadCount.postValue(count)
                }
        }
    }

    /** Refresh notifications */
    fun refreshNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            loadNotifications()
        }
    }

    /** Mark notification as read */
    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
            loadNotifications()
        }
    }

    /** Mark all as read */
    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadNotifications()
        }
    }

    /** Delete notification */
    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
            loadNotifications()
        }
    }

    /** Clear all notifications */
    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.getAllNotifications().first().forEach { notification ->
                notificationRepository.deleteNotification(notification.id)
            }
            loadNotifications()
        }
    }
}