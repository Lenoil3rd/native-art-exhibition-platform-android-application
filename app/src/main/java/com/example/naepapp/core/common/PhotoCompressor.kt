package com.example.naepapp.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PhotoCompressor {
    suspend fun compressAndSaveToPrivateDir(
        context: Context,
        uri: Uri,
        creationId: String,
        index: Int
    ): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val width = bitmap.width
            val height = bitmap.height
            val maxSide = 1600f
            val scale = if (width > maxSide || height > maxSide) {
                if (width > height) maxSide / width else maxSide / height
            } else {
                1f
            }

            val scaledBitmap = if (scale < 1f) {
                Bitmap.createScaledBitmap(bitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                bitmap
            }

            val dir = File(context.filesDir, "pending_photos/$creationId")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "photo_$index.jpg")
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            if (scaledBitmap != bitmap) {
                scaledBitmap.recycle()
            }
            bitmap.recycle()

            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }
}
