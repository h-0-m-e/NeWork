package ru.netology.nework.model

import ru.netology.nework.dto.Event
import ru.netology.nework.types.ErrorType

data class EventsFeedModel(
    val events: List<Event> = emptyList(),
    val empty: Boolean = false
)

data class EventsFeedModelState(
    val errorMessage: String? = null,
    val error: ErrorType? = null,
    val loading: Boolean = false,
    val refreshing: Boolean = false
)
