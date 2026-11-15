package com.example.naepapp.core.common

object CloudinaryUrl {
    fun optimized(url: String?, width: Int): String? {
        if (url.isNullOrBlank()) return null
        return if (url.contains("/upload/") && !url.contains("w_$width")) {
            url.replace("/upload/", "/upload/w_$width,c_scale,q_auto,f_auto/")
        } else {
            url
        }
    }
}
