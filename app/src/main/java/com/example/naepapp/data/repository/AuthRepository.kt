package com.example.naepapp.data.repository

import com.example.naepapp.core.common.ApiError
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager
) {
    suspend fun login(identifier: String, pass: String): Result<UserDto> {
        return apiCall {
            val response = apiService.login(LoginRequest(identifier, pass))
            tokenStorage.save(response.data.accessToken, response.data.refreshToken)
            sessionManager.setUser(response.data.user)
            response.data.user
        }
    }

    suspend fun register(fullName: String, phone: String, email: String, pass: String): Result<UserDto> {
        return apiCall {
            val response = apiService.register(RegisterRequest(fullName, phone, email, pass))
            tokenStorage.save(response.data.accessToken, response.data.refreshToken)
            sessionManager.setUser(response.data.user)
            response.data.user
        }
    }

    suspend fun updateMe(request: UpdateMeRequest): Result<UserDto> {
        return apiCall {
            val response = apiService.updateMe(request)
            sessionManager.setUser(response.data)
            response.data
        }
    }

    suspend fun restoreSession(): Result<UserDto?> {
        val access = tokenStorage.access()
        if (access.isNullOrBlank()) {
            sessionManager.setUser(null)
            return Result.success(null)
        }
        val result = apiCall {
            val response = apiService.getMe()
            sessionManager.setUser(response.data)
            response.data
        }
        result.fold(
            onSuccess = {
                return Result.success(it)
            },
            onFailure = { throwable ->
                val apiError = throwable as? ApiError
                val code = apiError?.code ?: ""
                val isStaleSession = code in listOf("UNAUTHORIZED", "TOKEN_EXPIRED", "NOT_FOUND", "REFRESH_INVALID") ||
                        code.startsWith("HTTP_401") || code.startsWith("HTTP_404")

                if (isStaleSession) {
                    tokenStorage.clear()
                    sessionManager.setUser(null)
                    sessionManager.setPendingAction(null, null)
                    return Result.failure(ApiError("STALE_SESSION", "Please sign in again."))
                } else {
                    sessionManager.setUser(null)
                    return Result.failure(throwable)
                }
            }
        )
    }

    suspend fun hasTokens(): Boolean = !tokenStorage.access().isNullOrBlank()

    fun isGuest(): Boolean = sessionManager.user.value == null

    suspend fun chooseMode(mode: String): Result<UserDto> {
        return apiCall {
            val validMode = if (mode.equals("artisan", ignoreCase = true)) "artisan" else "enthusiast"
            val response = apiService.postOnboarding(OnboardingRequest(mode = validMode))
            sessionManager.setUser(response.data)
            response.data
        }
    }

    suspend fun logout(): Result<Unit> {
        return apiCall {
            val refresh = tokenStorage.refresh()
            if (!refresh.isNullOrBlank()) {
                try {
                    apiService.logout(LogoutRequest(refresh))
                } catch (_: Exception) {}
            }
            sessionManager.signOutLocally()
        }
    }
}
