package ru.netology.nework.model

import ru.netology.nework.dto.Post
import ru.netology.nework.types.ErrorType

data class PostsFeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false
)

data class PostsFeedModelState(
    val error: ErrorType? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)
