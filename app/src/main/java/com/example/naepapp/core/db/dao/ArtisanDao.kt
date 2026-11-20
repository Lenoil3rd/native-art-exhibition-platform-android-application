package com.example.naepapp.core.db.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import com.example.naepapp.core.db.entity.ArtisanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArtisanDao {

    @Query("SELECT * FROM artisans WHERE userId = :userId")
    fun observeById(userId: String): Flow<ArtisanEntity?>

    @Query("SELECT * FROM artisans WHERE userId = :userId")
    suspend fun getById(userId: String): ArtisanEntity?

    @Upsert
    suspend fun upsert(artisan: ArtisanEntity)

    @Query("UPDATE artisans SET whatsappNumber = NULL")
    suspend fun clearAllWhatsappNumbers()
}
