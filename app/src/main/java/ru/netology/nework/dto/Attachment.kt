package ru.netology.nework.dto

import ru.netology.nework.types.AttachmentType

data class Attachment(
    val url: String,
    val type: AttachmentType,
)
