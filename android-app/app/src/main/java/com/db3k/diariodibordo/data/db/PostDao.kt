package com.db3k.diariodibordo.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow
import com.db3k.diariodibordo.data.model.Post

/**
 * DAO for Post entity
 */
@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<Post>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long

    @Query("SELECT * FROM posts ORDER BY date DESC")
    fun getAllPosts(): Flow<List<Post>>

    @Query("SELECT * FROM posts ORDER BY date DESC LIMIT :limit")
    fun getLatestPosts(limit: Int): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE slug = :slug")
    suspend fun getPostBySlug(slug: String): Post?

    @Query("SELECT * FROM posts WHERE slug = :slug")
    fun getPostBySlugFlow(slug: String): Flow<Post?>

    @Query("SELECT * FROM posts WHERE category = :category ORDER BY date DESC")
    fun getPostsByCategory(category: String): Flow<List<Post>>

    @Query("SELECT * FROM posts WHERE isRead = 0 ORDER BY date DESC")
    fun getUnreadPosts(): Flow<List<Post>>

    @Query("UPDATE posts SET isRead = 1 WHERE slug = :slug")
    suspend fun markAsRead(slug: String): Int

    @Query("UPDATE posts SET isRead = 1")
    suspend fun markAllAsRead(): Int

    @Query("DELETE FROM posts WHERE savedAt < :cutoffTime")
    suspend fun deleteOldPosts(cutoffTime: Long): Int

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int

    @Query("SELECT COUNT(*) FROM posts WHERE isRead = 0")
    suspend fun getUnreadCount(): Int

    @Delete
    suspend fun deletePost(post: Post)
}