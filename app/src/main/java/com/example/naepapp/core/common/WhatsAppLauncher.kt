package com.example.naepapp.core.common

import android.content.Context
import android.content.Intent
import android.net.Uri

object WhatsAppLauncher {
    fun launch(context: Context, whatsappUrl: String, onFallback: () -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            onFallback()
        }
    }
}
