package ru.netology.nework.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.model.AttachmentModel
import ru.netology.nework.repository.EventRepository
import ru.netology.nework.repository.PostRepository
import ru.netology.nework.types.AttachmentType

class CreateViewModel (application: Application) : AndroidViewModel(application) {
    
    private val myId = AppAuth.getInstance().data.value!!.id.toInt()

    private val postRepository = PostRepository(
        AppDb.getInstance(application).appDao(), myId
    )

    private val eventRepository = EventRepository(
        AppDb.getInstance(application).appDao(), myId
    )
    
    fun publishPost(post: Post) = viewModelScope.launch {
        try {
            postRepository.save(post)
        } catch (e: Exception) {
            print(e)
        }
    }

    fun publishPostWithAttachment(post: Post, model: AttachmentModel, type: AttachmentType) = viewModelScope.launch {
        try {
            postRepository.saveWithAttachment(post,model,type)
        } catch (e: Exception) {
            print(e)
        }
    }

    fun publishEvent(event: Event) = viewModelScope.launch {
        try {
            eventRepository.save(event)
        } catch (e: Exception) {
            print(e)
        }
    }

    fun publishEventWithAttachment(event: Event, model: AttachmentModel, type: AttachmentType) = viewModelScope.launch {
        try {
            eventRepository.saveWithAttachment(event,model,type)
        } catch (e: Exception) {
            print(e)
        }
    }
}
