package com.db3k.diariodibordo.ui.posts

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
 * ViewModel for PostsFragment
 */
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val _categories = MutableLiveData<List<String>>()
    val categories: LiveData<List<String>> = _categories

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentCategory: String? = null
    private var currentQuery: String = ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Load all posts for categories
            postRepository.getAllPosts()
                .collect { allPosts ->
                    // Extract unique categories
                    val cats = allPosts
                        .mapNotNull { it.category }
                        .distinct()
                        .sorted()
                    _categories.postValue(cats)

                    // Apply current filter
                    applyFilters(allPosts)
                    _isLoading.postValue(false)
                }
                .invokeOnCompletion { cause ->
                    if (cause != null) {
                        _error.postValue("Errore caricamento post: ${cause.message}")
                        _isLoading.postValue(false)
                    }
                }
        }
    }

    private fun applyFilters(allPosts: List<Post>) {
        var filtered = allPosts

        // Category filter
        currentCategory?.let { cat ->
            filtered = filtered.filter { it.category == cat }
        }

        // Search query filter
        if (currentQuery.isNotBlank()) {
            val query = currentQuery.lowercase()
            filtered = filtered.filter { post ->
                post.title.lowercase().contains(query) ||
                post.excerpt?.lowercase().contains(query) == true ||
                post.content?.lowercase().contains(query) == true ||
                post.tags.any { it.lowercase().contains(query) }
            }
        }

        _posts.postValue(filtered)
    }

    /** Filter by category */
    fun filterByCategory(category: String?) {
        currentCategory = category
        viewModelScope.launch {
            postRepository.getAllPosts().first().let { allPosts ->
                applyFilters(allPosts)
            }
        }
    }

    /** Search posts */
    fun searchPosts(query: String) {
        currentQuery = query
        viewModelScope.launch {
            postRepository.getAllPosts().first().let { allPosts ->
                applyFilters(allPosts)
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
            loadData()
        }
    }
}