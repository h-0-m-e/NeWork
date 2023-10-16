package ru.netology.nework.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.netology.nework.BuildConfig
import ru.netology.nework.dto.Token
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://netomedia.ru/api/"

private val client = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .run {
        if (BuildConfig.DEBUG) {
            this.addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
        } else {
            this
        }
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface AuthApiService {

    @FormUrlEncoded
    @POST("users/authentication/")
    suspend fun signIn(@Field("login") login: String, @Field("password") password: String): Response<Token>

    @Multipart
    @FormUrlEncoded
    @POST("users/registration/")
    suspend fun registerUser(@Field("login") login: String, @Field("password") password: String,@Field("name") name: String, @Part file: MultipartBody.Part): Response<Token>

    @FormUrlEncoded
    @POST("users/registration/")
    suspend fun registerUserWithNoAvatar(@Field("login") login: String, @Field("password") password: String,@Field("name") name: String): Response<Token>
}

object AuthApi {
    val service: AuthApiService by lazy { retrofit.create() }
}
