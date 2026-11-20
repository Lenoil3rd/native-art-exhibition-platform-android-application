package com.example.naepapp.ui.screens.artisan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.core.common.ConnectivityMonitor
import com.example.naepapp.core.common.InputValidator
import com.example.naepapp.core.network.dto.OpenGalleryRequest
import com.example.naepapp.data.repository.ArtisanRepository
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.ui.model.CategoryUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OpenGalleryViewModel @Inject constructor(
    private val artisanRepository: ArtisanRepository,
    sessionManager: SessionManager,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    var bio by mutableStateOf("")
        private set
    var location by mutableStateOf("")
        private set
    var categoryId by mutableStateOf<Int?>(null)
        private set
    var whatsapp by mutableStateOf("")
        private set
    var categories by mutableStateOf<List<CategoryUi>>(emptyList())
        private set
    var fieldErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var loading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        whatsapp = sessionManager.user.value?.phoneNumber.orEmpty()
        loadCategories()
    }

    fun updateBio(v: String) { bio = v; fieldErrors = fieldErrors - "bio"; errorMessage = null }
    fun updateLocation(v: String) { location = v; fieldErrors = fieldErrors - "location"; errorMessage = null }
    fun updateCategoryId(v: Int?) { categoryId = v; fieldErrors = fieldErrors - "primaryCategoryId"; errorMessage = null }

    fun updateWhatsapp(v: String) {
        whatsapp = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidPhone(v)) "Enter a valid phone number (at least 9 digits)" else null
        fieldErrors = if (err != null) fieldErrors + ("whatsappNumber" to err) else fieldErrors - "whatsappNumber"
    }

    private fun loadCategories() {
        viewModelScope.launch {
            artisanRepository.categories().fold(
                onSuccess = { dtos ->
                    categories = dtos.map { CategoryUi(id = it.id, name = it.name, bio = it.description.orEmpty()) }
                },
                onFailure = { categories = emptyList() }
            )
        }
    }

    fun openGallery(onSuccess: () -> Unit) {
        if (!connectivityMonitor.isOnline.value) {
            errorMessage = "This needs an internet connection."
            return
        }

        val errors = mutableMapOf<String, String>()
        if (bio.isBlank()) {
            errors["bio"] = "Bio is required"
        }
        if (location.isBlank()) {
            errors["location"] = "Location is required"
        }
        if (whatsapp.isNotBlank() && !InputValidator.isValidPhone(whatsapp)) {
            errors["whatsappNumber"] = "Enter a valid phone number (at least 9 digits)"
        }

        val catId = categoryId
        if (catId == null) {
            errorMessage = "Please select a category"
            fieldErrors = errors
            return
        }

        if (errors.isNotEmpty()) {
            fieldErrors = errors
            errorMessage = "Please fix the highlighted errors"
            return
        }

        viewModelScope.launch {
            loading = true
            errorMessage = null
            fieldErrors = emptyMap()
            val req = OpenGalleryRequest(
                bio = bio.ifBlank { null },
                location = location.ifBlank { null },
                primaryCategoryId = catId,
                whatsappNumber = whatsapp.ifBlank { null }
            )
            val result = artisanRepository.openGallery(req)
            loading = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    val apiError = err as? ApiError
                    if (apiError?.details != null) {
                        fieldErrors = apiError.details.associate { it.field to it.message }
                    } else {
                        errorMessage = err.message ?: "Failed to open gallery"
                    }
                }
            )
        }
    }
}
