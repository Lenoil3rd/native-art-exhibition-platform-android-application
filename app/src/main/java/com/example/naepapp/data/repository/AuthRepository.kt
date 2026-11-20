package com.example.naepapp.data.repository

import android.content.Context
import androidx.work.WorkManager
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.db.dao.ArtisanDao
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.dao.PendingOperationDao
import com.example.naepapp.core.db.dao.UserDao
import com.example.naepapp.core.db.toDto
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context?,
    private val apiService: ApiService,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager,
    private val userDao: UserDao,
    private val creationDao: CreationDao,
    private val artisanDao: ArtisanDao,
    private val pendingOperationDao: PendingOperationDao
) {
    suspend fun login(identifier: String, pass: String): Result<UserDto> {
        return apiCall {
            val response = apiService.login(LoginRequest(identifier, pass))
            tokenStorage.save(response.data.accessToken, response.data.refreshToken)
            val user = response.data.user
            userDao.upsert(user.toEntity())
            creationDao.updateIsOwn(user.id)
            sessionManager.setUser(user)
            user
        }
    }

    suspend fun register(fullName: String, phone: String, email: String, pass: String): Result<UserDto> {
        return apiCall {
            val response = apiService.register(RegisterRequest(fullName, phone, email, pass))
            tokenStorage.save(response.data.accessToken, response.data.refreshToken)
            val user = response.data.user
            userDao.upsert(user.toEntity())
            creationDao.updateIsOwn(user.id)
            sessionManager.setUser(user)
            user
        }
    }

    suspend fun updateMe(request: UpdateMeRequest): Result<UserDto> {
        return apiCall {
            val response = apiService.updateMe(request)
            val user = response.data
            userDao.upsert(user.toEntity())
            sessionManager.setUser(user)
            user
        }
    }

    suspend fun restoreSession(): Result<UserDto?> {
        val access = tokenStorage.access()
        if (access.isNullOrBlank()) {
            userDao.clear()
            sessionManager.setUser(null)
            return Result.success(null)
        }

        val cachedUser = userDao.getCurrentUser()
        if (cachedUser != null) {
            sessionManager.setUser(cachedUser.toDto())
        }

        val result = apiCall {
            val response = apiService.getMe()
            response.data
        }

        return result.fold(
            onSuccess = { user ->
                userDao.upsert(user.toEntity())
                creationDao.updateIsOwn(user.id)
                sessionManager.setUser(user)
                Result.success(user)
            },
            onFailure = { throwable ->
                val apiError = throwable as? ApiError
                val code = apiError?.code ?: ""
                val isStaleSession = code in listOf("UNAUTHORIZED", "TOKEN_EXPIRED", "NOT_FOUND", "REFRESH_INVALID") ||
                        code.startsWith("HTTP_401") || code.startsWith("HTTP_404")

                if (isStaleSession) {
                    tokenStorage.clear()
                    userDao.clear()
                    creationDao.clearUserPrivateData()
                    artisanDao.clearAllWhatsappNumbers()
                    sessionManager.setUser(null)
                    sessionManager.setPendingAction(null, null)
                    Result.failure(ApiError("STALE_SESSION", "Please sign in again."))
                } else {
                    if (cachedUser != null) {
                        sessionManager.setUser(cachedUser.toDto())
                        Result.success(cachedUser.toDto())
                    } else {
                        sessionManager.setUser(null)
                        Result.failure(throwable)
                    }
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
            val user = response.data
            userDao.upsert(user.toEntity())
            sessionManager.setUser(user)
            user
        }
    }

    suspend fun logout(): Result<Unit> {
        val userId = sessionManager.user.value?.id
        val ctx = context
        return apiCall {
            val refresh = tokenStorage.refresh()
            if (!refresh.isNullOrBlank()) {
                try {
                    apiService.logout(LogoutRequest(refresh))
                } catch (_: Exception) {}
            }
            tokenStorage.clear()
            userDao.clear()
            creationDao.clearUserPrivateData()
            artisanDao.clearAllWhatsappNumbers()
            if (userId != null) {
                pendingOperationDao.deleteOperationsForUser(userId)
            }
            if (ctx != null) {
                try {
                    val pendingDir = File(ctx.filesDir, "pending_photos")
                    if (pendingDir.exists()) pendingDir.deleteRecursively()
                } catch (_: Exception) {}
                try {
                    WorkManager.getInstance(ctx).cancelUniqueWork("naep-sync")
                } catch (_: Exception) {}
            }
            sessionManager.signOutLocally()
        }
    }
}
