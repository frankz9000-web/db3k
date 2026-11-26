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
 * ViewModel for PostDetailFragment
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _post = MutableLiveData<Post?>()
    val post: LiveData<Post?> = _post

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    /** Load post by slug */
    fun loadPost(slug: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            postRepository.getPostBySlug(slug)
                .collect { post ->
                    _post.postValue(post)
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
}