package com.example.naepapp.core.network

import com.example.naepapp.core.storage.TokenStorage
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStorage: TokenStorage
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStorage.access() }
        val requestBuilder = chain.request().newBuilder()
            .header("X-Client-Platform", "android")
        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}
