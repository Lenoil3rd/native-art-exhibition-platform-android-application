package com.example.naepapp.core.db.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "current_user")
data class CurrentUserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val profilePictureUrl: String?,
    val preferredMode: String,
    val onboardingCompleted: Boolean,
    val isArtisan: Boolean
)
