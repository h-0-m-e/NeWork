package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import ru.netology.nework.BuildConfig
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import java.util.concurrent.TimeUnit


private const val BASE_URL = "https://netomedia.ru/api/"

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .run {
        if (BuildConfig.DEBUG) {
            this.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        } else {
            this
        }
    }
    .addInterceptor { chain ->
        AppAuth.getInstance().data.value?.token?.let { token ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", token)
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        chain.proceed(chain.request())
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface AppApiService {

    @GET("posts/")
    suspend fun postGetAll(): Response<List<Post>>

    @GET("events/")
    suspend fun eventGetAll(): Response<List<Event>>

    @GET("users/")
    suspend fun userGetAll(): Response<List<User>>

    @GET("users/{id}/")
    suspend fun userGet(@Path("id") id: Long): Response<User>

    @GET("{user_id}/jobs/")
    suspend fun jobGet(@Path("user_id") userId: Long): Response<Job>

    @GET("my/jobs/")
    suspend fun myJobGet(): Response<Job>

    @GET("{author_id}/wall/")
    suspend fun wallGet(@Path("author_id") authorId: Long): Response<List<Post>>

    @GET("posts/{id}/after/")
    suspend fun postGetNewer(@Path("id") id: Long): Response<List<Post>>

    @GET("events/{id}/after/")
    suspend fun eventGetNewer(@Path("id") id: Long): Response<List<Event>>

    @POST("posts/")
    suspend fun postSave(@Body post: Post): Response<Post>

    @POST("my/jobs/")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{job_id}/")
    suspend fun deleteJob(jobId: String): Response<Unit>

    @POST("events/")
    suspend fun eventSave(@Body event: Event): Response<Event>

    @DELETE("posts/{id}/")
    suspend fun postRemoveById(@Path("id") id: Long): Response<Unit>

    @DELETE("events/{id}/")
    suspend fun eventRemoveById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes/")
    suspend fun postLikeById(@Path("id") id: Long): Response<Post>

    @POST("events/{id}/likes/")
    suspend fun eventLikeById(@Path("id") id: Long): Response<Event>

    @DELETE("posts/{id}/likes/")
    suspend fun postUnlikeById(@Path("id") id: Long): Response<Post>

    @DELETE("events/{id}/likes/")
    suspend fun eventUnlikeById(@Path("id") id: Long): Response<Event>

    @Multipart
    @POST("media/")
    suspend fun uploadMedia(@Part file: MultipartBody.Part): Response<String>

}

object AppApi {
    val service: AppApiService by lazy { retrofit.create() }
}
