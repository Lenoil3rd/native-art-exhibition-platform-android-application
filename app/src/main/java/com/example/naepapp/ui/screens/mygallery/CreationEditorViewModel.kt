package com.example.naepapp.ui.screens.mygallery

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.core.common.CloudinaryUploader
import com.example.naepapp.core.common.InputValidator
import com.example.naepapp.core.network.dto.CreationRequest
import com.example.naepapp.core.network.dto.MediaInput
import com.example.naepapp.data.repository.CreationRepository
import com.example.naepapp.ui.model.CategoryUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExistingPhoto(
    val url: String,
    val publicId: String,
    val width: Int?,
    val height: Int?
)

@HiltViewModel
class CreationEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creationRepository: CreationRepository,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {

    private val creationId: String? = savedStateHandle["id"]

    var title by mutableStateOf("")
        private set
    var description by mutableStateOf("")
        private set
    var categoryId by mutableStateOf<Int?>(null)
        private set
    var dimensions by mutableStateOf("")
        private set
    var price by mutableStateOf("")
        private set
    var inventory by mutableStateOf("1")
        private set
    var available by mutableStateOf(true)
        private set

    var existingMedia by mutableStateOf<List<ExistingPhoto>>(emptyList())
        private set

    private val _pickedUris = MutableStateFlow<List<Uri>>(emptyList())
    val pickedUris: StateFlow<List<Uri>> = _pickedUris.asStateFlow()

    var categories by mutableStateOf<List<CategoryUi>>(emptyList())
        private set

    var fieldErrors by mutableStateOf<Map<String, String>>(emptyMap())
        private set
    var loading by mutableStateOf(false)
        private set
    var saving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    val isEditing get() = creationId != null

    init {
        loadCategories()
        if (creationId != null) {
            loadCreation(creationId)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            creationRepository.categories().fold(
                onSuccess = { dtos ->
                    categories = dtos.map { CategoryUi(id = it.id, name = it.name, bio = it.description.orEmpty()) }
                },
                onFailure = { categories = emptyList() }
            )
        }
    }

    private fun loadCreation(id: String) {
        viewModelScope.launch {
            loading = true
            val result = creationRepository.getDetail(id)
            loading = false
            result.fold(
                onSuccess = { detail ->
                    title = detail.title
                    description = detail.description.orEmpty()
                    categoryId = detail.categoryId
                    dimensions = detail.dimensions.orEmpty()
                    price = detail.price?.toLong()?.toString().orEmpty()
                    inventory = detail.inventoryCount.toString()
                    available = detail.availabilityStatus == "available"
                    existingMedia = detail.media.map { ExistingPhoto(it.url, it.publicId, it.width, it.height) }
                },
                onFailure = { err ->
                    errorMessage = err.message ?: "Failed to load creation"
                }
            )
        }
    }

    fun updateTitle(v: String) {
        title = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidFullName(v)) "Title must be at least 2 characters" else null
        fieldErrors = if (err != null) fieldErrors + ("title" to err) else fieldErrors - "title"
    }

    fun updateDescription(v: String) { description = v; fieldErrors = fieldErrors - "description"; errorMessage = null }
    fun updateCategoryId(v: Int?) { categoryId = v; fieldErrors = fieldErrors - "categoryId"; errorMessage = null }
    fun updateDimensions(v: String) { dimensions = v; fieldErrors = fieldErrors - "dimensions"; errorMessage = null }

    fun updatePrice(v: String) {
        price = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidPrice(v)) "Enter a valid positive price" else null
        fieldErrors = if (err != null) fieldErrors + ("price" to err) else fieldErrors - "price"
    }

    fun updateInventory(v: String) {
        inventory = v
        errorMessage = null
        val err = if (v.isNotEmpty() && !InputValidator.isValidInventory(v)) "Enter a valid non-negative number" else null
        fieldErrors = if (err != null) fieldErrors + ("inventoryCount" to err) else fieldErrors - "inventoryCount"
    }

    fun updateAvailable(v: Boolean) { available = v }

    fun addPickedUris(uris: List<Uri>) {
        val current = _pickedUris.value
        val totalCount = existingMedia.size + current.size
        val maxNew = (8 - totalCount).coerceAtLeast(0)
        if (maxNew > 0) {
            _pickedUris.update { (current + uris.take(maxNew)) }
        }
    }

    fun removeExistingPhoto(photo: ExistingPhoto) {
        existingMedia = existingMedia.filter { it.publicId != photo.publicId || it.url != photo.url }
    }

    fun removePickedUri(uri: Uri) {
        _pickedUris.update { it.filter { u -> u != uri } }
    }

    fun save(context: Context, onSuccess: () -> Unit) {
        val errors = mutableMapOf<String, String>()
        if (title.isBlank()) {
            errors["title"] = "Title is required"
        } else if (!InputValidator.isValidFullName(title)) {
            errors["title"] = "Title must be at least 2 characters"
        }

        if (price.isNotBlank() && !InputValidator.isValidPrice(price)) {
            errors["price"] = "Enter a valid positive price"
        }

        if (inventory.isNotBlank() && !InputValidator.isValidInventory(inventory)) {
            errors["inventoryCount"] = "Enter a valid non-negative number"
        }

        val catId = categoryId
        if (catId == null) {
            errorMessage = "Please select a category"
            fieldErrors = errors
            return
        }

        val uris = _pickedUris.value
        if (existingMedia.isEmpty() && uris.isEmpty()) {
            errorMessage = "At least one photo is required"
            fieldErrors = errors
            return
        }

        if (errors.isNotEmpty()) {
            fieldErrors = errors
            errorMessage = "Please fix the highlighted errors"
            return
        }

        viewModelScope.launch {
            saving = true
            errorMessage = null
            fieldErrors = emptyMap()

            val allMedia = existingMedia.map {
                MediaInput(url = it.url, publicId = it.publicId, width = it.width, height = it.height)
            }

            val priceVal = price.toDoubleOrNull()
            val invVal = inventory.toIntOrNull() ?: 1
            val availStatus = if (available) "available" else "unavailable"

            val request = CreationRequest(
                title = title,
                description = description.ifBlank { null },
                categoryId = catId,
                dimensions = dimensions.ifBlank { null },
                price = priceVal,
                inventoryCount = invVal,
                availabilityStatus = availStatus,
                media = allMedia
            )

            val result = if (creationId != null) {
                creationRepository.update(creationId, request, uris)
            } else {
                creationRepository.create(request, uris)
            }

            saving = false
            result.fold(
                onSuccess = { onSuccess() },
                onFailure = { err ->
                    val apiError = err as? ApiError
                    if (apiError?.details != null) {
                        fieldErrors = apiError.details.associate { it.field to it.message }
                    } else {
                        errorMessage = err.message ?: "Failed to save creation"
                    }
                }
            )
        }
    }
}
