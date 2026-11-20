package com.example.naepapp.core.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.example.naepapp.core.db.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {
    @Query("SELECT * FROM pending_operations WHERE userId = :userId ORDER BY createdAt ASC")
    suspend fun getOperationsForUser(userId: String): List<PendingOperationEntity>

    @Query("SELECT * FROM pending_operations WHERE userId = :userId ORDER BY createdAt ASC")
    fun observeOperationsForUser(userId: String): Flow<List<PendingOperationEntity>>

    @Query("SELECT * FROM pending_operations WHERE id = :id")
    suspend fun getOperationById(id: Long): PendingOperationEntity?

    @Upsert
    suspend fun upsert(operation: PendingOperationEntity): Long

    @Query("DELETE FROM pending_operations WHERE id = :id")
    suspend fun deleteOperation(id: Long)

    @Query("DELETE FROM pending_operations WHERE userId = :userId")
    suspend fun deleteOperationsForUser(userId: String)

    @Query("DELETE FROM pending_operations WHERE userId = :userId AND type = :type AND entityId = :entityId")
    suspend fun deleteOperationByTypeAndEntity(userId: String, type: String, entityId: String)

    @Query("UPDATE pending_operations SET status = :status, attemptCount = :attemptCount, lastError = :lastError, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, attemptCount: Int, lastError: String?, updatedAt: Long)
}
