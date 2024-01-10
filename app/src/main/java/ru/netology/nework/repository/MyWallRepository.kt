package ru.netology.nework.repository


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.netology.nework.api.AppApi
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dao.AppDao
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.User
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.toEntity

class MyWallRepository(
    private val appDao: AppDao
) {
    private val myId = AppAuth.getInstance().data.value!!.id
    val data: Flow<List<Post>> =
        appDao.postGetPostsById(myId).map { it.map(PostEntity::toDto) }


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
        val response = AppApi.service.myWallGet()
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val posts = response.body() ?: throw RuntimeException("body is null")
        appDao.postInsert(posts.map { PostEntity.fromDto(it) })
    }

    suspend fun getUser(): User {
        val response = AppApi.service.userGet(myId.toString())
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body() ?: throw RuntimeException("body is null")
        appDao.userInsert(body.toEntity(body))
        return body
    }

    suspend fun getJob(): Job {
            val response = AppApi.service.myJobGet()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            val body = response.body()?.last() ?: throw RuntimeException("body is null")
            appDao.jobInsert(body.toEntity(body, myId))
            return body
    }

    suspend fun saveJob(job: Job) {
        val response = AppApi.service.saveJob(job)
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        val body = response.body() ?: throw RuntimeException("body is null")
        appDao.jobInsert(body.toEntity(body, myId))
    }

    suspend fun deleteJob(jobId: Int) {
        val response = AppApi.service.deleteJob(jobId.toString())
        if (!response.isSuccessful) {
            throw RuntimeException(response.message())
        }
        appDao.jobDelete(jobId)
    }

}
