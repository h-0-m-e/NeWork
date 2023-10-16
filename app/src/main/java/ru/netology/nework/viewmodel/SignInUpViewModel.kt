package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nework.api.AuthApi
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Token
import ru.netology.nework.error.ApiError
import ru.netology.nework.model.AuthModelState
import ru.netology.nework.model.AttachmentModel

class SignInUpViewModel : ViewModel() {

    private val _dataState = MutableLiveData<AuthModelState>()
    val dataState: LiveData<AuthModelState>
        get() = _dataState

    fun signIn(login: String, pass: String) = viewModelScope.launch {
        _dataState.value = AuthModelState(loading = true)
        try {
            val response = AuthApi.service.signIn(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val token: Token = requireNotNull(response.body())
            AppAuth.getInstance().setToken(token)
            _dataState.value = AuthModelState(success = true, signedIn = true)
        } catch (e: Exception) {
            _dataState.value = AuthModelState(error = true, message = e.message)
        }
    }

    fun signUpWithNoAvatar(login: String, pass: String, name: String) = viewModelScope.launch {
        _dataState.value = AuthModelState(loading = true)
        try {
            val response = AuthApi.service.registerUserWithNoAvatar(login, pass, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val token: Token = requireNotNull(response.body())
            AppAuth.getInstance().setToken(token)
            _dataState.value = AuthModelState(success = true, signedUp = true)
        } catch (e: Exception) {
            _dataState.value = AuthModelState(error = true)
        }
    }

    fun signUp(login: String, pass: String, name: String, model: AttachmentModel) =
        viewModelScope.launch {
            _dataState.value = AuthModelState(loading = true)
            try {
                val response = AuthApi.service.registerUser(
                    login,
                    pass,
                    name,
                    MultipartBody.Part.createFormData("file", "file", model.file.asRequestBody())
                )
                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val token: Token = requireNotNull(response.body())
                AppAuth.getInstance().setToken(token)
                _dataState.value = AuthModelState(success = true, signedUp = true)
            } catch (e: Exception) {
                _dataState.value = AuthModelState(error = true)
            }
        }

    fun clean() {
        _dataState.value = AuthModelState(loading = false, error = false, success = false)
    }
}
