package com.example.naepapp.data.repository

import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtisanRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun profile(id: String): Result<ArtisanReadProfileDto> {
        return apiCall {
            apiService.getArtisanProfile(id).data
        }
    }

    suspend fun gallery(id: String): Result<List<CreationSummaryDto>> {
        return apiCall {
            apiService.getArtisanCreations(id).data
        }
    }

    suspend fun openGallery(request: OpenGalleryRequest): Result<ArtisanReadProfileDto> {
        return apiCall {
            val res = apiService.openGallery(request).data
            refreshMe()
            res
        }
    }

    suspend fun updateProfile(request: ArtisanWriteProfileDto): Result<ArtisanReadProfileDto> {
        return apiCall {
            val res = apiService.updateArtisanProfile(request).data
            refreshMe()
            res
        }
    }

    suspend fun refreshMe(): Result<UserDto> {
        return apiCall {
            val res = apiService.getMe().data
            sessionManager.setUser(res)
            res
        }
    }

    suspend fun categories(): Result<List<CategoryDto>> {
        return apiCall {
            apiService.getCategories().data
        }
    }
}
