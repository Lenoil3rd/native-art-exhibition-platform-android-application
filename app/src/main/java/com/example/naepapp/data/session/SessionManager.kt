package com.example.naepapp.data.session

import com.example.naepapp.core.network.dto.UserDto
import com.example.naepapp.core.storage.TokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

enum class PendingAction {
    Like, Inquire
}

@Singleton
class SessionManager @Inject constructor(
    private val tokenStorage: TokenStorage
) {
    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _pendingAction = MutableStateFlow<Pair<PendingAction, String>?>(null)
    val pendingAction: StateFlow<Pair<PendingAction, String>?> = _pendingAction.asStateFlow()

    fun setUser(user: UserDto?) {
        _user.value = user
    }

    fun setPendingAction(action: PendingAction?, targetId: String?) {
        if (action != null && targetId != null) {
            _pendingAction.value = Pair(action, targetId)
        } else {
            _pendingAction.value = null
        }
    }

    fun signOutLocally() {
        runBlocking {
            tokenStorage.clear()
        }
        _user.value = null
    }
}
