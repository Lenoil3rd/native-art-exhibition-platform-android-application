package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naepapp.R
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CreationUi

@Composable
fun GalleryScreen(
    onBack: () -> Unit,
    onOpenCreation: (String) -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    when {
        state.loading -> {
            Scaffold(topBar = { NaepTopBar(stringResource(R.string.gallery), onBack) }) { pad ->
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        state.error != null -> {
            Scaffold(topBar = { NaepTopBar(stringResource(R.string.gallery), onBack) }) { pad ->
                ErrorView(message = state.error!!, onRetry = viewModel::loadGallery, modifier = Modifier.padding(pad))
            }
        }
        else -> {
            GalleryContent(
                creations = state.creations,
                isOnline = isOnline,
                onBack = onBack,
                onOpenCreation = onOpenCreation
            )
        }
    }
}

@Composable
fun GalleryContent(
    creations: List<CreationUi>,
    isOnline: Boolean = true,
    onBack: () -> Unit,
    onOpenCreation: (String) -> Unit
) {
    val grouped = remember(creations) { creations.groupBy { it.categoryName } }
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.gallery), onBack) }) { pad ->
        Column(Modifier.padding(pad)) {
            if (!isOnline) {
                OfflineBanner()
            }
            if (creations.isEmpty()) EmptyView()
            else LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                grouped.forEach { (category, list) ->
                    item { SectionHeader(category) }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(list, key = { it.id }) { c -> CreationCard(c, { onOpenCreation(c.id) }, Modifier.width(180.dp)) }
                        }
                    }
                }
            }
        }
    }
}
