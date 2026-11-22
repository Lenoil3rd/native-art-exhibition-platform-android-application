package com.example.naepapp

import com.example.naepapp.core.db.entity.CreationEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class MyGalleryMergeTest {

    @Test
    fun myGallery_mergeRuleKeepsPendingAndRemovesMissingSynced() {
        val serverDtos = listOf("c2")
        val serverIds = serverDtos.toSet()

        val localEntities = listOf(
            CreationEntity(id = "c1", artisanId = "u1", categoryId = 1, categoryName = "", title = "Synced 1", description = "", dimensions = "", price = null, currency = "", availabilityStatus = "", status = "published", viewsCount = 0, likesCount = 0, createdAt = "", updatedAt = "", coverImageUrl = null, artisanName = "", artisanProfilePictureUrl = null, isLikedByMe = false, isOwn = true, fetchedAt = 0L, syncState = "SYNCED"),
            CreationEntity(id = "c2", artisanId = "u1", categoryId = 1, categoryName = "", title = "Synced 2", description = "", dimensions = "", price = null, currency = "", availabilityStatus = "", status = "published", viewsCount = 0, likesCount = 0, createdAt = "", updatedAt = "", coverImageUrl = null, artisanName = "", artisanProfilePictureUrl = null, isLikedByMe = false, isOwn = true, fetchedAt = 0L, syncState = "SYNCED"),
            CreationEntity(id = "c3", artisanId = "u1", categoryId = 1, categoryName = "", title = "Pending 3", description = "", dimensions = "", price = null, currency = "", availabilityStatus = "", status = "published", viewsCount = 0, likesCount = 0, createdAt = "", updatedAt = "", coverImageUrl = null, artisanName = "", artisanProfilePictureUrl = null, isLikedByMe = false, isOwn = true, fetchedAt = 0L, syncState = "PENDING_CREATE")
        )

        val localPendingMap = localEntities.filter { it.syncState != "SYNCED" }.associateBy { it.id }
        val pendingToKeep = localPendingMap.filterKeys { it !in serverIds }.values

        val syncedToRemove = localEntities.filter { it.syncState == "SYNCED" && it.id !in serverIds }

        assertEquals(1, syncedToRemove.size)
        assertEquals("c1", syncedToRemove.first().id)

        assertEquals(1, pendingToKeep.size)
        assertEquals("c3", pendingToKeep.first().id)
    }
}
