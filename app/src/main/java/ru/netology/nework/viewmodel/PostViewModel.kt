package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.api.AppApi
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.PostsFeedModel
import ru.netology.nework.model.PostsFeedModelState
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.types.ErrorType

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    authorJob = "",
    content = "",
    published = "",
    coords = null,
    link = "",
    likeOwnerIds = emptyList(),
    mentionIds = emptyList(),
    mentionedMe = false,
    likedByMe = false,
    attachment = null,
    ownedByMe = false,
    users = emptyMap(),
    likes = 0L,
    isPlayingAudio = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PostRepository(
        AppDb.getInstance(application).appDao(),
        AppAuth.getInstance().data.value?.id?.toInt() ?: 0,
        AppApi
    )

    private val myId = AppAuth.getInstance().data.value!!.id.toInt()

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Post>> = AppAuth.getInstance().data.flatMapLatest {
        repository.data
            .map { posts ->
                posts.map {
                    it.copy(
                        likedByMe = it.likeOwnerIds.contains(myId),
                        likes = it.likeOwnerIds.size.toLong(),
                        ownedByMe = it.authorId == myId,
                    )
                }
            }
    }
        .flowOn(Dispatchers.Default)


    private val _dataState = MutableLiveData<PostsFeedModelState>()
    val dataState: LiveData<PostsFeedModelState>
        get() = _dataState

    private val _attachment = MutableLiveData<AttachmentModel?>()
    val attachment: LiveData<AttachmentModel?>
        get() = _attachment

    private val _attachmentType = MutableLiveData<AttachmentType?>()
    val attachmentType: LiveData<AttachmentType?>
        get() = _attachmentType

    private val edited = MutableLiveData(empty)

    private val _lastId = MutableLiveData<Long>()
    val lastId: LiveData<Long>
        get() = _lastId

    private val _lastPost =
        MutableLiveData(empty)

    val lastPost: LiveData<Post>
        get() = _lastPost

//    init {
//        loadPosts()
//    }

//    fun loadPosts() = viewModelScope.launch {
//        _dataState.value = PostsFeedModelState(loading = true)
//        try {
//            repository.getAll()
//            _dataState.value = PostsFeedModelState()
//        } catch (e: Exception) {
//            _dataState.value = PostsFeedModelState(error = ErrorType.LOADING)
//        }
//    }

//    fun refresh() = viewModelScope.launch {
//        _dataState.value = PostsFeedModelState(refreshing = true)
//        try {
//            repository.getAll()
//            _dataState.value = PostsFeedModelState()
//        } catch (e: Exception) {
//            _dataState.value = PostsFeedModelState(error = ErrorType.LOADING)
//        }
//    }

    fun removeEdit() {
        edited.value = empty
    }

    fun save() = viewModelScope.launch {
        try {
            edited.value?.let {
                when (val attachment = _attachment.value) {
                    null -> repository.save(it.copy(ownedByMe = true))
                    else -> repository.saveWithAttachment(
                        it.copy(ownedByMe = true),
                        attachment,
                        attachmentType.value!!
                    )
                }

            }
            edited.value = empty
        } catch (e: Exception) {
            _dataState.value = PostsFeedModelState(error = ErrorType.SAVE)
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun likeById(post: Post) = viewModelScope.launch {
        _lastPost.postValue(post)
        try {
            repository.likeById(lastPost.value!!.id.toLong())
        } catch (e: Exception) {
            print(e)
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        _lastId.postValue(id)
        try {
            repository.removeById(_lastId.value!!)
        } catch (e: Exception) {
            print(e)
        }
    }

    fun getById(id: Long) = viewModelScope.launch {
        _lastId.postValue(id)
        try {
            _lastPost.postValue(repository.getById(id))
        } catch (e: Exception) {
            print(e)
        }
    }

    fun setAttachment(attachmentModel: AttachmentModel, attachmentType: AttachmentType) {
        _attachment.value = attachmentModel
        _attachmentType.value = attachmentType
    }

    fun clearAttachment() {
        _attachment.value = null
        _attachmentType.value = null
    }
}
