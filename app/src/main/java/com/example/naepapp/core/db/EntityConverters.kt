package com.example.naepapp.core.db

import com.example.naepapp.core.db.entity.*
import com.example.naepapp.core.network.dto.*
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.CreationUi

fun CurrentUserEntity.toDto(): UserDto {
    return UserDto(
        id = id,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        preferredMode = preferredMode,
        onboardingCompleted = onboardingCompleted,
        isArtisan = isArtisan
    )
}

fun UserDto.toEntity(): CurrentUserEntity {
    return CurrentUserEntity(
        id = id,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        profilePictureUrl = profilePictureUrl,
        preferredMode = preferredMode ?: "enthusiast",
        onboardingCompleted = onboardingCompleted,
        isArtisan = isArtisan
    )
}

fun CategoryEntity.toUi(): CategoryUi {
    return CategoryUi(
        id = id,
        name = name,
        bio = ""
    )
}

fun CategoryDto.toEntity(): CategoryEntity {
    return CategoryEntity(
        id = id,
        slug = name.lowercase().replace(" ", "-"),
        name = name
    )
}

fun CreationEntity.toUi(): CreationUi {
    return CreationUi(
        id = id,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        artisanId = artisanId,
        artisanName = artisanName,
        price = price,
        currency = currency,
        available = availabilityStatus == "available",
        likes = likesCount,
        views = viewsCount,
        imageUrl = coverImageUrl.orEmpty(),
        dimensions = dimensions.orEmpty(),
        description = description.orEmpty(),
        photoCount = 3,
        syncState = syncState
    )
}

fun CreationEntity.toSummaryDto(): CreationSummaryDto {
    return CreationSummaryDto(
        id = id,
        artisanId = artisanId,
        artisanName = artisanName,
        categoryId = categoryId,
        categoryName = categoryName,
        title = title,
        description = description,
        dimensions = dimensions,
        price = price,
        currency = currency,
        availabilityStatus = availabilityStatus,
        coverImageUrl = coverImageUrl,
        likesCount = likesCount,
        viewsCount = viewsCount,
        createdAt = createdAt,
        artisanProfilePictureUrl = artisanProfilePictureUrl
    )
}

fun CreationEntity.toDetailDto(mediaList: List<CreationMediaEntity>): CreationDetailDto {
    return CreationDetailDto(
        id = id,
        artisanId = artisanId,
        artisanName = artisanName,
        artisanProfilePictureUrl = artisanProfilePictureUrl,
        categoryId = categoryId,
        categoryName = categoryName,
        title = title,
        description = description,
        dimensions = dimensions,
        price = price,
        currency = currency,
        availabilityStatus = availabilityStatus,
        status = status,
        coverImageUrl = coverImageUrl,
        likesCount = likesCount,
        viewsCount = viewsCount,
        isLikedByMe = isLikedByMe,
        isOwnCreation = isOwn,
        createdAt = createdAt,
        updatedAt = updatedAt,
        media = mediaList.map { it.toDto() }
    )
}

fun CreationMediaEntity.toDto(): MediaItemDto {
    return MediaItemDto(
        id = id,
        url = url,
        publicId = publicId,
        position = position,
        width = width,
        height = height
    )
}

fun MediaItemDto.toEntity(creationId: String): CreationMediaEntity {
    return CreationMediaEntity(
        id = id,
        creationId = creationId,
        url = url,
        publicId = publicId,
        position = position,
        width = width,
        height = height
    )
}

fun ArtisanEntity.toDto(): ArtisanReadProfileDto {
    return ArtisanReadProfileDto(
        userId = userId,
        fullName = fullName,
        profilePictureUrl = profilePictureUrl,
        bio = bio,
        location = location,
        primaryCategoryId = primaryCategoryId,
        primaryCategoryName = primaryCategoryName,
        whatsappNumber = whatsappNumber,
        galleryOpenedAt = galleryOpenedAt
    )
}

fun ArtisanReadProfileDto.toEntity(fetchedAt: Long = System.currentTimeMillis(), isGuest: Boolean = false): ArtisanEntity {
    return ArtisanEntity(
        userId = userId,
        fullName = fullName,
        profilePictureUrl = profilePictureUrl,
        bio = bio,
        location = location,
        primaryCategoryId = primaryCategoryId,
        primaryCategoryName = primaryCategoryName,
        whatsappNumber = if (isGuest) null else whatsappNumber,
        galleryOpenedAt = galleryOpenedAt,
        fetchedAt = fetchedAt
    )
}
