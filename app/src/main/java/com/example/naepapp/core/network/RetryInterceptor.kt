package com.example.naepapp.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isGet = request.method.equals("GET", ignoreCase = true)

        if (!isGet) {
            return chain.proceed(request)
        }

        val maxRetries = 2
        var currentAttempt = 0
        var response: Response? = null

        while (true) {
            currentAttempt++
            try {
                response?.close()
                response = chain.proceed(request)
                if (response.code !in listOf(502, 503, 504)) {
                    return response
                }
                response.close()
            } catch (e: Exception) {
                when (e) {
                    is SocketTimeoutException, is IOException -> {
                        if (currentAttempt > maxRetries) {
                            throw e
                        }
                    }
                    else -> throw e
                }
            }

            if (currentAttempt > maxRetries) {
                break
            }

            try {
                Thread.sleep(2000)
            } catch (_: InterruptedException) {}
        }

        return response ?: chain.proceed(request)
    }
}
