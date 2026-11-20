package com.example.naepapp.core.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.naepapp.core.network.dto.InquiryDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class WhatsAppSharer @Inject constructor(
    @ApplicationContext private val context: Context?
) {
    open suspend fun prepareImageShare(inquiry: InquiryDto, creationId: String): Result<Intent> {
        val ctx = context ?: return Result.failure(Exception("Context not available"))
        return withContext(Dispatchers.IO) {
            try {
                val pm = ctx.packageManager
                val whatsappPackage = listOf("com.whatsapp", "com.whatsapp.w4b").firstOrNull { pkg ->
                    try {
                        pm.getPackageInfo(pkg, 0)
                        true
                    } catch (_: Exception) {
                        false
                    }
                } ?: return@withContext Result.failure(Exception("WhatsApp is not installed"))

                val imageUrl = inquiry.imageUrl
                if (imageUrl.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("Image URL is missing"))
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder().url(imageUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Failed to download image: ${response.code}"))
                }

                val contentType = response.header("Content-Type") ?: ""
                if (!contentType.startsWith("image/")) {
                    return@withContext Result.failure(Exception("Invalid content type: $contentType"))
                }

                val body = response.body ?: return@withContext Result.failure(Exception("Empty response body"))
                val bytes = body.bytes()

                if (bytes.size > 5 * 1024 * 1024) {
                    return@withContext Result.failure(Exception("Image size exceeds 5MB limit"))
                }

                val sharedDir = File(ctx.cacheDir, "shared_images")
                if (!sharedDir.exists()) {
                    sharedDir.mkdirs()
                }

                // Cleanup older files, keeping newest 5
                val existingFiles = sharedDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
                if (existingFiles.size >= 5) {
                    existingFiles.drop(4).forEach { it.delete() }
                }

                val imageFile = File(sharedDir, "inquiry_$creationId.jpg")
                imageFile.writeBytes(bytes)

                val uri = FileProvider.getUriForFile(
                    ctx,
                    "${ctx.packageName}.fileprovider",
                    imageFile
                )

                val fixedText = (inquiry.captionMessage ?: inquiry.message).replace("localhost:3000", "127.0.0.1:3000")

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, fixedText)
                    setPackage(whatsappPackage)
                    putExtra("jid", "${inquiry.whatsappNumber}@s.whatsapp.net")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newRawUri("", uri)
                }

                Result.success(intent)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    open suspend fun prepareImageShare(inquiry: InquiryDto): Result<Intent> {
        return prepareImageShare(inquiry, inquiry.id ?: "item")
    }
}
