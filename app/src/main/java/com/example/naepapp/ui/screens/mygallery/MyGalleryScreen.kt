package com.example.naepapp.ui.screens.mygallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.naepapp.R
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.OxBlood

@Composable
fun MyGalleryScreen(
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCreation: (String) -> Unit,
    viewModel: MyGalleryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LifecycleResumeEffect(Unit) {
        viewModel.loadCreations()
        onPauseOrDispose {}
    }

    state.error?.let { err ->
        LaunchedEffect(err) {
            snackbarHostState.showSnackbar(err)
            viewModel.dismissError()
        }
    }

    when {
        state.loading -> {
            Scaffold(topBar = { NaepTopBar(stringResource(R.string.my_gallery)) }) { pad ->
                Box(Modifier.padding(pad).fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        else -> {
            MyGalleryContent(
                creations = state.creations,
                togglingMap = state.togglingMap,
                deletingId = state.deletingId,
                snackbarHostState = snackbarHostState,
                onAdd = onAdd,
                onEdit = onEdit,
                onOpenCreation = onOpenCreation,
                onToggleAvailability = viewModel::toggleAvailability,
                onSetDeletingId = viewModel::setDeletingId,
                onDeleteConfirmed = viewModel::deleteCreation
            )
        }
    }
}

@Composable
fun MyGalleryContent(
    creations: List<CreationUi>,
    togglingMap: Map<String, Boolean>,
    deletingId: String?,
    snackbarHostState: SnackbarHostState,
    onAdd: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenCreation: (String) -> Unit,
    onToggleAvailability: (String, Boolean) -> Unit,
    onSetDeletingId: (String?) -> Unit,
    onDeleteConfirmed: (String) -> Unit
) {
    Scaffold(
        topBar = { NaepTopBar(stringResource(R.string.my_gallery)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, containerColor = BlackForest, contentColor = BrightSnow,
                icon = { Icon(painterResource(R.drawable.ic_add), null) }, text = { Text(stringResource(R.string.add_creation)) })
        },
    ) { pad ->
        if (creations.isEmpty()) EmptyView(Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(creations, key = { it.id }) { c ->
                val isToggling = togglingMap[c.id] == true
                Card(Modifier.fillMaxWidth().clickable { onOpenCreation(c.id) },
                    colors = CardDefaults.cardColors(containerColor = BrightSnow), border = BorderStroke(1.dp, DustGrey)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ArtImage(imageUrl = c.imageUrl, width = 200, modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall)
                            PriceText(c.price, c.currency)
                            AvailabilityToggle(
                                available = c.available,
                                onToggle = { onToggleAvailability(c.id, c.available) },
                                enabled = !isToggling,
                                modifier = Modifier.testTag("availability_switch")
                            )
                            StatsRow(c.likes, c.views)
                        }
                        Column {
                            IconButton({ onEdit(c.id) }) { Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.edit_creation)) }
                            IconButton({ onSetDeletingId(c.id) }) { Icon(painterResource(R.drawable.ic_delete), stringResource(R.string.delete), tint = OxBlood) }
                        }
                    }
                }
            }
        }
        deletingId?.let { id ->
            ConfirmDialog(stringResource(R.string.delete), "Delete this creation?",
                onConfirm = { onDeleteConfirmed(id) }, onDismiss = { onSetDeletingId(null) })
        }
    }
}
