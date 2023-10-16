package ru.netology.nework.model

data class AuthModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val success: Boolean = false,
    val signedIn: Boolean = false,
    val signedUp: Boolean = false,
    val message: String? = null
)
