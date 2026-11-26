package com.db3k.diariodibordo.data.repository

import com.db3k.diariodibordo.data.db.PostDao
import com.db3k.diariodibordo.data.model.Post
import com.db3k.diariodibordo.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Repository for posts - handles both local and remote data
 */
class PostRepository @Inject constructor(
    private val postDao: PostDao,
    private val apiService: ApiService
) {

    /** Get all posts from local database (with offline support) */
    fun getAllPosts(): Flow<List<Post>> = postDao.getAllPosts()

    /** Get latest posts for home screen */
    fun getLatestPosts(limit: Int = 10): Flow<List<Post>> = postDao.getLatestPosts(limit)

    /** Get post by slug */
    fun getPostBySlug(slug: String): Flow<Post?> = postDao.getPostBySlugFlow(slug)

    /** Get posts by category */
    fun getPostsByCategory(category: String): Flow<List<Post>> = postDao.getPostsByCategory(category)

    /** Get unread posts count */
    suspend fun getUnreadCount(): Int = postDao.getUnreadCount()

    /** Mark post as read */
    suspend fun markAsRead(slug: String) = postDao.markAsRead(slug)

    /** Fetch latest posts from API and save to local DB */
    suspend fun syncPosts(forceRefresh: Boolean = false): Result<Int> {
        return try {
            val response = apiService.getPosts()
            if (!response.isSuccessful) {
                return Result.failure(Exception("API Error: ${response.code()}"))
            }

            val posts = response.body()?.posts ?: emptyList()
            if (posts.isNotEmpty()) {
                postDao.insertPosts(posts)
            }

            Result.success(posts.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Search posts locally */
    fun searchPosts(query: String): Flow<List<Post>> = postDao.getAllPosts()
        .map { posts ->
            if (query.isBlank()) posts
            else posts.filter { post ->
                post.title.contains(query, ignoreCase = true) ||
                post.excerpt?.contains(query, ignoreCase = true) == true ||
                post.content?.contains(query, ignoreCase = true) == true ||
                post.tags.any { it.contains(query, ignoreCase = true) }
            }
        }

    /** Clean old posts (older than 90 days) */
    suspend fun cleanOldPosts(): Int {
        val cutoffTime = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        return postDao.deleteOldPosts(cutoffTime)
    }
}