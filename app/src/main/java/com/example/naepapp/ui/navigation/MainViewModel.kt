package com.example.naepapp.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.data.repository.AuthRepository
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.ui.model.UserUi
import com.example.naepapp.ui.model.toUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    sessionManager: SessionManager
) : ViewModel() {
    val user: StateFlow<UserUi?> = sessionManager.user
        .map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    suspend fun checkSession(): Boolean {
        val result = authRepository.restoreSession()
        if (result.isSuccess) return true
        return authRepository.hasTokens()
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
