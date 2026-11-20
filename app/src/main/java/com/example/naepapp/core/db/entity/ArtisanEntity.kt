package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "artisans")
data class ArtisanEntity(
    @PrimaryKey val userId: String,
    val fullName: String,
    val profilePictureUrl: String?,
    val bio: String?,
    val location: String?,
    val primaryCategoryId: Int?,
    val primaryCategoryName: String?,
    val whatsappNumber: String?,
    val galleryOpenedAt: String?,
    val fetchedAt: Long
)
