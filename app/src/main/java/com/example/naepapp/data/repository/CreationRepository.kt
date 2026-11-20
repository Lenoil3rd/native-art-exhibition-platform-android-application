package com.example.naepapp.data.repository

import android.content.Context
import android.net.Uri
import com.example.naepapp.core.common.OutboxPlanner
import com.example.naepapp.core.common.PhotoCompressor
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.dao.PendingOperationDao
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.entity.CreationMediaEntity
import com.example.naepapp.core.db.entity.PendingOperationEntity
import com.example.naepapp.core.db.toDetailDto
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.db.toSummaryDto
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.data.sync.CreationPayload
import com.example.naepapp.data.sync.LikePayload
import com.example.naepapp.data.sync.AvailabilityPayload
import com.example.naepapp.data.sync.SyncScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreationRepository @Inject constructor(
    @ApplicationContext private val context: Context?,
    private val apiService: ApiService,
    private val creationDao: CreationDao,
    private val pendingOperationDao: PendingOperationDao,
    private val sessionManager: SessionManager,
    private val syncScheduler: SyncScheduler
) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    private val outboxPlanner = OutboxPlanner()

    fun observeDetail(id: String): Flow<CreationDetailDto?> {
        return combine(
            creationDao.observeById(id),
            creationDao.observeMediaForCreation(id)
        ) { entity, mediaList ->
            entity?.toDetailDto(mediaList)
        }
    }

    suspend fun getDetail(id: String): Result<CreationDetailDto> {
        val result = apiCall {
            apiService.getCreationDetail(id).data
        }
        return result.fold(
            onSuccess = { dto ->
                val now = System.currentTimeMillis()
                val existing = creationDao.getById(id)
                val entity = CreationEntity(
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
                    coverImageUrl = dto.coverImageUrl ?: dto.media.firstOrNull()?.url,
                    artisanName = dto.artisanName,
                    artisanProfilePictureUrl = dto.artisanProfilePictureUrl,
                    isLikedByMe = dto.isLikedByMe,
                    isOwn = dto.isOwnCreation,
                    fetchedAt = now,
                    syncState = existing?.syncState ?: "SYNCED"
                )
                val mediaEntities = dto.media.map { it.toEntity(dto.id) }
                creationDao.saveCreationWithMedia(entity, mediaEntities)
                Result.success(dto)
            },
            onFailure = { throwable ->
                val cachedEntity = creationDao.getById(id)
                if (cachedEntity != null) {
                    val mediaList = creationDao.getMediaForCreation(id)
                    Result.success(cachedEntity.toDetailDto(mediaList))
                } else {
                    Result.failure(throwable)
                }
            }
        )
    }

    fun observeMyCreations(artisanId: String): Flow<List<CreationSummaryDto>> {
        return creationDao.observeMyCreations(artisanId).combine(creationDao.observeById(artisanId)) { list, _ ->
            list.map { it.toSummaryDto() }
        }
    }

    suspend fun getMyCreations(): Result<List<CreationSummaryDto>> {
        val result = apiCall {
            apiService.getMyCreations().data
        }
        val currentUserId = sessionManager.user.value?.id.orEmpty()
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
                        isOwn = true,
                        fetchedAt = now,
                        syncState = existing?.syncState ?: "SYNCED"
                    )
                }
                creationDao.upsertAll(entities)
                Result.success(dtos)
            },
            onFailure = { throwable ->
                if (currentUserId.isNotBlank()) {
                    val cachedList = creationDao.observeMyCreations(currentUserId).first().map { it.toSummaryDto() }
                    Result.success(cachedList)
                } else {
                    Result.failure(throwable)
                }
            }
        )
    }

    suspend fun recordView(id: String) {
        val userId = sessionManager.user.value?.id ?: return
        try {
            apiService.viewCreation(id)
        } catch (_: Exception) {
            val existingOps = pendingOperationDao.getOperationsForUser(userId)
            val newOp = PendingOperationEntity(
                userId = userId,
                type = "RECORD_VIEW",
                entityId = id,
                payloadJson = "{}"
            )
            outboxPlanner.planOperation(existingOps, newOp)
            pendingOperationDao.deleteOperationByTypeAndEntity(userId, "RECORD_VIEW", id)
            pendingOperationDao.upsert(newOp)
            syncScheduler.triggerSync()
        }
    }

    suspend fun like(id: String): Result<LikeResult> {
        val userId = sessionManager.user.value?.id ?: "guest"
        val existing = creationDao.getById(id)
        val currentLikes = existing?.likesCount ?: 0
        val newLikes = currentLikes + 1

        creationDao.updateLikeStatus(id, true, newLikes)

        val existingOps = pendingOperationDao.getOperationsForUser(userId)
        val newOp = PendingOperationEntity(
            userId = userId,
            type = "LIKE_SET",
            entityId = id,
            payloadJson = json.encodeToString(LikePayload.serializer(), LikePayload(true))
        )
        outboxPlanner.planOperation(existingOps, newOp)
        pendingOperationDao.deleteOperationByTypeAndEntity(userId, "LIKE_SET", id)
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        val res = apiCall { apiService.likeCreation(id).data }
        res.onSuccess {
            creationDao.updateLikeStatus(id, true, it.likesCount)
            pendingOperationDao.deleteOperationByTypeAndEntity(userId, "LIKE_SET", id)
        }
        return res.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                Result.success(LikeResult(liked = true, likesCount = newLikes))
            }
        )
    }

    suspend fun unlike(id: String): Result<LikeResult> {
        val userId = sessionManager.user.value?.id ?: "guest"
        val existing = creationDao.getById(id)
        val currentLikes = existing?.likesCount ?: 1
        val newLikes = (currentLikes - 1).coerceAtLeast(0)

        creationDao.updateLikeStatus(id, false, newLikes)

        val existingOps = pendingOperationDao.getOperationsForUser(userId)
        val newOp = PendingOperationEntity(
            userId = userId,
            type = "LIKE_SET",
            entityId = id,
            payloadJson = json.encodeToString(LikePayload.serializer(), LikePayload(false))
        )
        outboxPlanner.planOperation(existingOps, newOp)
        pendingOperationDao.deleteOperationByTypeAndEntity(userId, "LIKE_SET", id)
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        val res = apiCall { apiService.unlikeCreation(id).data }
        res.onSuccess {
            creationDao.updateLikeStatus(id, false, it.likesCount)
            pendingOperationDao.deleteOperationByTypeAndEntity(userId, "LIKE_SET", id)
        }
        return res.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                Result.success(LikeResult(liked = false, likesCount = newLikes))
            }
        )
    }

    suspend fun updateAvailability(id: String, status: String): Result<CreationDetailDto> {
        val userId = sessionManager.user.value?.id ?: "guest"
        creationDao.updateAvailability(id, status)

        val existingOps = pendingOperationDao.getOperationsForUser(userId)
        val newOp = PendingOperationEntity(
            userId = userId,
            type = "AVAILABILITY_SET",
            entityId = id,
            payloadJson = json.encodeToString(AvailabilityPayload.serializer(), AvailabilityPayload(status))
        )
        outboxPlanner.planOperation(existingOps, newOp)
        pendingOperationDao.deleteOperationByTypeAndEntity(userId, "AVAILABILITY_SET", id)
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        val res = apiCall { apiService.updateAvailability(id, AvailabilityRequest(status)).data }
        res.onSuccess {
            pendingOperationDao.deleteOperationByTypeAndEntity(userId, "AVAILABILITY_SET", id)
        }
        return res.fold(
            onSuccess = { Result.success(it) },
            onFailure = {
                val cached = creationDao.getById(id)
                val media = creationDao.getMediaForCreation(id)
                if (cached != null) {
                    Result.success(cached.toDetailDto(media))
                } else {
                    Result.failure(it)
                }
            }
        )
    }

    suspend fun inquire(id: String, message: String): Result<InquiryDto> {
        return apiCall {
            apiService.sendInquiry(id, InquiryRequest(message)).data
        }
    }

    suspend fun delete(id: String): Result<Unit> {
        val userId = sessionManager.user.value?.id ?: "guest"
        val existing = creationDao.getById(id)

        if (existing?.syncState == "PENDING_CREATE") {
            try {
                if (context != null) {
                    val dir = File(context.filesDir, "pending_photos/$id")
                    if (dir.exists()) dir.deleteRecursively()
                }
            } catch (_: Exception) {}
            creationDao.deleteMediaForCreation(id)
            creationDao.deleteById(id)
            val ops = pendingOperationDao.getOperationsForUser(userId)
            ops.filter { it.entityId == id }.forEach { pendingOperationDao.deleteOperation(it.id) }
            return Result.success(Unit)
        }

        creationDao.deleteMediaForCreation(id)
        creationDao.deleteById(id)

        val existingOps = pendingOperationDao.getOperationsForUser(userId)
        val newOp = PendingOperationEntity(
            userId = userId,
            type = "DELETE_CREATION",
            entityId = id,
            payloadJson = "{}"
        )
        outboxPlanner.planOperation(existingOps, newOp)
        existingOps.filter { it.entityId == id }.forEach { pendingOperationDao.deleteOperation(it.id) }
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        val res = apiCall {
            apiService.deleteCreation(id)
            Unit
        }
        res.onSuccess {
            pendingOperationDao.deleteOperationByTypeAndEntity(userId, "DELETE_CREATION", id)
        }
        return Result.success(Unit)
    }

    suspend fun categories(): Result<List<CategoryDto>> {
        return apiCall {
            apiService.getCategories().data
        }
    }

    suspend fun create(request: CreationRequest, localUris: List<Uri> = emptyList()): Result<CreationDetailDto> {
        val userId = sessionManager.user.value?.id ?: "guest"
        val creationId = request.id ?: UUID.randomUUID().toString()

        val localPhotoPaths = mutableListOf<String>()
        localUris.forEachIndexed { index, uri ->
            if (context != null) {
                PhotoCompressor.compressAndSaveToPrivateDir(context, uri, creationId, index)?.let {
                    localPhotoPaths.add(it)
                }
            }
        }

        val now = System.currentTimeMillis()
        val creationEntity = CreationEntity(
            id = creationId,
            artisanId = userId,
            categoryId = request.categoryId,
            categoryName = "",
            title = request.title,
            description = request.description,
            dimensions = request.dimensions,
            price = request.price,
            currency = request.currency,
            availabilityStatus = request.availabilityStatus,
            status = "published",
            viewsCount = 0,
            likesCount = 0,
            createdAt = System.currentTimeMillis().toString(),
            updatedAt = System.currentTimeMillis().toString(),
            coverImageUrl = localPhotoPaths.firstOrNull() ?: request.media.firstOrNull()?.url,
            artisanName = sessionManager.user.value?.fullName.orEmpty(),
            artisanProfilePictureUrl = sessionManager.user.value?.profilePictureUrl,
            isLikedByMe = false,
            isOwn = true,
            fetchedAt = now,
            syncState = "PENDING_CREATE"
        )

        val mediaEntities = request.media.mapIndexed { index, m ->
            CreationMediaEntity(
                id = UUID.randomUUID().toString(),
                creationId = creationId,
                url = m.url,
                publicId = m.publicId,
                position = index,
                width = m.width,
                height = m.height
            )
        } + localPhotoPaths.mapIndexed { index, path ->
            CreationMediaEntity(
                id = UUID.randomUUID().toString(),
                creationId = creationId,
                url = path,
                publicId = "local_$index",
                position = request.media.size + index,
                width = null,
                height = null,
                localPath = path
            )
        }

        creationDao.saveCreationWithMedia(creationEntity, mediaEntities)

        val payload = CreationPayload(
            title = request.title,
            description = request.description,
            categoryId = request.categoryId,
            dimensions = request.dimensions,
            price = request.price,
            currency = request.currency,
            inventoryCount = request.inventoryCount,
            availabilityStatus = request.availabilityStatus,
            media = request.media,
            localPhotoPaths = localPhotoPaths
        )

        val newOp = PendingOperationEntity(
            userId = userId,
            type = "CREATE_CREATION",
            entityId = creationId,
            payloadJson = json.encodeToString(CreationPayload.serializer(), payload)
        )
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        return Result.success(creationEntity.toDetailDto(mediaEntities))
    }

    suspend fun update(id: String, request: CreationRequest, localUris: List<Uri> = emptyList()): Result<CreationDetailDto> {
        val userId = sessionManager.user.value?.id ?: "guest"

        val localPhotoPaths = mutableListOf<String>()
        localUris.forEachIndexed { index, uri ->
            if (context != null) {
                PhotoCompressor.compressAndSaveToPrivateDir(context, uri, id, index)?.let {
                    localPhotoPaths.add(it)
                }
            }
        }

        creationDao.updateSyncState(id, "PENDING_UPDATE")

        val payload = CreationPayload(
            title = request.title,
            description = request.description,
            categoryId = request.categoryId,
            dimensions = request.dimensions,
            price = request.price,
            currency = request.currency,
            inventoryCount = request.inventoryCount,
            availabilityStatus = request.availabilityStatus,
            media = request.media,
            localPhotoPaths = localPhotoPaths
        )

        val existingOps = pendingOperationDao.getOperationsForUser(userId)
        val newOp = PendingOperationEntity(
            userId = userId,
            type = "UPDATE_CREATION",
            entityId = id,
            payloadJson = json.encodeToString(CreationPayload.serializer(), payload)
        )
        outboxPlanner.planOperation(existingOps, newOp)
        existingOps.filter { it.entityId == id && it.type != "CREATE_CREATION" }.forEach { pendingOperationDao.deleteOperation(it.id) }
        pendingOperationDao.upsert(newOp)
        syncScheduler.triggerSync()

        val cached = creationDao.getById(id)
        val media = creationDao.getMediaForCreation(id)
        return Result.success(cached?.toDetailDto(media) ?: CreationDetailDto(id = id, artisanId = userId, categoryId = request.categoryId, title = request.title))
    }
}
