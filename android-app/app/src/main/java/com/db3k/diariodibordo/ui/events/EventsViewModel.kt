package com.db3k.diariodibordo.ui.events

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
 * ViewModel for EventsFragment
 */
@HiltViewModel
class EventsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _events = MutableLiveData<List<Post>>()
    val events: LiveData<List<Post>> = _events

    private val _pastEvents = MutableLiveData<List<Post>>()
    val pastEvents: LiveData<List<Post>> = _pastEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadEvents()
    }

    private fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            postRepository.getAllPosts()
                .collect { allPosts ->
                    val now = System.currentTimeMillis()
                    val upcoming = allPosts.filter { it.isEvent() }
                        .filter { post ->
                            try {
                                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(post.date)
                                date?.time ?: 0 > now
                            } catch (e: Exception) {
                                true // Include if date parsing fails
                            }
                        }
                        .sortedBy { it.date }

                    val past = allPosts.filter { it.isEvent() }
                        .filter { post ->
                            try {
                                val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).parse(post.date)
                                date?.time ?: 0 <= now
                            } catch (e: Exception) {
                                false
                            }
                        }
                        .sortedByDescending { it.date }

                    _events.postValue(upcoming)
                    _pastEvents.postValue(past)
                    _isLoading.postValue(false)
                }
                .invokeOnCompletion { cause ->
                    if (cause != null) {
                        _error.postValue("Errore caricamento eventi: ${cause.message}")
                        _isLoading.postValue(false)
                    }
                }
        }
    }

    /** Refresh events from API */
    fun refreshEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = postRepository.syncPosts(forceRefresh = true)
            if (result.isFailure) {
                _error.postValue(result.exceptionOrNull()?.message ?: "Errore sincronizzazione")
            }
            _isLoading.value = false
            loadEvents()
        }
    }
}