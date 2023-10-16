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
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.entity.EventEntity
import ru.netology.nework.entity.UserEntity
import ru.netology.nework.entity.toEntity
import ru.netology.nework.error.ApiError
import ru.netology.nework.error.AppError
import ru.netology.nework.error.NetworkError
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.types.AttachmentType
import java.io.IOException

class EventRepository(private val appDao: AppDao, private val  myId: Int) {
    val data: Flow<List<Event>> =
        appDao.eventGetAll().map { it.map(EventEntity::toDto) }




    fun getNewer(id: Long): Flow<Int> = flow {
        while (true) {
            val response = AppApi.service.eventGetNewer(id)
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
        val response = AppApi.service.eventGetAll()
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
            val response = AppApi.service.userGet(user.toLong())
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
            val liked = appDao.eventGetById(id).likedByMe
            val response =
                if (liked) AppApi.service.eventUnlikeById(id)
                else AppApi.service.eventLikeById(id)
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

    suspend fun save(event: Event) {
        try {
            val response = AppApi.service.eventSave(event)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            appDao.eventInsert(EventEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw ru.netology.nework.error.UnknownError
        }
    }

    suspend fun saveWithAttachment(event: Event, model: AttachmentModel, type: AttachmentType) {

        try {
            val mediaUrl = uploadMedia(model)

            val response = AppApi.service.eventSave(
                event.copy(
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
            appDao.eventInsert(EventEntity.fromDto(body))
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
            appDao.eventRemoveById(id)
            val response = AppApi.service.eventRemoveById(id)
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
