package com.example.naepapp.core.common

import com.example.naepapp.core.network.dto.ErrorEnvelope
import com.example.naepapp.core.network.dto.ValidationErrorDetail
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

data class ApiError(
    val code: String,
    override val message: String,
    val details: List<ValidationErrorDetail>? = null
) : Throwable(message)

suspend inline fun <T> apiCall(crossinline block: suspend () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        var code = "HTTP_${e.code()}"
        var message = e.message() ?: "Server error"
        var details: List<ValidationErrorDetail>? = null

        if (!errorBody.isNullOrBlank()) {
            try {
                val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
                val errorEnvelope = json.decodeFromString<ErrorEnvelope>(errorBody)
                code = errorEnvelope.error.code
                message = errorEnvelope.error.message
                details = errorEnvelope.error.details
            } catch (_: Exception) {}
        }
        Result.failure(ApiError(code, message, details))
    } catch (e: SocketTimeoutException) {
        Result.failure(ApiError("TIMEOUT", "The server is taking too long to respond. Please try again."))
    } catch (e: IOException) {
        Result.failure(ApiError("NETWORK", "Can't reach the server. Check your connection."))
    } catch (e: SerializationException) {
        Result.failure(ApiError("BAD_RESPONSE", "Invalid response from server."))
    } catch (e: Throwable) {
        Result.failure(ApiError("UNKNOWN", e.localizedMessage ?: "Unknown error occurred."))
    }
}
