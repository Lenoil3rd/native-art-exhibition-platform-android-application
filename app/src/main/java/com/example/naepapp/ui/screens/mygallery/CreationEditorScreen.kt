package com.example.naepapp.ui.screens.mygallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CategoryUi

@Composable
fun CreationEditorScreen(
    onBack: () -> Unit,
    viewModel: CreationEditorViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val pickedUris by viewModel.pickedUris.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.errorMessage?.let { err ->
        LaunchedEffect(err) {
            snackbarHostState.showSnackbar(err)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addPickedUris(uris)
        }
    }

    CreationEditorContent(
        isEditing = viewModel.isEditing,
        title = viewModel.title,
        description = viewModel.description,
        categoryId = viewModel.categoryId,
        dimensions = viewModel.dimensions,
        price = viewModel.price,
        inventory = viewModel.inventory,
        available = viewModel.available,
        categories = viewModel.categories,
        pickedUris = pickedUris,
        loading = viewModel.loading,
        saving = viewModel.saving,
        snackbarHostState = snackbarHostState,
        onTitleChanged = viewModel::updateTitle,
        onDescriptionChanged = viewModel::updateDescription,
        onCategoryChanged = viewModel::updateCategoryId,
        onDimensionsChanged = viewModel::updateDimensions,
        onPriceChanged = viewModel::updatePrice,
        onInventoryChanged = viewModel::updateInventory,
        onAvailableChanged = viewModel::updateAvailable,
        onAddPhotos = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemovePhoto = viewModel::removePickedUri,
        onSave = { viewModel.save(context, onSuccess = onBack) },
        onBack = onBack
    )
}

@Composable
fun CreationEditorContent(
    isEditing: Boolean,
    title: String,
    description: String,
    categoryId: Int?,
    dimensions: String,
    price: String,
    inventory: String,
    available: Boolean,
    categories: List<CategoryUi>,
    pickedUris: List<Uri>,
    loading: Boolean,
    saving: Boolean,
    snackbarHostState: SnackbarHostState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onDimensionsChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onInventoryChanged: (String) -> Unit,
    onAvailableChanged: (Boolean) -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Uri) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val valid = title.isNotBlank() && categoryId != null && (pickedUris.isNotEmpty() || isEditing)

    Scaffold(
        topBar = { NaepTopBar(stringResource(if (!isEditing) R.string.add_creation else R.string.edit_creation), onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (pickedUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pickedUris, key = { it.toString() }) { uri ->
                        Box {
                            ArtImage(imageUrl = uri.toString(), width = 200, modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)))
                        }
                    }
                }
            }
            NaepButton(stringResource(R.string.add_photos), onAddPhotos, icon = painterResource(R.drawable.ic_add_photo))
            NaepTextField(title, onTitleChanged, stringResource(R.string.title))
            NaepTextField(description, onDescriptionChanged, stringResource(R.string.description), singleLine = false, minLines = 4)
            CategoryDropdown(categories, categoryId, stringResource(R.string.category), onCategoryChanged)
            NaepTextField(dimensions, onDimensionsChanged, stringResource(R.string.dimensions) + " (e.g. 60 cm x 90 cm)")
            NaepTextField(price, onPriceChanged, stringResource(R.string.price_mwk), keyboardType = KeyboardType.Number, prefix = "MWK ")
            NaepTextField(inventory, onInventoryChanged, stringResource(R.string.inventory_count), keyboardType = KeyboardType.Number)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.availability_status), Modifier.weight(1f))
                AvailabilityToggle(available, onToggle = { onAvailableChanged(!available) }, modifier = Modifier.testTag("availability_switch"))
            }
            NaepButton(
                stringResource(R.string.save),
                onSave,
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, saving, valid
            )
        }
    }
}
