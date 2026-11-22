package com.example.naepapp

import com.example.naepapp.core.network.dto.MediaInput
import com.example.naepapp.data.sync.CreationPayload
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreationPayloadTest {

    @Test
    fun creationPayload_containsIdAndMediaHasOnlyAllowedKeys() {
        val payload = CreationPayload(
            id = "test-uuid-v4",
            title = "Test Creation",
            categoryId = 1,
            media = listOf(MediaInput(url = "https://example.com/img.jpg", publicId = "img_1", width = 800, height = 600))
        )

        val json = Json { explicitNulls = false }
        val jsonString = json.encodeToString(CreationPayload.serializer(), payload)

        assertTrue(jsonString.contains("test-uuid-v4"))
        assertTrue(jsonString.contains("https://example.com/img.jpg"))
        assertTrue(jsonString.contains("img_1"))
        assertTrue(jsonString.contains("800"))
        assertTrue(jsonString.contains("600"))

        val mediaInput = payload.media.first()
        assertEquals("https://example.com/img.jpg", mediaInput.url)
        assertEquals("img_1", mediaInput.publicId)
        assertEquals(800, mediaInput.width)
        assertEquals(600, mediaInput.height)
    }
}
