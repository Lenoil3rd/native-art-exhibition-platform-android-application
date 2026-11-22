package com.example.naepapp.data.sync

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.naepapp.BuildConfig
import com.example.naepapp.core.common.CloudinaryUploader
import com.example.naepapp.core.db.NaepDatabase
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.data.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CreationPayload(
    val id: String? = null,
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

@Singleton
class SyncEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: NaepDatabase,
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val cloudinaryUploader: CloudinaryUploader
) {
    private val mutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun drain(): Result<Unit> = mutex.withLock {
        val user = sessionManager.user.value ?: return@withLock Result.success(Unit)
        val userId = user.id

        if (BuildConfig.DEBUG) {
            Log.d("NaepSync", "SyncEngine drain started for user $userId")
        }

        while (true) {
            val ops = database.pendingOperationDao().getOperationsForUser(userId)
            if (ops.isEmpty()) break

            var anyProcessed = false

            for (op in ops) {
                if (op.status == "FAILED") continue

                if (BuildConfig.DEBUG) {
                    Log.d("NaepSync", "Processing op type=${op.type} entityId=${op.entityId}")
                }

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
                                        val uri = Uri.fromFile(file)
                                        if (BuildConfig.DEBUG) {
                                            Log.d("NaepUpload", "Uploading photo path=$path for creation=${op.entityId}")
                                        }
                                        val uploadResult = cloudinaryUploader.upload(context, uri, purpose = "creation")
                                        if (uploadResult.isSuccess) {
                                            uploadResult.getOrNull()?.let { uploadedMedia.add(it) }
                                            if (BuildConfig.DEBUG) {
                                                Log.d("NaepUpload", "Upload successful for photo path=$path")
                                            }
                                        } else {
                                            val err = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                                            if (BuildConfig.DEBUG) {
                                                Log.e("NaepUpload", "Upload failed for photo path=$path: $err")
                                            }
                                            throw Exception(err)
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

                            if (BuildConfig.DEBUG) {
                                Log.d("NaepSync", "POST /creations id=${op.entityId}")
                            }

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
                                        val uri = Uri.fromFile(file)
                                        val uploadResult = cloudinaryUploader.upload(context, uri, purpose = "creation")
                                        if (uploadResult.isSuccess) {
                                            uploadResult.getOrNull()?.let { uploadedMedia.add(it) }
                                        } else {
                                            val err = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                                            throw Exception(err)
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
                    if (BuildConfig.DEBUG) {
                        Log.d("NaepSync", "Successfully processed op type=${op.type} entityId=${op.entityId}")
                    }
                } catch (e: Exception) {
                    val isIOException = e is IOException || (e is HttpException && (e.code() in 500..599 || e.code() == 408 || e.code() == 429))
                    val isAuthError = e is HttpException && e.code() == 401
                    val isPermanentError = e is HttpException && (e.code() == 403 || e.code() == 404 || e.code() == 409 || e.code() == 422)

                    val errorMsg = e.localizedMessage ?: e.message ?: "Unknown error"
                    if (BuildConfig.DEBUG) {
                        Log.e("NaepSync", "Error processing op type=${op.type} entityId=${op.entityId}: $errorMsg", e)
                    }

                    if (isAuthError) {
                        return@withLock Result.failure(e)
                    } else if (isPermanentError || op.attemptCount >= 7) {
                        database.pendingOperationDao().updateStatus(op.id, "FAILED", op.attemptCount + 1, errorMsg, System.currentTimeMillis())
                        if (op.type in listOf("CREATE_CREATION", "UPDATE_CREATION")) {
                            database.creationDao().updateSyncState(op.entityId, "FAILED")
                        }
                    } else {
                        database.pendingOperationDao().updateStatus(op.id, "PENDING", op.attemptCount + 1, errorMsg, System.currentTimeMillis())
                    }
                }
            }
            if (!anyProcessed) break
        }
        return Result.success(Unit)
    }
}
