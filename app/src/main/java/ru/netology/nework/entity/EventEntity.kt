package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentEmbeddable
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.UserPreview
import ru.netology.nework.types.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val coords: String,
    val type: EventType,
    val likeOwnerIds: List<Int>,
    val likedByMe: Boolean = false,
    val speakerIds: List<Int>,
    val participantIds: List<Int>?,
    val participatedByMe: Boolean = false,
    val link: String?,
    val ownedByMe: Boolean = false,
    val likes: Long,
    val taggedMe: Boolean = false,
    val speakersAvatarUrls: List<String>,
    val isPlayingAudio: Boolean = false,
    @Embedded(prefix = "event_")
    var attachment: AttachmentEmbeddable?
) {
    fun toDto() = Event(
        id = id,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        datetime = datetime,
        published = published,
        type = type,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantIds = participantIds,
        participatedByMe = participatedByMe,
        attachment = attachment?.toDto(),
        link = link,
        ownedByMe = ownedByMe,
        likes = likes,
        speakersAvatarUrls = speakersAvatarUrls,
        taggedMe = taggedMe,
        isPlayingAudio = isPlayingAudio,
    )

    companion object {
        fun fromDto(dto: Event) =
            EventEntity(
                id = dto.id,
                authorId = dto.authorId,
                authorAvatar = dto.authorAvatar,
                authorJob = dto.authorJob,
                author = dto.author,
                content = dto.content,
                datetime = dto.datetime,
                published = dto.published,
                coords = if(dto.coords != null)
                    "${dto.coords.lat}/${dto.coords.long}"
                else
                    "",
                type = dto.type,
                likeOwnerIds = dto.likeOwnerIds,
                likedByMe = dto.likedByMe,
                speakerIds = dto.speakerIds,
                participantIds = dto.participantIds,
                participatedByMe = dto.participatedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment),
                link = dto.link,
                ownedByMe = dto.ownedByMe,
                likes = dto.likes,
                taggedMe = dto.taggedMe,
                speakersAvatarUrls = dto.speakersAvatarUrls
            )

    }
}

fun List<EventEntity>.toDto(): List<Event> = map(EventEntity::toDto)
fun List<Event>.toEntity(): List<EventEntity> = map {
    EventEntity.fromDto(it)
}
