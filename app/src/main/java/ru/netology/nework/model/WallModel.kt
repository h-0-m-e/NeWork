package ru.netology.nework.model

import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.types.ErrorType

data class WallModel(
    val posts: List<Post> = emptyList(),
    val events: List<Event> = emptyList(),
    val empty: Boolean = false
)

data class WallModelState(
    val error: ErrorType? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)
