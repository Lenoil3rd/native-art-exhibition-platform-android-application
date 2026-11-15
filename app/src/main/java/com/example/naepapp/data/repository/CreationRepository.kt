package com.example.naepapp.data.repository

import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDetail(id: String): Result<CreationDetailDto> {
        return apiCall {
            apiService.getCreationDetail(id).data
        }
    }

    suspend fun getMyCreations(): Result<List<CreationSummaryDto>> {
        return apiCall {
            apiService.getMyCreations().data
        }
    }

    suspend fun recordView(id: String) {
        try {
            apiService.viewCreation(id)
        } catch (_: Exception) {}
    }

    suspend fun like(id: String): Result<LikeResult> {
        return apiCall {
            apiService.likeCreation(id).data
        }
    }

    suspend fun unlike(id: String): Result<LikeResult> {
        return apiCall {
            apiService.unlikeCreation(id).data
        }
    }

    suspend fun updateAvailability(id: String, status: String): Result<CreationDetailDto> {
        return apiCall {
            apiService.updateAvailability(id, AvailabilityRequest(status)).data
        }
    }

    suspend fun inquire(id: String, message: String): Result<InquiryDto> {
        return apiCall {
            apiService.sendInquiry(id, InquiryRequest(message)).data
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        return apiCall {
            apiService.deleteCreation(id)
            Unit
        }
    }

    suspend fun categories(): Result<List<CategoryDto>> {
        return apiCall {
            apiService.getCategories().data
        }
    }

    suspend fun create(request: CreationRequest): Result<CreationDetailDto> {
        return apiCall {
            apiService.createCreation(request).data
        }
    }

    suspend fun update(id: String, request: CreationRequest): Result<CreationDetailDto> {
        return apiCall {
            apiService.updateCreation(id, request).data
        }
    }
}
