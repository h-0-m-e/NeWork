package ru.netology.nework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.AttachmentEmbeddable
import ru.netology.nework.dto.Coordinates
import ru.netology.nework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val authorId: Int,
    val authorAvatar: String?,
    val authorName: String,
    val authorJob: String?,
    val content: String,
    val published: String,
    val coords: String,
    val link: String?,
    val likeOwnerIds: List<Int>,
    val mentionIds: List<Int>,
    val mentionedMe: Boolean,
    val likedByMe: Boolean,
    val likes: Long = 0,
    val ownedByMe: Boolean,
    @Embedded
    var attachment: AttachmentEmbeddable?
) {
    fun toDto() = Post(
        id = id,
        authorId = authorId,
        authorAvatar = authorAvatar,
        author = authorName,
        authorJob = authorJob,
        content = content,
        published = published,
        coords =
        if(coords.isBlank())
            null
        else
            Coordinates(
                coords.split("/").first(),
                coords.split("/").last()
            ),
        link = link,
        likeOwnerIds = likeOwnerIds,
        mentionIds = mentionIds,
        mentionedMe = mentionedMe,
        likedByMe = likedByMe,
        likes = likes,
        ownedByMe = ownedByMe,
        attachment = attachment?.toDto()
    )

    companion object {
        fun fromDto(dto: Post) =
            PostEntity(
                id = dto.id,
                authorId = dto.authorId,
                authorAvatar = dto.authorAvatar,
                authorName = dto.author,
                authorJob = dto.authorJob,
                content = dto.content,
                published = dto.published,
                coords = if(dto.coords != null)
                    "${dto.coords.lat}/${dto.coords.long}"
                else
                    "",
                link = dto.link,
                likeOwnerIds = dto.likeOwnerIds,
                mentionIds = dto.mentionIds,
                mentionedMe = dto.mentionedMe,
                likedByMe = dto.likedByMe,
                likes = dto.likeOwnerIds.size.toLong(),
                ownedByMe = dto.ownedByMe,
                attachment = AttachmentEmbeddable.fromDto(dto.attachment)
            )

    }
}

fun List<PostEntity>.toDto(): List<Post> = map(PostEntity::toDto)
fun List<Post>.toEntity(): List<PostEntity> = map {
    PostEntity.fromDto(it)
}

