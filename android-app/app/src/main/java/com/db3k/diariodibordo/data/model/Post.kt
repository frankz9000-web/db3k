package com.db3k.diariodibordo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

/**
 * Blog post model - matches the backend API structure (api/posts.json)
 */
@Entity(tableName = "posts")
data class Post(
    @PrimaryKey
    @Json(name = "slug")
    val slug: String = "",

    @Json(name = "title")
    val title: String = "",

    @Json(name = "excerpt")
    val excerpt: String? = null,

    @Json(name = "content")
    val content: String? = null,

    @Json(name = "pubDate")
    val date: String = "",

    @Json(name = "author")
    val author: String? = null,

    @Json(name = "category")
    val category: String? = null,

    @Json(name = "tags")
    val tags: List<String> = emptyList(),

    @Json(name = "coverImage")
    val coverImage: String? = null,

    @Json(name = "images")
    val images: List<String> = emptyList(),

    @Json(name = "url")
    val url: String? = null,

    // Local fields
    @Json(name = "isRead")
    val isRead: Boolean = false,

    @Json(name = "savedAt")
    val savedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun createEmpty(slug: String): Post = Post(
            slug = slug,
            title = "",
            excerpt = null,
            content = null,
            date = "",
            author = null,
            category = null,
            coverImage = null,
            url = null
        )

        /** Generate a slug from a title (fallback when API doesn't provide one) */
        fun slugify(title: String): String {
            return title.lowercase()
                .replace(Regex("[^a-z0-9\\s-]"), "")
                .trim()
                .replace(Regex("\\s+"), "-")
                .take(80)
                .ifBlank { "post-${System.currentTimeMillis()}" }
        }
    }

    /** Format date for display */
    fun getFormattedDate(): String {
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd",
                "dd/MM/yyyy"
            )
            for (format in formats) {
                try {
                    val sdf = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                    val dateObj = sdf.parse(date)
                    val outFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.ITALIAN)
                    return outFormat.format(dateObj!!)
                } catch (e: Exception) {
                    // Try next format
                }
            }
            date // fallback
        } catch (e: Exception) {
            date
        }
    }

    /** Get short date for notifications */
    fun getShortDate(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            val dateObj = sdf.parse(date)
            val outFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale.ITALIAN)
            outFormat.format(dateObj!!)
        } catch (e: Exception) {
            ""
        }
    }

    /** Check if post is a race result */
    fun isRaceResult(): Boolean {
        val lowerTitle = title.lowercase()
        val lowerCategory = category?.lowercase() ?: ""
        return lowerTitle.contains("risultati") || lowerTitle.contains("gara") ||
               lowerTitle.contains("race") || lowerCategory.contains("gara") ||
               lowerCategory.contains("race")
    }

    /** Check if post is an event */
    fun isEvent(): Boolean {
        val lowerTitle = title.lowercase()
        val lowerCategory = category?.lowercase() ?: ""
        return lowerTitle.contains("evento") || lowerTitle.contains("uscita") ||
               lowerTitle.contains("event") || lowerCategory.contains("evento") ||
               lowerCategory.contains("event")
    }
}