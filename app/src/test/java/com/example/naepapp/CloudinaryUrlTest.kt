package com.example.naepapp

import com.example.naepapp.core.common.CloudinaryUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CloudinaryUrlTest {

    @Test
    fun optimized_nullOrBlankUrl_returnsNull() {
        assertNull(CloudinaryUrl.optimized(null, 600))
        assertNull(CloudinaryUrl.optimized("", 600))
    }

    @Test
    fun optimized_cloudinaryUrl_insertsOptimizationParams() {
        val input = "https://res.cloudinary.com/demo/image/upload/v1234567/sample.jpg"
        val result = CloudinaryUrl.optimized(input, 600)
        assertEquals("https://res.cloudinary.com/demo/image/upload/w_600,c_scale,q_auto,f_auto/v1234567/sample.jpg", result)
    }

    @Test
    fun optimized_nonCloudinaryUrl_returnsOriginalUrl() {
        val input = "https://example.com/image.jpg"
        val result = CloudinaryUrl.optimized(input, 600)
        assertEquals("https://example.com/image.jpg", result)
    }
}
