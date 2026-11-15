package com.example.naepapp.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.InputValidator
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

    var identifierError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateIdentifier(value: String) {
        identifier = value
        errorMessage = null
        identifierError = if (value.isNotEmpty() && !InputValidator.isValidEmailOrPhone(value)) {
            "Enter a valid email address or phone number"
        } else null
    }

    fun updatePassword(value: String) {
        password = value
        errorMessage = null
        passwordError = if (value.isNotEmpty() && value.length < 6) {
            "Password must be at least 6 characters"
        } else null
    }

    fun login(onSuccess: (UserDto) -> Unit) {
        val idErr = when {
            identifier.isBlank() -> "Email or phone number is required"
            !InputValidator.isValidEmailOrPhone(identifier) -> "Enter a valid email address or phone number"
            else -> null
        }
        val pwErr = when {
            password.isBlank() -> "Password is required"
            else -> null
        }

        identifierError = idErr
        passwordError = pwErr

        if (idErr != null || pwErr != null) {
            errorMessage = "Please enter valid credentials"
            return
        }

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
