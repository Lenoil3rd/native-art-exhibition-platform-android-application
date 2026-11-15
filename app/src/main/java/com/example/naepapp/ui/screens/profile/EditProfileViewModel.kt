package com.example.naepapp.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.CloudinaryUploader
import com.example.naepapp.core.common.InputValidator
import com.example.naepapp.core.network.dto.ArtisanWriteProfileDto
import com.example.naepapp.core.network.dto.UpdateMeRequest
import com.example.naepapp.data.repository.ArtisanRepository
import com.example.naepapp.data.repository.AuthRepository
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.ui.model.CategoryUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val artisanRepository: ArtisanRepository,
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {
    var fullName by mutableStateOf("")
        private set
    var bio by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var whatsapp by mutableStateOf("")
        private set
    var categoryId by mutableStateOf<Int?>(null)
        private set
    var categories by mutableStateOf<List<CategoryUi>>(emptyList())
        private set
    var profilePictureUri by mutableStateOf<Uri?>(null)
        private set
    var profilePictureUrl by mutableStateOf<String?>(null)
        private set

    var fieldErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var saving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val isArtisan: Boolean get() {
        val u = sessionManager.user.value
        return u?.isArtisan == true || u?.preferredMode?.equals("artisan", ignoreCase = true) == true
    }
    val userName: String get() = sessionManager.user.value?.fullName ?: "User"

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            artisanRepository.categories().fold(
                onSuccess = { dtos ->
                    categories = dtos.map { CategoryUi(id = it.id, name = it.name, bio = it.description.orEmpty()) }
                },
                onFailure = { categories = emptyList() }
            )
            val user = sessionManager.user.value
            fullName = user?.fullName.orEmpty()
            profilePictureUrl = user?.profilePictureUrl
            if (isArtisan) {
                artisanRepository.profile(user!!.id).fold(
                    onSuccess = { profile ->
                        bio = profile.bio.orEmpty()
                        location = profile.location.orEmpty()
                        whatsapp = profile.whatsappNumber.orEmpty()
                        categoryId = profile.primaryCategoryId
                    },
                    onFailure = {}
                )
            }
        }
    }

    fun updateFullName(v: String) {
        fullName = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidFullName(v)) "Full name must be at least 2 characters" else null
        fieldErrors = if (err != null) fieldErrors + ("fullName" to err) else fieldErrors - "fullName"
    }

    fun updateBio(v: String) { fullName = v; errorMessage = null }
    fun updateLocation(v: String) { location = v; errorMessage = null }

    fun updateWhatsapp(v: String) {
        whatsapp = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidPhone(v)) "Enter a valid phone number (at least 9 digits)" else null
        fieldErrors = if (err != null) fieldErrors + ("whatsapp" to err) else fieldErrors - "whatsapp"
    }

    fun updateCategoryId(v: Int?) { categoryId = v; errorMessage = null }
    fun updateProfilePictureUri(uri: Uri?) { profilePictureUri = uri; errorMessage = null }

    fun save(context: Context, onSuccess: () -> Unit) {
        val errors = mutableMapOf<String, String>()
        if (fullName.isBlank()) {
            errors["fullName"] = "Full name is required"
        } else if (!InputValidator.isValidFullName(fullName)) {
            errors["fullName"] = "Full name must be at least 2 characters"
        }
        if (isArtisan && whatsapp.isNotBlank() && !InputValidator.isValidPhone(whatsapp)) {
            errors["whatsapp"] = "Enter a valid phone number (at least 9 digits)"
        }

        if (errors.isNotEmpty()) {
            fieldErrors = errors
            errorMessage = "Please fix the highlighted errors"
            return
        }

        viewModelScope.launch {
            saving = true
            errorMessage = null

            var picUrl: String? = profilePictureUrl
            var picPublicId: String? = null

            val picUri = profilePictureUri
            if (picUri != null) {
                val uploadResult = cloudinaryUploader.upload(context, picUri, purpose = "profile")
                if (uploadResult.isSuccess) {
                    val mediaInput = uploadResult.getOrNull()
                    picUrl = mediaInput?.url
                    picPublicId = mediaInput?.publicId
                } else {
                    saving = false
                    errorMessage = uploadResult.exceptionOrNull()?.message ?: "Profile picture upload failed"
                    return@launch
                }
            }

            val updateMeResult = authRepository.updateMe(
                UpdateMeRequest(
                    fullName = fullName.ifBlank { null },
                    preferredMode = null,
                    profilePictureUrl = picUrl,
                    profilePicturePublicId = picPublicId
                )
            )

            if (updateMeResult.isFailure) {
                saving = false
                errorMessage = updateMeResult.exceptionOrNull()?.message ?: "Failed to update profile"
                return@launch
            }

            if (isArtisan) {
                val artisanReq = ArtisanWriteProfileDto(
                    bio = bio.ifBlank { null },
                    location = location.ifBlank { null },
                    primaryCategoryId = categoryId,
                    whatsappNumber = whatsapp.ifBlank { null },
                    isActive = true
                )
                val artisanResult = artisanRepository.updateProfile(artisanReq)
                if (artisanResult.isFailure) {
                    saving = false
                    errorMessage = artisanResult.exceptionOrNull()?.message ?: "Failed to update artisan profile"
                    return@launch
                }
            }

            artisanRepository.refreshMe()
            saving = false
            onSuccess()
        }
    }
}
