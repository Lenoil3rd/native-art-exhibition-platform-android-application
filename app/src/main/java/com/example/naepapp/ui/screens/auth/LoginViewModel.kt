package com.example.naepapp.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.network.dto.UserDto
import com.example.naepapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var identifier by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateIdentifier(value: String) { identifier = value; errorMessage = null }
    fun updatePassword(value: String) { password = value; errorMessage = null }

    fun login(onSuccess: (UserDto) -> Unit) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            val result = authRepository.login(identifier, password)
            loading = false
            result.fold(
                onSuccess = { user -> onSuccess(user) },
                onFailure = { err -> errorMessage = err.message ?: "Login failed" }
            )
        }
    }
}
