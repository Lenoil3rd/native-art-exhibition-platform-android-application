package com.example.naepapp.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
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
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun updateFullName(v: String) { fullName = v; errorMessage = null }
    fun updatePhone(v: String) { phone = v; errorMessage = null }
    fun updateEmail(v: String) { email = v; errorMessage = null }
    fun updatePassword(v: String) { password = v; errorMessage = null }
    fun updateConfirmPassword(v: String) { confirmPassword = v; errorMessage = null }

    fun register(onSuccess: () -> Unit) {
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
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
