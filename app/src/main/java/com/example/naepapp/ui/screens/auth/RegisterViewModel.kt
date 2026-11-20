package com.example.naepapp.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.core.common.ConnectivityMonitor
import com.example.naepapp.core.common.InputValidator
import com.example.naepapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {
    var fullName by mutableStateOf("")
        private set
    var phone by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var confirmPassword by mutableStateOf("")
        private set

    var fullNameError by mutableStateOf<String?>(null)
        private set
    var phoneError by mutableStateOf<String?>(null)
        private set
    var emailError by mutableStateOf<String?>(null)
        private set
    var passwordError by mutableStateOf<String?>(null)
        private set
    var confirmPasswordError by mutableStateOf<String?>(null)
        private set

    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateFullName(v: String) {
        fullName = v
        errorMessage = null
        fullNameError = if (v.isNotEmpty() && !InputValidator.isValidFullName(v)) {
            "Full name must be at least 2 characters"
        } else null
    }

    fun updatePhone(v: String) {
        phone = v
        errorMessage = null
        phoneError = if (v.isNotEmpty() && !InputValidator.isValidPhone(v)) {
            "Enter a valid phone number (at least 9 digits)"
        } else null
    }

    fun updateEmail(v: String) {
        email = v
        errorMessage = null
        emailError = if (v.isNotEmpty() && !InputValidator.isValidEmail(v)) {
            "Enter a valid email address (e.g. name@example.com)"
        } else null
    }

    fun updatePassword(v: String) {
        password = v
        errorMessage = null
        passwordError = if (v.isNotEmpty() && !InputValidator.isValidPassword(v)) {
            "Password must be at least 8 characters"
        } else null
        if (confirmPassword.isNotEmpty()) {
            confirmPasswordError = if (confirmPassword != v) "Passwords do not match" else null
        }
    }

    fun updateConfirmPassword(v: String) {
        confirmPassword = v
        errorMessage = null
        confirmPasswordError = if (v.isNotEmpty() && v != password) {
            "Passwords do not match"
        } else null
    }

    fun register(onSuccess: () -> Unit) {
        if (!connectivityMonitor.isOnline.value) {
            errorMessage = "This needs an internet connection."
            return
        }

        val fnErr = when {
            fullName.isBlank() -> "Full name is required"
            !InputValidator.isValidFullName(fullName) -> "Full name must be at least 2 characters"
            else -> null
        }
        val phErr = when {
            phone.isBlank() -> "Phone number is required"
            !InputValidator.isValidPhone(phone) -> "Enter a valid phone number (at least 9 digits)"
            else -> null
        }
        val emErr = when {
            email.isBlank() -> "Email address is required"
            !InputValidator.isValidEmail(email) -> "Enter a valid email address (e.g. name@example.com)"
            else -> null
        }
        val pwErr = when {
            password.isBlank() -> "Password is required"
            !InputValidator.isValidPassword(password) -> "Password must be at least 8 characters"
            else -> null
        }
        val cpwErr = when {
            confirmPassword.isBlank() -> "Please confirm your password"
            confirmPassword != password -> "Passwords do not match"
            else -> null
        }

        fullNameError = fnErr
        phoneError = phErr
        emailError = emErr
        passwordError = pwErr
        confirmPasswordError = cpwErr

        if (fnErr != null || phErr != null || emErr != null || pwErr != null || cpwErr != null) {
            errorMessage = "Please fix the highlighted errors above"
            return
        }

        viewModelScope.launch {
            loading = true
            errorMessage = null
            val result = authRepository.register(fullName, phone, email, password)
            loading = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    val apiError = err as? ApiError
                    val detailsText = apiError?.details?.joinToString("\n") { "${it.field}: ${it.message}" }
                    errorMessage = if (!detailsText.isNullOrBlank()) detailsText else (err.message ?: "Registration failed")
                }
            )
        }
    }
}
