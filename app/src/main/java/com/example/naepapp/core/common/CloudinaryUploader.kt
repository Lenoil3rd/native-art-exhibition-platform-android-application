package com.example.naepapp.core.common

import android.content.Context
import android.net.Uri
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.MediaInput
import com.example.naepapp.core.network.dto.SignatureRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class CloudinaryUploadResponse(
    val secure_url: String,
    val public_id: String,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class CloudinaryErrorResponse(
    val error: CloudinaryErrorDetail? = null
)

@Serializable
data class CloudinaryErrorDetail(
    val message: String
)

@Singleton
class CloudinaryUploader @Inject constructor(
    private val apiService: ApiService
) {
    private val client = OkHttpClient.Builder().build()
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun upload(context: Context, uri: Uri, purpose: String? = null): Result<MediaInput> = withContext(Dispatchers.IO) {
        try {
            val sigRes = apiService.getUploadSignature(SignatureRequest(purpose = purpose))
            val sig = sigRes.data

            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
            val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext Result.failure(Exception("Could not read image file"))

            val mediaType = mimeType.toMediaTypeOrNull() ?: "image/jpeg".toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val multipartBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "upload.jpg", requestBody)
                .addFormDataPart("api_key", sig.apiKey)
                .addFormDataPart("timestamp", sig.timestamp.toString())
                .addFormDataPart("signature", sig.signature)

            sig.folder?.let { multipartBodyBuilder.addFormDataPart("folder", it) }

            val uploadUrl = "https://api.cloudinary.com/v1_1/${sig.cloudName}/image/upload"
            val request = Request.Builder()
                .url(uploadUrl)
                .post(multipartBodyBuilder.build())
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val serverErrorMsg = responseBody?.let { body ->
                    try {
                        json.decodeFromString<CloudinaryErrorResponse>(body).error?.message
                    } catch (_: Exception) { null }
                }
                val errorMsg = serverErrorMsg
                    ?: if (response.code == 503) "Image uploads are not set up yet" else "Upload failed (${response.code})"
                return@withContext Result.failure(Exception(errorMsg))
            }

            if (responseBody.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty response from Cloudinary"))
            }

            val uploadResult = json.decodeFromString<CloudinaryUploadResponse>(responseBody)
            Result.success(
                MediaInput(
                    url = uploadResult.secure_url,
                    publicId = uploadResult.public_id,
                    width = uploadResult.width,
                    height = uploadResult.height
                )
            )
        } catch (e: HttpException) {
            if (e.code() == 503) {
                Result.failure(Exception("Image uploads are not set up yet"))
            } else {
                Result.failure(e)
            }
        } catch (e: Exception) {
            if (e.message?.contains("503") == true || e.message?.contains("UPLOADS_NOT_CONFIGURED") == true) {
                Result.failure(Exception("Image uploads are not set up yet"))
            } else {
                Result.failure(e)
            }
        }
    }
}
