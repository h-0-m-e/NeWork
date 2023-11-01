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

//    val user: User =
//        appDao.userGet(userId).toDto()
//    suspend fun getUserFromDB
//
//    val job: Job? =
//        appDao.jobGet(userId)?.toDto()


    fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = AppApi.service.postGetNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.postInsert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    suspend fun getWall() {
        val response = AppApi.service.wallGet(userId.value!!)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(posts.map { PostEntity.fromDto(it) })
    }

    suspend fun getUser(): User {
        val response = AppApi.service.userGet(userId.value!!)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body() ?: throw RuntimeException("body is null")
        appDao.userInsert(body.toEntity(body))
        return body
    }

    suspend fun getJob(): Job {
        if(userId.value == AppAuth.getInstance().data.value?.id){
            val response = AppApi.service.myJobGet()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val body = response.body() ?: throw RuntimeException("body is null")
            appDao.jobInsert(body.toEntity(body, AppAuth.getInstance().data.value!!.id))
            return body
        } else {
            val response = AppApi.service.jobGet(userId.value!!)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val body = response.body() ?: throw RuntimeException("body is null")
            appDao.jobInsert(body.toEntity(body, userId.value!!))
            return body
        }
    }

    suspend fun saveJob(job: Job) {
        val response = AppApi.service.saveJob(job)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body() ?: throw RuntimeException("body is null")
        appDao.jobInsert(body.toEntity(body, AppAuth.getInstance().data.value!!.id))
    }

    suspend fun deleteJob(jobId: String) {
        appDao.jobDelete(jobId.toInt())
        val response = AppApi.service.deleteJob(jobId)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
    }

    suspend fun likeById(id: Long) {
        try {
            val liked = appDao.postGetById(id).likedByMe
            val response =
                if (liked) AppApi.service.postUnlikeById(id)
                else AppApi.service.postLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: ApiError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun save(post: Post) {
        try {
            val response = AppApi.service.postSave(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.postInsert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun saveWithAttachment(post: Post, model: AttachmentModel, type: AttachmentType) {

        try {
            val mediaUrl = uploadMedia(model)

            val response = AppApi.service.postSave(
                post.copy(
                    attachment =
                    Attachment(
                        mediaUrl,
                        type
                    )
                )
            )
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.postInsert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    private suspend fun uploadMedia(model: AttachmentModel): String {
        val response = AppApi.service.uploadMedia(
            MultipartBody.Part.createFormData("file", "file", model.file.asRequestBody())
        )

        if (!response.isSuccessful) {
            throw ApiError(response.code(), response.message())
        }

        return requireNotNull(response.body())
    }

    suspend fun removeById(id: Long) {
        try {
            appDao.postRemoveById(id)
            val response = AppApi.service.postRemoveById(id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

}
