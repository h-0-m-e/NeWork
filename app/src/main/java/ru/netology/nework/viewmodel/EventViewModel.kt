package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import ru.netology.nework.dto.Event
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.model.EventsFeedModelState
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.types.AttachmentType
import ru.netology.nework.types.ErrorType
import ru.netology.nework.types.EventType

private val empty = Event(
    id = 0,
authorId = 0,
author = "",
authorAvatar = null,
authorJob = null,
content = "",
datetime = "",
published = "",
coords = null,
type = EventType.OFFLINE,
likeOwnerIds = emptyList(),
likedByMe = false,
speakerIds = emptyList(),
participantIds = emptyList(),
participatedByMe = false,
attachment = null,
link = "",
ownedByMe = false,
users = emptyMap(),
likes = 0,
speakersAvatarUrls = emptyList(),
taggedMe = false,
isPlayingAudio = false
)

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository(
        AppDb.getInstance(application).appDao(),
        AppAuth.getInstance().data.value?.id?.toInt() ?: 0,
        AppApi
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<Event>> = AppAuth.getInstance().data.flatMapLatest { token ->
        repository.data
            .map{ events ->
                events.map{
                    it.copy(ownedByMe = it.authorId == token?.id?.toInt())
                }
            }
    }
        .flowOn(Dispatchers.Default)


    private val _dataState = MutableLiveData<EventsFeedModelState>()
    val dataState: LiveData<EventsFeedModelState>
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

    private val _lastEvent =
        MutableLiveData(empty)

    val lastEvent: LiveData<Event>
        get() = _lastEvent

    //This parameter is shows which list is on(Events or Posts)
    val isPostsShowed = MutableLiveData(true)

//    init {
//        loadEvents()
//    }
//
//    fun loadEvents() = viewModelScope.launch {
//        _dataState.value = EventsFeedModelState(loading = true)
//        try {
//            repository.getAll()
//            _dataState.value = EventsFeedModelState()
//        } catch (e: Exception) {
//            _dataState.value = EventsFeedModelState(error = ErrorType.LOADING, errorMessage = e.message)
//        }
//    }
//
//    fun refresh() = viewModelScope.launch {
//        _dataState.value = EventsFeedModelState(refreshing = true)
//        try {
//            repository.getAll()
//            _dataState.value = EventsFeedModelState()
//        } catch (e: Exception) {
//            _dataState.value = EventsFeedModelState(error = ErrorType.LOADING)
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
                    else -> repository.saveWithAttachment(it.copy(ownedByMe = true), attachment, attachmentType.value!!)
                }

            }
            edited.value = empty
        } catch (e: Exception) {
            _dataState.value = EventsFeedModelState(error = ErrorType.SAVE)
        }
    }

    fun edit(event: Event) {
        edited.value = event
    }

    fun likeById(event: Event) = viewModelScope.launch {
        _lastEvent.postValue(event)
        try {
            repository.likeById(event.id.toLong())
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
            _lastEvent.postValue(repository.getById(id))
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
