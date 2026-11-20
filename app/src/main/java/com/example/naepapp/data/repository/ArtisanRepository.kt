package com.example.naepapp.data.repository

import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.db.dao.ArtisanDao
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.toDto
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.db.toSummaryDto
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArtisanRepository @Inject constructor(
    private val apiService: ApiService,
    private val artisanDao: ArtisanDao,
    private val creationDao: CreationDao,
    private val sessionManager: SessionManager
) {
    fun observeProfile(id: String): Flow<ArtisanReadProfileDto?> {
        return artisanDao.observeById(id).map { entity ->
            entity?.toDto()
        }
    }

    suspend fun profile(id: String): Result<ArtisanReadProfileDto> {
        val result = apiCall {
            apiService.getArtisanProfile(id).data
        }
        val isGuest = sessionManager.user.value == null
        return result.fold(
            onSuccess = { dto ->
                artisanDao.upsert(dto.toEntity(isGuest = isGuest))
                Result.success(dto)
            },
            onFailure = { throwable ->
                val cached = artisanDao.getById(id)
                if (cached != null) {
                    Result.success(cached.toDto())
                } else {
                    Result.failure(throwable)
                }
            }
        )
    }

    fun observeGallery(id: String): Flow<List<CreationSummaryDto>> {
        return creationDao.observeMyCreations(id).map { entities ->
            entities.map { it.toSummaryDto() }
        }
    }

    suspend fun gallery(id: String): Result<List<CreationSummaryDto>> {
        val result = apiCall {
            apiService.getArtisanCreations(id).data
        }
        return result.fold(
            onSuccess = { dtos ->
                val now = System.currentTimeMillis()
                val entities = dtos.map { dto ->
                    val existing = creationDao.getById(dto.id)
                    CreationEntity(
                        id = dto.id,
                        artisanId = dto.artisanId,
                        categoryId = dto.categoryId,
                        categoryName = dto.categoryName,
                        title = dto.title,
                        description = dto.description.orEmpty(),
                        dimensions = dto.dimensions.orEmpty(),
                        price = dto.price,
                        currency = dto.currency,
                        availabilityStatus = dto.availabilityStatus,
                        status = dto.status,
                        viewsCount = dto.viewsCount,
                        likesCount = dto.likesCount,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt,
                        coverImageUrl = dto.coverImageUrl,
                        artisanName = dto.artisanName,
                        artisanProfilePictureUrl = dto.artisanProfilePictureUrl,
                        isLikedByMe = existing?.isLikedByMe ?: false,
                        isOwn = existing?.isOwn ?: false,
                        fetchedAt = now
                    )
                }
                creationDao.upsertAll(entities)
                Result.success(dtos)
            },
            onFailure = { throwable ->
                val cached = creationDao.observeMyCreations(id).first()
                if (cached.isNotEmpty()) {
                    Result.success(cached.map { it.toSummaryDto() })
                } else {
                    Result.failure(throwable)
                }
            }
        )
    }

    suspend fun openGallery(request: OpenGalleryRequest): Result<ArtisanReadProfileDto> {
        return apiCall {
            val res = apiService.openGallery(request).data
            artisanDao.upsert(res.toEntity(isGuest = sessionManager.user.value == null))
            refreshMe()
            res
        }
    }

    suspend fun updateProfile(request: ArtisanWriteProfileDto): Result<ArtisanReadProfileDto> {
        return apiCall {
            val res = apiService.updateArtisanProfile(request).data
            artisanDao.upsert(res.toEntity(isGuest = sessionManager.user.value == null))
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
