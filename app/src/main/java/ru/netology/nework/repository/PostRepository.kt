package ru.netology.nework.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nework.api.AppApi
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import java.io.IOException

class PostRepository(
    private val appDao: AppDao,
    private val myId: Int,
    private val api: AppApi
) {
//    val data: Flow<List<Post>> =
//        appDao.postGetAll().map { it.map(PostEntity::toDto) }

    val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource(
                api
            )
        }
    ).flow


    fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = api.service.postGetNewer(id)
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
        val response = api.service.postGetAll()
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
        val response = api.service.postGetAll()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(posts.map { PostEntity.fromDto(it) })
    }

    suspend fun likeById(id: Long) {
        try {
            val liked = api.service.postGetById(id.toString()).body()?.likedByMe
            val response =
                if (liked!!) api.service.postUnlikeById(id)
                else api.service.postLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            appDao.postInsert(PostEntity.fromDto(body))
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
            val response = api.service.postSave(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            appDao.postInsert(PostEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        }
    }

    suspend fun saveWithAttachment(post: Post, model: AttachmentModel, type: AttachmentType) {
        try {
            val media = upload(model, type)
            val posts = post.copy(
                attachment = Attachment(
                    media.url,
                    type = type
                )
            )
            val response = api.service.postSave(posts)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.postInsert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    private suspend fun upload(model: AttachmentModel, type: AttachmentType): MediaResponse {
        try {
            val media = MultipartBody.Part.createFormData(
                "file",
                model.file.name,
                when(type){
                    AttachmentType.IMAGE -> model.file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    AttachmentType.AUDIO -> model.file.asRequestBody("audio/mpeg".toMediaTypeOrNull())
                    AttachmentType.VIDEO -> model.file.asRequestBody("video/mp4".toMediaTypeOrNull())
                }
            )

//            val media = MultipartBody.Builder()
//                .setType(MultipartBody.FORM)
//                .addFormDataPart("file", model.file.name, model.file.asRequestBody("*/*".toMediaTypeOrNull())).build()

//            val fileRequestBody =
//                model.uri.path!!.toRequestBody("*/*".toMediaTypeOrNull())
//            val filePart = MultipartBody.Part.createFormData("file","file_name", fileRequestBody)

            val response = api.service.uploadMedia(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    suspend fun removeById(id: Long) {
        try {
            val response = api.service.postRemoveById(id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            appDao.postRemoveById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun getById(id: Long): Post {
        try {
            val response = api.service.postGetById(id.toString())
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }

}
