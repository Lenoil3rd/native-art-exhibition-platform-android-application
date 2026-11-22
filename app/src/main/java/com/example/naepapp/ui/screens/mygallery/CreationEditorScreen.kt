package com.example.naepapp.ui.screens.mygallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.OxBlood

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
        existingMedia = viewModel.existingMedia,
        pickedUris = pickedUris,
        fieldErrors = viewModel.fieldErrors,
        loading = viewModel.loading,
        saving = viewModel.saving,
        errorMessage = viewModel.errorMessage,
        snackbarHostState = snackbarHostState,
        onTitleChanged = viewModel::updateTitle,
        onDescriptionChanged = viewModel::updateDescription,
        onCategoryChanged = viewModel::updateCategoryId,
        onDimensionsChanged = viewModel::updateDimensions,
        onPriceChanged = viewModel::updatePrice,
        onInventoryChanged = viewModel::updateInventory,
        onAvailableChanged = viewModel::updateAvailable,
        onAddPhotos = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onRemoveExistingPhoto = viewModel::removeExistingPhoto,
        onRemovePickedUri = viewModel::removePickedUri,
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
    existingMedia: List<ExistingPhoto>,
    pickedUris: List<Uri>,
    fieldErrors: Map<String, String>,
    loading: Boolean,
    saving: Boolean,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onDimensionsChanged: (String) -> Unit,
    onPriceChanged: (String) -> Unit,
    onInventoryChanged: (String) -> Unit,
    onAvailableChanged: (Boolean) -> Unit,
    onAddPhotos: () -> Unit,
    onRemoveExistingPhoto: (ExistingPhoto) -> Unit,
    onRemovePickedUri: (Uri) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { NaepTopBar(stringResource(if (!isEditing) R.string.add_creation else R.string.edit_creation), onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(16.dp).imePadding().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (existingMedia.isNotEmpty() || pickedUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(existingMedia, key = { "existing_${it.publicId}" }) { photo ->
                        EditableImageTile(
                            imageUrl = photo.url,
                            onRemove = { onRemoveExistingPhoto(photo) }
                        )
                    }
                    items(pickedUris, key = { "picked_${it}" }) { uri ->
                        EditableImageTile(
                            imageUrl = uri.toString(),
                            onRemove = { onRemovePickedUri(uri) }
                        )
                    }
                }
            }
            NaepButton(stringResource(R.string.add_photos), onAddPhotos, icon = painterResource(R.drawable.ic_add_photo))
            NaepTextField(title, onTitleChanged, stringResource(R.string.title), error = fieldErrors["title"])
            NaepTextField(description, onDescriptionChanged, stringResource(R.string.description), singleLine = false, minLines = 4, error = fieldErrors["description"])
            CategoryDropdown(categories, categoryId, stringResource(R.string.category), onCategoryChanged, error = fieldErrors["categoryId"])
            NaepTextField(dimensions, onDimensionsChanged, stringResource(R.string.dimensions) + " (e.g. 60 cm x 90 cm)", error = fieldErrors["dimensions"])
            NaepTextField(price, onPriceChanged, stringResource(R.string.price_mwk), keyboardType = KeyboardType.Number, prefix = "MWK ", error = fieldErrors["price"])
            NaepTextField(inventory, onInventoryChanged, stringResource(R.string.inventory_count), keyboardType = KeyboardType.Number, error = fieldErrors["inventoryCount"], imeAction = ImeAction.Done, onImeAction = onSave)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.availability_status), Modifier.weight(1f))
                AvailabilityToggle(available, onToggle = { onAvailableChanged(!available) }, modifier = Modifier.testTag("availability_switch"))
            }

            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            NaepButton(
                stringResource(R.string.save),
                onSave,
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, saving, enabled = !saving
            )
        }
    }
}

@Composable
fun EditableImageTile(
    imageUrl: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        ArtImage(
            imageUrl = imageUrl,
            width = 200,
            modifier = Modifier.size(88.dp).clip(RoundedCornerShape(12.dp))
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .background(BrightSnow.copy(alpha = 0.85f), CircleShape)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.delete),
                tint = OxBlood,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
