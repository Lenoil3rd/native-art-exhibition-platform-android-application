package com.example.naepapp.ui.screens.creation

import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.naepapp.core.common.ConnectivityMonitor
import com.example.naepapp.core.common.WhatsAppSharer
import com.example.naepapp.data.paging.toUi
import com.example.naepapp.data.repository.CreationRepository
import com.example.naepapp.data.session.PendingAction
import com.example.naepapp.data.session.SessionManager
import com.example.naepapp.ui.model.CreationUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreationDetailUiState(
    val loading: Boolean = true,
    val error: String? = null,
    val creationUi: CreationUi? = null,
    val mediaUrls: List<String> = emptyList(),
    val isLikedByMe: Boolean = false,
    val isOwnCreation: Boolean = false,
    val inquiring: Boolean = false,
    val togglingAvailability: Boolean = false,
    val showSignInPrompt: PendingAction? = null,
    val whatsappUrl: String? = null
)

@HiltViewModel
class CreationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val creationRepository: CreationRepository,
    private val sessionManager: SessionManager,
    private val whatsappSharer: WhatsAppSharer,
    private val connectivityMonitor: ConnectivityMonitor
) : ViewModel() {

    private val creationId: String = savedStateHandle["id"] ?: ""

    val isOnline: StateFlow<Boolean> = connectivityMonitor.isOnline

    private val _uiState = MutableStateFlow(CreationDetailUiState())
    val uiState: StateFlow<CreationDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail(recordViewIfNeeded = true)
    }

    fun loadDetail(recordViewIfNeeded: Boolean) {
        if (creationId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            val result = creationRepository.getDetail(creationId)
            result.fold(
                onSuccess = { detail ->
                    val ui = detail.toUi()
                    val urls = detail.media.sortedBy { it.position }.map { it.url }.ifEmpty { listOfNotNull(detail.coverImageUrl) }
                    val isSigned = sessionManager.user.value != null
                    if (recordViewIfNeeded && isSigned && !detail.isOwnCreation && connectivityMonitor.isOnline.value) {
                        creationRepository.recordView(creationId)
                    }
                    _uiState.update {
                        it.copy(
                            loading = false,
                            creationUi = ui,
                            mediaUrls = urls,
                            isLikedByMe = detail.isLikedByMe,
                            isOwnCreation = detail.isOwnCreation
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(loading = false, error = err.message ?: "Failed to load details") }
                }
            )
        }
    }

    fun onResumed() {
        val user = sessionManager.user.value
        val pending = sessionManager.pendingAction.value
        if (user != null && pending != null && pending.second == creationId) {
            sessionManager.setPendingAction(null, null)
            loadDetail(recordViewIfNeeded = false)
            when (pending.first) {
                PendingAction.Like -> onLike()
                PendingAction.Inquire -> {}
            }
        }
    }

    fun onLike() {
        val user = sessionManager.user.value
        val current = _uiState.value
        if (user == null) {
            sessionManager.setPendingAction(PendingAction.Like, creationId)
            _uiState.update { it.copy(showSignInPrompt = PendingAction.Like) }
            return
        }
        if (current.isOwnCreation) return

        if (!connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = "This needs an internet connection.") }
            return
        }

        viewModelScope.launch {
            val wasLiked = current.isLikedByMe
            val currentLikes = current.creationUi?.likes ?: 0
            val newLiked = !wasLiked
            val newLikes = if (newLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
            _uiState.update { state ->
                state.copy(
                    isLikedByMe = newLiked,
                    creationUi = state.creationUi?.copy(likes = newLikes)
                )
            }

            val result = if (newLiked) {
                creationRepository.like(creationId)
            } else {
                creationRepository.unlike(creationId)
            }

            result.fold(
                onSuccess = { res ->
                    _uiState.update { state ->
                        state.copy(
                            isLikedByMe = res.liked,
                            creationUi = state.creationUi?.copy(likes = res.likesCount)
                        )
                    }
                },
                onFailure = {
                    _uiState.update { state ->
                        state.copy(
                            isLikedByMe = wasLiked,
                            creationUi = state.creationUi?.copy(likes = currentLikes)
                        )
                    }
                }
            )
        }
    }

    fun onInquire(
        onLaunchWhatsApp: (String) -> Unit,
        onLaunchWhatsAppWithImage: (Intent, String?) -> Unit,
        onShowSnackbar: (String) -> Unit
    ) {
        val user = sessionManager.user.value
        val current = _uiState.value
        if (user == null) {
            sessionManager.setPendingAction(PendingAction.Inquire, creationId)
            _uiState.update { it.copy(showSignInPrompt = PendingAction.Inquire) }
            return
        }
        if (current.isOwnCreation) return

        if (!connectivityMonitor.isOnline.value) {
            onShowSnackbar("This needs an internet connection.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(inquiring = true) }
            val defaultMessage = "Hello, I am interested in your creation \"${current.creationUi?.title}\"."
            val result = creationRepository.inquire(creationId, defaultMessage)
            result.fold(
                onSuccess = { dto ->
                    val imageUrl = dto.imageUrl
                    val fallbackUrl = dto.whatsappUrl?.replace("localhost:3000", "127.0.0.1:3000")
                    if (imageUrl.isNullOrBlank()) {
                        _uiState.update { it.copy(inquiring = false) }
                        if (!fallbackUrl.isNullOrBlank()) {
                            onLaunchWhatsApp(fallbackUrl)
                        }
                    } else {
                        val shareResult = whatsappSharer.prepareImageShare(dto, creationId)
                        _uiState.update { it.copy(inquiring = false) }
                        shareResult.fold(
                            onSuccess = { intent ->
                                onLaunchWhatsAppWithImage(intent, fallbackUrl)
                            },
                            onFailure = {
                                onShowSnackbar("Could not attach the photo. Opening WhatsApp with a link instead.")
                                if (!fallbackUrl.isNullOrBlank()) {
                                    onLaunchWhatsApp(fallbackUrl)
                                }
                            }
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(inquiring = false, error = err.message ?: "Failed to send inquiry") }
                }
            )
        }
    }

    fun onToggleAvailability() {
        val current = _uiState.value
        val ui = current.creationUi ?: return
        if (!current.isOwnCreation) return

        if (!connectivityMonitor.isOnline.value) {
            _uiState.update { it.copy(error = "This needs an internet connection.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(togglingAvailability = true) }
            val newStatus = if (ui.available) "unavailable" else "available"
            val result = creationRepository.updateAvailability(creationId, newStatus)
            _uiState.update { it.copy(togglingAvailability = false) }
            result.fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(creationUi = detail.toUi()) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.message ?: "Failed to update availability") }
                }
            )
        }
    }

    fun dismissSignInPrompt() {
        _uiState.update { it.copy(showSignInPrompt = null) }
    }
}
