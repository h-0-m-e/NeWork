package ru.netology.nework.model

import android.net.Uri
import java.io.File

data class AttachmentModel(
    val uri: Uri,
    val file: File
)
