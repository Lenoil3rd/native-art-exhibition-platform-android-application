package com.example.naepapp.core.network

import android.content.Context
import com.example.naepapp.core.network.dto.RefreshRequest
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val refreshApiProvider: Provider<RefreshApi>,
    private val tokenStorage: TokenStorage,
    private val sessionManager: SessionManager
) : Authenticator {
    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val path = response.request.url.encodedPath
        if (path.contains("/auth/")) return null

        if (response.priorResponse != null && response.priorResponse?.code == 401) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val currentAccess = tokenStorage.access()
                val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
                if (!currentAccess.isNullOrBlank() && currentAccess != requestToken) {
                    return@withLock response.request.newBuilder()
                        .header("Authorization", "Bearer $currentAccess")
                        .build()
                }

                val currentRefresh = tokenStorage.refresh()
                if (currentRefresh.isNullOrBlank()) {
                    sessionManager.signOutLocally()
                    return@withLock null
                }

                try {
                    val result = refreshApiProvider.get().refresh(RefreshRequest(currentRefresh, "android"))
                    val pair = result.data
                    tokenStorage.save(pair.accessToken, pair.refreshToken)
                    response.request.newBuilder()
                        .header("Authorization", "Bearer ${pair.accessToken}")
                        .build()
                } catch (e: Exception) {
                    tokenStorage.clear()
                    sessionManager.signOutLocally()
                    null
                }
            }
        }
    }
}
