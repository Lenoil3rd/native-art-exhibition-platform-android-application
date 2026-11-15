package com.example.naepapp.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    var selectedMode by mutableStateOf("enthusiast")
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun selectMode(mode: String) { selectedMode = mode }

    fun submit(onSuccess: (becameArtisan: Boolean) -> Unit) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            val result = authRepository.chooseMode(selectedMode)
            loading = false
            result.fold(
                onSuccess = { user ->
                    val isArtisan = user.isArtisan || user.preferredMode?.equals("artisan", ignoreCase = true) == true
                    onSuccess(isArtisan)
                },
                onFailure = { err -> errorMessage = err.message ?: "Role selection failed" }
            )
        }
    }
}
