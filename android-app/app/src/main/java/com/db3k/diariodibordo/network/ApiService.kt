package com.db3k.diariodibordo.network

import com.db3k.diariodibordo.data.model.Post
import com.squareup.moshi.Json
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit API service for DB3K Blog backend
 */
interface ApiService {

    @GET("api/posts")
    suspend fun getPosts(): Response<PostsResponse>

    @GET("api/posts/{slug}")
    suspend fun getPost(@Path("slug") slug: String): Response<PostResponse>

    @POST("api/push/register")
    suspend fun registerPushToken(@Body body: RegisterTokenRequest): Response<RegisterTokenResponse>

    @POST("api/push/unregister")
    suspend fun unregisterPushToken(@Body body: UnregisterTokenRequest): Response<UnregisterTokenResponse>
}

/** Response wrapper for posts list */
data class PostsResponse(
    @Json(name = "posts") val posts: List<Post>,
    @Json(name = "total") val total: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "perPage") val perPage: Int
)

/** Response wrapper for single post */
data class PostResponse(
    @Json(name = "post") val post: Post
)

/** Register token request */
data class RegisterTokenRequest(
    @Json(name = "token") val token: String,
    @Json(name = "platform") val platform: String = "android",
    @Json(name = "model") val model: String = android.os.Build.MODEL,
    @Json(name = "appVersion") val appVersion: String = "1.0.0"
)

/** Register token response */
data class RegisterTokenResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "error") val error: String? = null
)

/** Unregister token request */
data class UnregisterTokenRequest(
    @Json(name = "token") val token: String
)

/** Unregister token response */
data class UnregisterTokenResponse(
    @Json(name = "ok") val ok: Boolean,
    @Json(name = "error") val error: String? = null
)