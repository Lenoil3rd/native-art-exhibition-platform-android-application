package com.example.naepapp.core.common

object ErrorMapper {
    fun getMessage(error: Throwable?): String {
        val apiError = error as? ApiError ?: return error?.localizedMessage ?: "Something went wrong"
        return when (apiError.code) {
            "NETWORK" -> "Can't reach the server. Check your connection."
            "RATE_LIMITED" -> "Too many requests. Please try again later."
            "UNAUTHORIZED", "TOKEN_EXPIRED" -> "Please sign in again"
            "VALIDATION_ERROR" -> apiError.message.ifBlank { "Validation error" }
            "NOT_FOUND" -> "Resource not found"
            else -> apiError.message.ifBlank { "Something went wrong" }
        }
    }
}
