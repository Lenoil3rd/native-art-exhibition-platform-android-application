package com.example.naepapp.core.db.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.entity.CreationMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
interface CreationDao {

    @Query("SELECT * FROM creations WHERE id = :id")
    fun observeById(id: String): Flow<CreationEntity?>

    @Query("SELECT * FROM creations WHERE id = :id")
    suspend fun getById(id: String): CreationEntity?

    @Query("SELECT * FROM creation_media WHERE creationId = :creationId ORDER BY position ASC")
    fun observeMediaForCreation(creationId: String): Flow<List<CreationMediaEntity>>

    @Query("SELECT * FROM creation_media WHERE creationId = :creationId ORDER BY position ASC")
    suspend fun getMediaForCreation(creationId: String): List<CreationMediaEntity>

    @Query("SELECT * FROM creations WHERE artisanId = :artisanId AND syncState != 'PENDING_DELETE' ORDER BY createdAt DESC")
    fun observeMyCreations(artisanId: String): Flow<List<CreationEntity>>

    @Query("SELECT c.* FROM creations c INNER JOIN feed_entries f ON c.id = f.creationId WHERE f.queryKey = :queryKey ORDER BY f.position ASC")
    fun getFeed(queryKey: String): PagingSource<Int, CreationEntity>

    @Upsert
    suspend fun upsert(creation: CreationEntity)

    @Upsert
    suspend fun upsertAll(creations: List<CreationEntity>)

    @Upsert
    suspend fun upsertMedia(media: List<CreationMediaEntity>)

    @Query("DELETE FROM creation_media WHERE creationId = :creationId")
    suspend fun deleteMediaForCreation(creationId: String)

    @Transaction
    suspend fun saveCreationWithMedia(creation: CreationEntity, mediaList: List<CreationMediaEntity>) {
        upsert(creation)
        deleteMediaForCreation(creation.id)
        upsertMedia(mediaList)
    }

    @Query("DELETE FROM creations WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE creations SET isLikedByMe = :isLikedByMe, likesCount = :likesCount WHERE id = :id")
    suspend fun updateLikeStatus(id: String, isLikedByMe: Boolean, likesCount: Int)

    @Query("UPDATE creations SET availabilityStatus = :status WHERE id = :id")
    suspend fun updateAvailability(id: String, status: String)

    @Query("UPDATE creations SET isOwn = 1 WHERE artisanId = :artisanId")
    suspend fun updateIsOwn(artisanId: String)

    @Query("UPDATE creations SET isLikedByMe = 0, isOwn = 0")
    suspend fun clearUserPrivateData()

    @Query("UPDATE creations SET syncState = :syncState WHERE id = :id")
    suspend fun updateSyncState(id: String, syncState: String)

    @Query("""
        DELETE FROM creations 
        WHERE fetchedAt < :cutoff 
          AND isOwn = 0 
          AND id NOT IN (SELECT creationId FROM feed_entries)
    """)
    suspend fun pruneUnreferencedCreations(cutoff: Long)
}
