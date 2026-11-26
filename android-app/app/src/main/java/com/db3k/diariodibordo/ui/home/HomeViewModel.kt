package com.db3k.diariodibordo.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.db3k.diariodibordo.data.model.Post
import com.db3k.diariodibordo.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel for HomeFragment
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _latestPosts = MutableLiveData<List<Post>>()
    val latestPosts: LiveData<List<Post>> = _latestPosts

    private val _upcomingEvents = MutableLiveData<List<Post>>()
    val upcomingEvents: LiveData<List<Post>> = _upcomingEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Load latest posts from local DB
            postRepository.getLatestPosts(10)
                .collect { posts ->
                    _latestPosts.postValue(posts)
                    _isLoading.postValue(false)
                }
                .invokeOnCompletion { cause ->
                    if (cause != null) {
                        _error.postValue("Errore caricamento post: ${cause.message}")
                        _isLoading.postValue(false)
                    }
                }

            // Load upcoming events
            postRepository.getLatestPosts(20)
                .collect { posts ->
                    val events = posts.filter { it.isEvent() }
                    _upcomingEvents.postValue(events)
                }
        }
    }

    /** Refresh posts from API */
    fun refreshPosts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = postRepository.syncPosts(forceRefresh = true)
            if (result.isFailure) {
                _error.postValue(result.exceptionOrNull()?.message ?: "Errore sincronizzazione")
            }
            _isLoading.value = false
            loadData() // Reload from local DB
        }
    }
}