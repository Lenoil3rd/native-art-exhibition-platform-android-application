package com.example.naepapp.ui.screens.mygallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ConnectivityMonitor
import com.example.naepapp.data.paging.toUi
import com.example.naepapp.data.repository.CreationRepository
import com.example.naepapp.ui.model.CreationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyGalleryUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val creations: List<CreationUi> = emptyList(),
    val togglingMap: Map<String, Boolean> = emptyMap(),
    val deletingId: String? = null
)

@HiltViewModel
class MyGalleryViewModel @Inject constructor(
    private val creationRepository: CreationRepository,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _uiState = MutableStateFlow(MyGalleryUiState())
    val uiState: StateFlow<MyGalleryUiState> = _uiState.asStateFlow()

    init {
        loadCreations()
    }

    fun loadCreations() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = creationRepository.getMyCreations()
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

    fun toggleAvailability(id: String, currentAvailable: Boolean) {
        if (!connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = "This needs an internet connection.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(togglingMap = state.togglingMap + (id to true))
            }
            val newStatus = if (currentAvailable) "unavailable" else "available"
            val result = creationRepository.updateAvailability(id, newStatus)
            _uiState.update { state ->
                state.copy(togglingMap = state.togglingMap - id)
            }
            result.fold(
                onSuccess = { detail ->
                    val updated = detail.toUi()
                    _uiState.update { state ->
                        state.copy(creations = state.creations.map { if (it.id == id) updated else it })
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.message ?: "Failed to update availability") }
                }
            )
        }
    }

    fun setDeletingId(id: String?) {
        _uiState.update { it.copy(deletingId = id) }
    }

    fun deleteCreation(id: String) {
        if (!connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = "This needs an internet connection.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(deletingId = null) }
            val result = creationRepository.delete(id)
            result.fold(
                onSuccess = {
                    _uiState.update { state ->
                        state.copy(creations = state.creations.filter { it.id != id })
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.message ?: "Failed to delete creation") }
                }
            )
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
