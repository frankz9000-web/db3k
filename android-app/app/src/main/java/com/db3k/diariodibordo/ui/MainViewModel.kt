package com.db3k.diariodibordo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.db3k.diariodibordo.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * Main ViewModel for shared state
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    /** Trigger a full refresh of posts from API */
    fun refreshPosts() {
        viewModelScope.launch {
            postRepository.syncPosts(forceRefresh = true)
        }
    }

    /** Clean old data */
    fun cleanOldData() {
        viewModelScope.launch {
            postRepository.cleanOldPosts()
        }
    }
}