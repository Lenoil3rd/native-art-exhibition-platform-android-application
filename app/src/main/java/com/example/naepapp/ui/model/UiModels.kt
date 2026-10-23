package com.example.naepapp.ui.model

import com.example.naepapp.core.network.dto.UserDto

object DemoConfig {
    const val SKIP_AUTH_VALIDATION = false
}

enum class FeedSort { Likes, Views }

data class CategoryUi(
    val id: Int,
    val name: String,
    val bio: String = "",
    val category: String = "",
    val whatsapp: String = ""
)

data class ArtisanUi(
    val id: String,
    val name: String,
    val location: String,
    val bio: String,
    val categoryId: Int,
    val categoryName: String,
    val whatsapp: String,
    val imageUrl: String = ""
)

data class CreationUi(
    val id: String,
    val title: String,
    val categoryId: Int,
    val categoryName: String,
    val artisanId: String,
    val artisanName: String,
    val price: Double?,
    val currency: String = "MWK",
    val available: Boolean,
    val likes: Int,
    val views: Int,
    val imageUrl: String = "",
    val dimensions: String,
    val description: String,
    val photoCount: Int = 3,
)

data class UserUi(
    var name: String,
    val email: String,
    val phone: String,
    val role: String = "Enthusiast",
    val artisanId: String? = null,
    val onboardingDone: Boolean = true,
) {
    val isArtisan get() = artisanId != null
}

fun UserDto?.toUi(): UserUi? {
    if (this == null) return null
    return UserUi(
        name = fullName,
        email = email,
        phone = phoneNumber,
        role = if (isArtisan) "Artisan" else "Enthusiast",
        artisanId = if (isArtisan) id else null,
        onboardingDone = onboardingCompleted
    )
}
