package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.User

@Entity
data class JobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userId: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String?,
    val link: String?
) {
    fun toDto() = Job(
        id = id,
        name = name,
        position = position,
        start = start,
        finish = finish,
        link = link
    )

    companion object {
        fun fromDto(dto: Job, userId: Long) =
            JobEntity(
                id = dto.id,
                userId = userId,
                name = dto.name,
                position = dto.position,
                start = dto.start,
                finish = dto.finish,
                link = dto.link
            )

    }
}
fun Job.toEntity(job: Job, userId: Long): JobEntity = JobEntity.fromDto(job, userId)
