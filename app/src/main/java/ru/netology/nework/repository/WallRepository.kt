package ru.netology.nework.repository

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.AppApi
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import java.io.IOException

class WallRepository(
    private val appDao: AppDao,
    private val userId: MutableLiveData<Long>
) {
    val data: Flow<List<Post>> =
        appDao.postGetPostsById(userId.value!!).map { it.map(PostEntity::toDto) }

//    fun getNewer(id: Long): Flow<Int> = flow {
//        while (true) {
//            val response = AppApi.service.postGetNewer(id)
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//            appDao.postInsert(body.toEntity())
//            emit(body.size)
//        }
//    }
//        .catch { e -> throw AppError.from(e) }
//        .flowOn(Dispatchers.Default)

    suspend fun getWall() {
        val response = AppApi.service.wallGet(userId.value!!)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(posts.map { PostEntity.fromDto(it) })
    }

    suspend fun getUser(): User {
        val response = AppApi.service.userGet(userId.value!!.toString())
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body() ?: throw RuntimeException("body is null")
        appDao.userInsert(body.toEntity(body))
        return body
    }


    suspend fun getJob(): Job {
        val response = AppApi.service.jobGet(userId.value!!)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body()?.last() ?: throw RuntimeException("body is null")
        appDao.jobInsert(body.toEntity(body, userId.value!!))
        return body
    }
}
