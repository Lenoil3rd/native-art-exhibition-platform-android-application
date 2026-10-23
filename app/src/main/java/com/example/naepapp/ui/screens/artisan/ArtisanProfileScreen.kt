package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naepapp.R
import com.example.naepapp.core.common.WhatsAppLauncher
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.ArtisanUi
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.theme.BlackForest
import kotlinx.coroutines.launch

@Composable
fun ArtisanProfileScreen(
    onBack: () -> Unit,
    onOpenCreation: (String) -> Unit,
    onOpenGallery: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: ArtisanProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val notInstalledMsg = stringResource(R.string.whatsapp_not_installed)

    state.error?.let { err ->
        LaunchedEffect(err) {
            snackbar.showSnackbar(err)
        }
    }

    when {
        state.loading -> {
            Scaffold(topBar = { NaepTopBar(stringResource(R.string.artisan_profile), onBack) }) { pad ->
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        state.artisan == null -> {
            Scaffold(topBar = { NaepTopBar(stringResource(R.string.artisan_profile), onBack) }) { pad ->
                ErrorView(message = state.error ?: "Artisan not found", onRetry = viewModel::loadProfile, modifier = Modifier.padding(pad))
            }
        }
        else -> {
            ArtisanProfileContent(
                artisan = state.artisan!!,
                creations = state.creations,
                signedIn = state.signedIn,
                snackbarHostState = snackbar,
                onBack = onBack,
                onOpenCreation = onOpenCreation,
                onOpenGallery = onOpenGallery,
                onSignIn = onSignIn,
                onContactWhatsApp = { number ->
                    val digits = number.filter { it.isDigit() }
                    val waUrl = "https://wa.me/$digits"
                    WhatsAppLauncher.launch(context, waUrl) {
                        scope.launch {
                            snackbar.showSnackbar(notInstalledMsg)
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ArtisanProfileContent(
    artisan: ArtisanUi,
    creations: List<CreationUi>,
    signedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenCreation: (String) -> Unit,
    onOpenGallery: (String) -> Unit,
    onSignIn: () -> Unit,
    onContactWhatsApp: (String) -> Unit
) {
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.artisan_profile), onBack) }, snackbarHost = { SnackbarHost(snackbarHostState) }) { pad ->
        LazyVerticalGrid(
            GridCells.Adaptive(160.dp), Modifier.padding(pad), contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AvatarImage(artisan.name, size = 96.dp)
                    Text(artisan.name, style = MaterialTheme.typography.headlineSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_location_on), null, Modifier.size(18.dp)); Text(artisan.location)
                    }
                    Text(artisan.categoryName, color = BlackForest,
                        modifier = Modifier.background(BlackForest.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(8.dp, 4.dp))
                    Text(artisan.bio)
                    if (signedIn) {
                        if (artisan.whatsapp.isNotBlank()) {
                            NaepButton(stringResource(R.string.contact_on_whatsapp), { onContactWhatsApp(artisan.whatsapp) }, Modifier.fillMaxWidth())
                        } else {
                            NaepButton(stringResource(R.string.contact_on_whatsapp), {}, Modifier.fillMaxWidth(), enabled = false)
                        }
                    } else {
                        NaepButton(stringResource(R.string.sign_in_to_contact), onSignIn, Modifier.fillMaxWidth())
                    }
                    SectionHeader(stringResource(R.string.recent_creations), Modifier.fillMaxWidth())
                }
            }
            items(creations, key = { it.id }) { c -> CreationCard(c, { onOpenCreation(c.id) }) }
            item(span = { GridItemSpan(maxLineSpan) }) {
                NaepButton(stringResource(R.string.view_full_gallery), { onOpenGallery(artisan.id) }, Modifier.fillMaxWidth())
            }
        }
    }
}
