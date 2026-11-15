package com.example.naepapp.ui.screens.artisan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.data.paging.toUi
import com.example.naepapp.data.repository.ArtisanRepository
import com.example.naepapp.ui.model.ArtisanUi
import com.example.naepapp.ui.model.CreationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArtisanProfileUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val artisan: ArtisanUi? = null,
    val creations: List<CreationUi> = emptyList(),
    val signedIn: Boolean = false
)

@HiltViewModel
class ArtisanProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val artisanRepository: ArtisanRepository,
    sessionManager: com.example.naepapp.data.session.SessionManager
) : ViewModel() {

    private val artisanId: String = savedStateHandle["id"] ?: ""

    private val _uiState = MutableStateFlow(ArtisanProfileUiState())
    val uiState: StateFlow<ArtisanProfileUiState> = _uiState.asStateFlow()

    init {
        val isSigned = sessionManager.user.value != null
        _uiState.update { it.copy(signedIn = isSigned) }
        loadProfile()
    }

    fun loadProfile() {
        if (artisanId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = artisanRepository.profile(artisanId)
            result.fold(
                onSuccess = { dto ->
                    val artisanUi = ArtisanUi(
                        id = dto.userId,
                        name = dto.fullName,
                        location = dto.location.orEmpty(),
                        bio = dto.bio.orEmpty(),
                        categoryId = dto.primaryCategoryId ?: 0,
                        categoryName = dto.primaryCategoryName.orEmpty(),
                        whatsapp = dto.whatsappNumber.orEmpty(),
                        imageUrl = dto.profilePictureUrl.orEmpty()
                    )
                    val recent = dto.recentCreations.map { it.toUi() }.take(10)
                    _uiState.update {
                        it.copy(
                            loading = false,
                            artisan = artisanUi,
                            creations = recent
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to load profile") }
                }
            )
        }
    }
}
