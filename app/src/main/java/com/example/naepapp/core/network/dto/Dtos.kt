package com.example.naepapp.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class Envelope<T>(
    val data: T,
    val meta: Meta? = null
)

@Serializable
data class Meta(
    val page: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val hasMore: Boolean = false
)

@Serializable
data class ErrorEnvelope(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<ValidationErrorDetail>? = null
)

@Serializable
data class ValidationErrorDetail(
    val field: String,
    val message: String,
    val code: String
)

@Serializable
data class LoginRequest(
    val identifier: String,
    val password: String,
    val platform: String = "android"
)

@Serializable
data class RegisterRequest(
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val platform: String = "android"
)

@Serializable
data class RefreshRequest(
    val refreshToken: String,
    val platform: String = "android"
)

@Serializable
data class LogoutRequest(
    val refreshToken: String
)

@Serializable
data class OnboardingRequest(
    val mode: String
)

@Serializable
data class OpenGalleryRequest(
    val bio: String? = null,
    val location: String? = null,
    val primaryCategoryId: Int? = null,
    val whatsappNumber: String? = null
)

@Serializable
data class ArtisanWriteProfileDto(
    val bio: String? = null,
    val location: String? = null,
    val primaryCategoryId: Int? = null,
    val whatsappNumber: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class UpdateMeRequest(
    val fullName: String? = null,
    val preferredMode: String? = null,
    val profilePictureUrl: String? = null,
    val profilePicturePublicId: String? = null
)

@Serializable
data class SignatureRequest(
    val folder: String? = null,
    val publicId: String? = null,
    val purpose: String? = null
)

@Serializable
data class MediaInput(
    val url: String,
    val publicId: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class CreationRequest(
    val id: String? = null,
    val title: String,
    val description: String? = null,
    val categoryId: Int,
    val dimensions: String? = null,
    val price: Double? = null,
    val currency: String = "MWK",
    val inventoryCount: Int = 1,
    val availabilityStatus: String = "available",
    val media: List<MediaInput> = emptyList()
)

@Serializable
data class AvailabilityRequest(
    val availabilityStatus: String
)

@Serializable
data class InquiryRequest(
    val message: String
)

@Serializable
data class AuthResult(
    val user: UserDto,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)

@Serializable
data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String,
    val expiresIn: Long
)

@Serializable
data class UserDto(
    val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val profilePictureUrl: String? = null,
    val preferredMode: String? = null,
    val onboardingCompleted: Boolean = false,
    val isArtisan: Boolean = false
)

@Serializable
data class CreationSummaryDto(
    val id: String,
    val artisanId: String,
    val categoryId: Int,
    val title: String,
    val description: String? = null,
    val dimensions: String? = null,
    val price: Double? = null,
    val currency: String = "MWK",
    val inventoryCount: Int = 1,
    val availabilityStatus: String = "available",
    val status: String = "active",
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val categoryName: String = "",
    val coverImageUrl: String? = null,
    val artisanName: String = "",
    val artisanProfilePictureUrl: String? = null
)

@Serializable
data class CreationDetailDto(
    val id: String,
    val artisanId: String,
    val categoryId: Int,
    val title: String,
    val description: String? = null,
    val dimensions: String? = null,
    val price: Double? = null,
    val currency: String = "MWK",
    val inventoryCount: Int = 1,
    val availabilityStatus: String = "available",
    val status: String = "active",
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val categoryName: String = "",
    val coverImageUrl: String? = null,
    val artisanName: String = "",
    val artisanProfilePictureUrl: String? = null,
    val isLikedByMe: Boolean = false,
    val isOwnCreation: Boolean = false,
    val media: List<MediaItemDto> = emptyList()
)

@Serializable
data class MediaItemDto(
    val id: String,
    val url: String,
    val publicId: String,
    val position: Int = 0,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class ArtisanReadProfileDto(
    val userId: String,
    val bio: String? = null,
    val location: String? = null,
    val primaryCategoryId: Int? = null,
    val primaryCategoryName: String? = null,
    val whatsappNumber: String? = null,
    val isActive: Boolean = true,
    val galleryOpenedAt: String? = null,
    val fullName: String = "",
    val profilePictureUrl: String? = null,
    val recentCreations: List<CreationSummaryDto> = emptyList()
)

@Serializable
data class CategoryDto(
    val id: Int,
    val name: String,
    val description: String? = null
)

@Serializable
data class LikeResult(
    val liked: Boolean,
    val likesCount: Int
)

@Serializable
data class InquiryDto(
    val id: String? = null,
    val message: String,
    val createdAt: String = "",
    val whatsappUrl: String? = null,
    val captionMessage: String? = null,
    val whatsappNumber: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class UploadSignatureDto(
    val signature: String,
    val timestamp: Long,
    val apiKey: String,
    val cloudName: String,
    val folder: String? = null,
    val publicId: String? = null
)
