package com.example.naepapp.data.sync

import android.content.Context
import androidx.core.content.FileProvider
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.naepapp.core.common.CloudinaryUploader
import com.example.naepapp.core.db.NaepDatabase
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.core.storage.TokenStorage
import com.example.naepapp.data.session.SessionManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.File
import java.io.IOException

@Serializable
data class CreationPayload(
    val title: String,
    val description: String? = null,
    val categoryId: Int,
    val dimensions: String? = null,
    val price: Double? = null,
    val currency: String = "MWK",
    val inventoryCount: Int = 1,
    val availabilityStatus: String = "available",
    val media: List<MediaInput> = emptyList(),
    val localPhotoPaths: List<String> = emptyList()
)

@Serializable
data class LikePayload(val liked: Boolean)

@Serializable
data class AvailabilityPayload(val status: String)

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: NaepDatabase,
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val tokenStorage: TokenStorage,
    private val cloudinaryUploader: CloudinaryUploader
) : CoroutineWorker(appContext, workerParams) {

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    override suspend fun doWork(): Result {
        val user = sessionManager.user.value ?: return Result.success()
        val userId = user.id

        while (true) {
            val ops = database.pendingOperationDao().getOperationsForUser(userId)
            if (ops.isEmpty()) break

            var anyProcessed = false

            for (op in ops) {
                if (op.status == "FAILED") continue

                try {
                    when (op.type) {
                        "LIKE_SET" -> {
                            val payload = json.decodeFromString<LikePayload>(op.payloadJson)
                            if (payload.liked) {
                                val res = apiService.likeCreation(op.entityId)
                                database.creationDao().updateLikeStatus(op.entityId, true, res.data.likesCount)
                            } else {
                                val res = apiService.unlikeCreation(op.entityId)
                                database.creationDao().updateLikeStatus(op.entityId, false, res.data.likesCount)
                            }
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                        "AVAILABILITY_SET" -> {
                            val payload = json.decodeFromString<AvailabilityPayload>(op.payloadJson)
                            apiService.updateAvailability(op.entityId, AvailabilityRequest(payload.status))
                            database.creationDao().updateAvailability(op.entityId, payload.status)
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                        "CREATE_CREATION" -> {
                            val payload = json.decodeFromString<CreationPayload>(op.payloadJson)
                            val uploadedMedia = payload.media.toMutableList()

                            if (payload.localPhotoPaths.isNotEmpty()) {
                                for (path in payload.localPhotoPaths) {
                                    val file = File(path)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            applicationContext,
                                            "${applicationContext.packageName}.fileprovider",
                                            file
                                        )
                                        val uploadResult = cloudinaryUploader.upload(applicationContext, uri)
                                        if (uploadResult.isSuccess) {
                                            uploadResult.getOrNull()?.let { uploadedMedia.add(it) }
                                        }
                                    }
                                }
                            }

                            val request = CreationRequest(
                                id = op.entityId,
                                title = payload.title,
                                description = payload.description,
                                categoryId = payload.categoryId,
                                dimensions = payload.dimensions,
                                price = payload.price,
                                currency = payload.currency,
                                inventoryCount = payload.inventoryCount,
                                availabilityStatus = payload.availabilityStatus,
                                media = uploadedMedia
                            )

                            val response = apiService.createCreation(request)
                            val detail = response.data

                            payload.localPhotoPaths.forEach { path ->
                                try { File(path).delete() } catch (_: Exception) {}
                            }

                            val now = System.currentTimeMillis()
                            val creationEntity = CreationEntity(
                                id = detail.id,
                                artisanId = detail.artisanId,
                                categoryId = detail.categoryId,
                                categoryName = detail.categoryName,
                                title = detail.title,
                                description = detail.description.orEmpty(),
                                dimensions = detail.dimensions.orEmpty(),
                                price = detail.price,
                                currency = detail.currency,
                                availabilityStatus = detail.availabilityStatus,
                                status = detail.status,
                                viewsCount = detail.viewsCount,
                                likesCount = detail.likesCount,
                                createdAt = detail.createdAt,
                                updatedAt = detail.updatedAt,
                                coverImageUrl = detail.coverImageUrl ?: detail.media.firstOrNull()?.url,
                                artisanName = detail.artisanName,
                                artisanProfilePictureUrl = detail.artisanProfilePictureUrl,
                                isLikedByMe = detail.isLikedByMe,
                                isOwn = true,
                                fetchedAt = now,
                                syncState = "SYNCED"
                            )
                            database.creationDao().saveCreationWithMedia(creationEntity, detail.media.map { it.toEntity(detail.id) })
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                        "UPDATE_CREATION" -> {
                            val payload = json.decodeFromString<CreationPayload>(op.payloadJson)
                            val uploadedMedia = payload.media.toMutableList()
                            if (payload.localPhotoPaths.isNotEmpty()) {
                                for (path in payload.localPhotoPaths) {
                                    val file = File(path)
                                    if (file.exists()) {
                                        val uri = FileProvider.getUriForFile(
                                            applicationContext,
                                            "${applicationContext.packageName}.fileprovider",
                                            file
                                        )
                                        val uploadResult = cloudinaryUploader.upload(applicationContext, uri)
                                        if (uploadResult.isSuccess) {
                                            uploadResult.getOrNull()?.let { uploadedMedia.add(it) }
                                        }
                                    }
                                }
                            }
                            val request = CreationRequest(
                                title = payload.title,
                                description = payload.description,
                                categoryId = payload.categoryId,
                                dimensions = payload.dimensions,
                                price = payload.price,
                                currency = payload.currency,
                                inventoryCount = payload.inventoryCount,
                                availabilityStatus = payload.availabilityStatus,
                                media = uploadedMedia
                            )
                            val response = apiService.updateCreation(op.entityId, request)
                            val detail = response.data
                            payload.localPhotoPaths.forEach { path ->
                                try { File(path).delete() } catch (_: Exception) {}
                            }
                            val now = System.currentTimeMillis()
                            val existing = database.creationDao().getById(op.entityId)
                            val creationEntity = CreationEntity(
                                id = detail.id,
                                artisanId = detail.artisanId,
                                categoryId = detail.categoryId,
                                categoryName = detail.categoryName,
                                title = detail.title,
                                description = detail.description.orEmpty(),
                                dimensions = detail.dimensions.orEmpty(),
                                price = detail.price,
                                currency = detail.currency,
                                availabilityStatus = detail.availabilityStatus,
                                status = detail.status,
                                viewsCount = detail.viewsCount,
                                likesCount = detail.likesCount,
                                createdAt = detail.createdAt,
                                updatedAt = detail.updatedAt,
                                coverImageUrl = detail.coverImageUrl ?: detail.media.firstOrNull()?.url,
                                artisanName = detail.artisanName,
                                artisanProfilePictureUrl = detail.artisanProfilePictureUrl,
                                isLikedByMe = detail.isLikedByMe,
                                isOwn = true,
                                fetchedAt = now,
                                syncState = "SYNCED"
                            )
                            database.creationDao().saveCreationWithMedia(creationEntity, detail.media.map { it.toEntity(detail.id) })
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                        "DELETE_CREATION" -> {
                            try {
                                apiService.deleteCreation(op.entityId)
                            } catch (e: HttpException) {
                                if (e.code() != 404 && e.code() != 204) throw e
                            }
                            database.creationDao().deleteMediaForCreation(op.entityId)
                            database.creationDao().deleteById(op.entityId)
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                        "RECORD_VIEW" -> {
                            apiService.viewCreation(op.entityId)
                            database.pendingOperationDao().deleteOperation(op.id)
                        }
                    }
                    anyProcessed = true
                } catch (e: Exception) {
                    val isIOException = e is IOException || (e is HttpException && (e.code() in 500..599 || e.code() == 408 || e.code() == 429))
                    val isAuthError = e is HttpException && e.code() == 401
                    val isPermanentError = e is HttpException && (e.code() == 403 || e.code() == 404 || e.code() == 409 || e.code() == 422)

                    if (isAuthError) {
                        return Result.retry()
                    } else if (isIOException) {
                        val newAttempts = op.attemptCount + 1
                        if (newAttempts >= 8) {
                            database.pendingOperationDao().updateStatus(op.id, "FAILED", newAttempts, e.message, System.currentTimeMillis())
                            if (op.type in listOf("CREATE_CREATION", "UPDATE_CREATION")) {
                                database.creationDao().updateSyncState(op.entityId, "FAILED")
                            }
                        } else {
                            database.pendingOperationDao().updateStatus(op.id, "PENDING", newAttempts, e.message, System.currentTimeMillis())
                            return Result.retry()
                        }
                    } else if (isPermanentError) {
                        database.pendingOperationDao().updateStatus(op.id, "FAILED", op.attemptCount + 1, e.message, System.currentTimeMillis())
                        if (op.type in listOf("CREATE_CREATION", "UPDATE_CREATION")) {
                            database.creationDao().updateSyncState(op.entityId, "FAILED")
                        }
                    } else {
                        val newAttempts = op.attemptCount + 1
                        if (newAttempts >= 8) {
                            database.pendingOperationDao().updateStatus(op.id, "FAILED", newAttempts, e.message, System.currentTimeMillis())
                        } else {
                            database.pendingOperationDao().updateStatus(op.id, "PENDING", newAttempts, e.message, System.currentTimeMillis())
                            return Result.retry()
                        }
                    }
                }
            }

            if (!anyProcessed) break
        }

        return Result.success()
    }
}
