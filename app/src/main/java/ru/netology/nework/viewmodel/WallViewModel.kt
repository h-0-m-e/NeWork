package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.User
import ru.netology.nework.model.PostsFeedModel
import ru.netology.nework.model.PostsFeedModelState
import ru.netology.nework.repository.WallRepository
import ru.netology.nework.types.ErrorType

private val emptyUser = User(
    id = 0,
    login = "",
    name = "",
    avatar = null
)

class WallViewModel(private val application: Application) : AndroidViewModel(application) {

    private val userId = MutableLiveData(0L)

    private var repository = WallRepository(
        AppDb.getInstance(application).appDao(),
        userId
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: LiveData<PostsFeedModel> = AppAuth.getInstance().data.flatMapLatest { token ->
        repository.data
            .map { posts ->
                posts.map {
                    it.copy(ownedByMe = it.authorId == token?.id?.toInt())
                }
            }
            .map { PostsFeedModel(it) }
    }
        .asLiveData(Dispatchers.Default)

    val user = MutableLiveData(emptyUser)

    val job = MutableLiveData<Job?>(null)


    private val _dataState = MutableLiveData<PostsFeedModelState>()
    val dataState: LiveData<PostsFeedModelState>
        get() = _dataState


    fun loadPosts() = viewModelScope.launch {
        _dataState.value = PostsFeedModelState(loading = true)
        try {
            repository.getWall()
            _dataState.value = PostsFeedModelState()
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = ErrorType.LOADING)
        }
    }

    fun loadUser() = viewModelScope.launch {
        _dataState.value = PostsFeedModelState(loading = true)
        try {
            user.value = repository.getUser()
        } catch (e: Exception) {
            print(e)
        }
    }

    fun loadJob() = viewModelScope.launch {
        _dataState.value = PostsFeedModelState(loading = true)
        try {
            job.value = repository.getJob()
        } catch (e: Exception) {
            print(e)
        }
    }

    fun saveJob(job: Job) = viewModelScope.launch {
        try {
            repository.saveJob(job)
        } catch (e: Exception) {
            print(e)
        }
    }

    fun deleteJob() = viewModelScope.launch {
        try {
            repository.deleteJob(job.value?.id.toString())
        } catch (e: Exception) {
            print(e)
        }
        job.value = null
    }

    fun refreshGeneral(userId: Long) {
        setUser(userId)
        loadUser()
        loadJob()
        loadPosts()
    }


    fun setUser(id: Long){
        userId.value = id
        repository = WallRepository(
            AppDb.getInstance(application).appDao(),
            userId
        )
    }

}
