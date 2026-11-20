package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "creations")
data class CreationEntity(
    @PrimaryKey val id: String,
    val artisanId: String,
    val categoryId: Int,
    val categoryName: String,
    val title: String,
    val description: String?,
    val dimensions: String?,
    val price: Double?,
    val currency: String,
    val availabilityStatus: String,
    val status: String,
    val viewsCount: Int,
    val likesCount: Int,
    val createdAt: String,
    val updatedAt: String,
    val coverImageUrl: String?,
    val artisanName: String,
    val artisanProfilePictureUrl: String?,
    val isLikedByMe: Boolean,
    val isOwn: Boolean,
    val fetchedAt: Long,
    val syncState: String = "SYNCED"
)
