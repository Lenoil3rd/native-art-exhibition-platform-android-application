package com.example.naepapp.core.storage

interface TokenStorage {
    suspend fun save(accessToken: String, refreshToken: String)
    suspend fun access(): String?
    suspend fun refresh(): String?
    suspend fun clear()
}
