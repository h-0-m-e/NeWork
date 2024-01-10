package ru.netology.nework.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.AppApi
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.MediaResponse
import ru.netology.nework.dto.Post
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import java.io.IOException

class EventRepository(
    private val appDao: AppDao,
    private val  myId: Int,
    private val api: AppApi
    ) {
//    val data: Flow<List<Event>> =
//        appDao.eventGetAll().map { it.map(EventEntity::toDto) }

    val data = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = {
            EventPagingSource(
                api
            )
        }
    ).flow

    fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = api.service.eventGetNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.eventInsert(body.toEntity())
            emit(body.size)
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    suspend fun getAll() {
        val response = api.service.eventGetAll()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val events = response.body() ?: throw RuntimeException("body is null")
        appDao.eventInsert(
            events.map {
                EventEntity.fromDto(
                    it.copy(
                        likedByMe = it.likeOwnerIds.contains(myId),
                        likes = it.likeOwnerIds.size.toLong(),
                        ownedByMe = it.authorId == myId,
                        taggedMe = it.speakerIds.contains(myId),
                        speakersAvatarUrls = getUsersAvatarUrls(it.speakerIds)
                    )
                )
            }
        )
    }

    private suspend fun getUsersAvatarUrls(userIds: List<Int>): List<String> {
        val userAvatarUrls = mutableListOf<String>()
        for (user in userIds){
            val response = api.service.userGet(user.toLong())
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val body = response.body() ?: throw RuntimeException("body is null")
            userAvatarUrls.add(body.avatar ?: "")
        }
        return userAvatarUrls
    }

    suspend fun likeById(id: Long) {
        try {
            val liked = api.service.eventGetById(id.toString()).body()?.likedByMe
            val response =
                if (liked!!) api.service.eventUnlikeById(id)
                else api.service.eventLikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            appDao.eventInsert(EventEntity.fromDto(body))
        } catch (e: ApiError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun save(event: Event) {
        try {
            val response = api.service.eventSave(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            appDao.eventInsert(EventEntity.fromDto(body))

        } catch (e: IOException) {
            throw NetworkError
        }
    }

    suspend fun saveWithAttachment(event: Event, model: AttachmentModel, type: AttachmentType) {
        try {
            val media = upload(model, type)
            val events = event.copy(
                attachment = Attachment(
                    media.url,
                    type = type
                )
            )
            val response = api.service.eventSave(events)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.eventInsert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        }
    }

    suspend fun upload(model: AttachmentModel, type: AttachmentType): MediaResponse {
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
//                model.file.inputStream().readBytes().toRequestBody("*/*".toMediaTypeOrNull())
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

            val response = api.service.eventRemoveById(id)
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            appDao.eventRemoveById(id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun getById(id: Long): Event {
        try {
            val response = api.service.eventGetById(id.toString())
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        }
    }


}
