package ru.netology.nework.dto

import ru.netology.nework.types.EventType

data class Event (
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: Coordinates? = null,
    val type: EventType,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean = false,
    val speakerIds: List<Int>,
    val participantIds: List<Int>?,
    val participatedByMe: Boolean = false,
    val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview> = emptyMap(),
    val likes: Long,
    val speakersAvatarUrls: List<String>,
    val taggedMe: Boolean = false,
    val isPlayingAudio: Boolean = false
)

