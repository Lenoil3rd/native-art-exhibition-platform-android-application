package com.example.naepapp.ui.screens.artisan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.data.paging.toUi
import com.example.naepapp.data.repository.ArtisanRepository
import com.example.naepapp.ui.model.CreationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GalleryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val creations: List<CreationUi> = emptyList()
)

@HiltViewModel
class GalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artisanRepository: ArtisanRepository
) : ViewModel() {

    private val artisanId: String = savedStateHandle["id"] ?: ""

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    init {
        loadGallery()
    }

    fun loadGallery() {
        if (artisanId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = artisanRepository.gallery(artisanId)
            result.fold(
                onSuccess = { dtos ->
                    val items = dtos.map { it.toUi() }
                    _uiState.update { it.copy(loading = false, creations = items) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to load gallery") }
                }
            )
        }
    }
}
