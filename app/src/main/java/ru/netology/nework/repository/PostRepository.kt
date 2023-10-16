package ru.netology.nework.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.AppApi
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import java.io.IOException

class PostRepository(private val appDao: AppDao, private val myId: Int) {
    val data: Flow<List<Post>> =
        appDao.postGetAll().map { it.map(PostEntity::toDto) }


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

    suspend fun getAll() {
        val response = AppApi.service.postGetAll()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())

        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(
            posts.map {
                PostEntity.fromDto(
                    it.copy(
                        likedByMe = it.likeOwnerIds.contains(myId),
                        likes = it.likeOwnerIds.size.toLong(),
                        ownedByMe = it.authorId == myId,
                    )
                )
            }
        )
    }

    suspend fun getAllVisible() {
        val response = AppApi.service.postGetAll()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(posts.map { PostEntity.fromDto(it) })
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
