package com.example.naepapp.ui.screens.creation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naepapp.R
import com.example.naepapp.core.common.WhatsAppLauncher
import com.example.naepapp.data.session.PendingAction
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.Onyx
import kotlinx.coroutines.launch

@Composable
fun CreationDetailScreen(
    onBack: () -> Unit,
    onOpenArtisan: (String) -> Unit,
    onEdit: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: CreationDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notInstalledMsg = stringResource(R.string.whatsapp_not_installed)
    val couldNotAttachMsg = stringResource(R.string.could_not_attach_photo)
    var fullScreenUrl by remember { mutableStateOf<String?>(null) }

    LifecycleResumeEffect(Unit) {
        viewModel.onResumed()
        onPauseOrDispose {}
    }

    state.error?.let { err ->
        LaunchedEffect(err) {
            snackbarHostState.showSnackbar(err)
        }
    }

    state.showSignInPrompt?.let { pending ->
        val promptText = when (pending) {
            PendingAction.Like -> stringResource(R.string.guest_prompt_like)
            PendingAction.Inquire -> stringResource(R.string.guest_prompt_inquire)
        }
        SignInPromptSheet(
            message = promptText,
            onSignIn = {
                viewModel.dismissSignInPrompt()
                onSignIn()
            },
            onDismiss = { viewModel.dismissSignInPrompt() }
        )
    }

    fullScreenUrl?.let { url ->
        FullScreenImageView(
            imageUrl = url,
            onDismiss = { fullScreenUrl = null }
        )
    }

    state.creationUi?.let { creation ->
        CreationDetailContent(
            creation = creation,
            mediaUrls = state.mediaUrls,
            isLikedByMe = state.isLikedByMe,
            isOwnCreation = state.isOwnCreation,
            loading = state.loading,
            inquiring = state.inquiring,
            togglingAvailability = state.togglingAvailability,
            isOnline = isOnline,
            snackbarHostState = snackbarHostState,
            onBack = back@{ onBack() },
            onOpenArtisan = onOpenArtisan,
            onEdit = onEdit,
            onLike = viewModel::onLike,
            onInquire = {
                viewModel.onInquire(
                    onLaunchWhatsApp = { url ->
                        WhatsAppLauncher.launch(context, url) {
                            scope.launch {
                                snackbarHostState.showSnackbar(notInstalledMsg)
                            }
                        }
                    },
                    onLaunchWhatsAppWithImage = { intent, fallbackUrl ->
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            scope.launch {
                                snackbarHostState.showSnackbar(couldNotAttachMsg)
                            }
                            if (!fallbackUrl.isNullOrBlank()) {
                                WhatsAppLauncher.launch(context, fallbackUrl) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(notInstalledMsg)
                                    }
                                }
                            }
                        }
                    },
                    onShowSnackbar = { msg ->
                        scope.launch {
                            snackbarHostState.showSnackbar(msg)
                        }
                    }
                )
            },
            onToggleAvailability = viewModel::onToggleAvailability,
            onImageClick = { url ->
                if (!url.isNullOrBlank()) fullScreenUrl = url
            }
        )
    } ?: run {
        if (state.loading) {
            LoadingView()
        } else {
            ErrorView(message = state.error ?: "Creation not found", onRetry = { viewModel.loadDetail(recordViewIfNeeded = true) })
        }
    }
}

@Composable
fun CreationDetailContent(
    creation: CreationUi,
    mediaUrls: List<String>,
    isLikedByMe: Boolean,
    isOwnCreation: Boolean,
    loading: Boolean,
    inquiring: Boolean,
    togglingAvailability: Boolean,
    isOnline: Boolean = true,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenArtisan: (String) -> Unit,
    onEdit: (String) -> Unit,
    onLike: () -> Unit,
    onInquire: () -> Unit,
    onToggleAvailability: () -> Unit,
    onImageClick: (String?) -> Unit
) {
    Scaffold(topBar = { NaepTopBar(creation.title, onBack) }, snackbarHost = { SnackbarHost(snackbarHostState) }) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState())) {
            if (!isOnline) {
                OfflineBanner()
            }
            ImageCarousel(
                pageCount = mediaUrls.size.coerceAtLeast(1),
                imageUrls = mediaUrls,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                onImageClick = onImageClick
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(creation.title, style = MaterialTheme.typography.headlineSmall)
                Row(Modifier.clickable { onOpenArtisan(creation.artisanId) }, verticalAlignment = Alignment.CenterVertically) {
                    AvatarImage(creation.artisanName); Spacer(Modifier.width(8.dp)); Text(creation.artisanName)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PriceText(creation.price, creation.currency); AvailabilityBadge(creation.available)
                }
                DetailRow(stringResource(R.string.category), creation.categoryName)
                DetailRow(stringResource(R.string.dimensions), creation.dimensions)
                Text(creation.description)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LikeButton(isLikedByMe, creation.likes, enabled = !isOwnCreation, onClick = onLike)
                    Spacer(Modifier.width(16.dp))
                    Icon(painterResource(R.drawable.ic_visibility), stringResource(R.string.views), Modifier.size(20.dp))
                    Text(" ${creation.views}")
                }
                if (isOwnCreation) {
                    // AvailabilityToggle's Switch carries Modifier.testTag("availability_switch") per TESTABILITY rules
                    AvailabilityToggle(
                        available = creation.available,
                        onToggle = onToggleAvailability,
                        enabled = !togglingAvailability,
                        modifier = Modifier.testTag("availability_switch")
                    )
                    NaepButton(stringResource(R.string.edit_creation), { onEdit(creation.id) }, Modifier.fillMaxWidth())
                } else {
                    NaepButton(stringResource(R.string.inquire_creation), onInquire, Modifier.fillMaxWidth(), NaepButtonKind.Confirm, loading = inquiring)
                }
            }
        }
    }
}

@Composable
fun FullScreenImageView(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Onyx)
        ) {
            ArtImage(
                imageUrl = imageUrl,
                width = 1600,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .statusBarsPadding()
                    .size(48.dp)
                    .background(Onyx.copy(alpha = 0.85f), CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_back),
                    contentDescription = stringResource(R.string.cancel),
                    tint = BrightSnow
                )
            }
        }
    }
}

@Composable private fun DetailRow(label: String, value: String) =
    Row { Text("$label: ", fontWeight = FontWeight.SemiBold); Text(value) }
