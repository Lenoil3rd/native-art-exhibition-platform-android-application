package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.CategoryDropdown
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.model.CategoryUi

@Composable
fun OpenGalleryScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: OpenGalleryViewModel = hiltViewModel()
) {
    OpenGalleryContent(
        bio = viewModel.bio,
        location = viewModel.location,
        categoryId = viewModel.categoryId,
        whatsapp = viewModel.whatsapp,
        categories = viewModel.categories,
        fieldErrors = viewModel.fieldErrors,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onBioChanged = viewModel::updateBio,
        onLocationChanged = viewModel::updateLocation,
        onCategoryChanged = viewModel::updateCategoryId,
        onWhatsappChanged = viewModel::updateWhatsapp,
        onSubmit = { viewModel.openGallery(onSuccess = onDone) },
        onBack = onBack
    )
}

@Composable
fun OpenGalleryContent(
    bio: String,
    location: String,
    categoryId: Int?,
    whatsapp: String,
    categories: List<CategoryUi>,
    fieldErrors: Map<String, String>,
    loading: Boolean,
    errorMessage: String?,
    onBioChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onWhatsappChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.open_a_gallery), onBack) }) { pad ->
        Column(Modifier.padding(pad).padding(24.dp).imePadding().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NaepTextField(bio, onBioChanged, stringResource(R.string.bio), singleLine = false, minLines = 4, error = fieldErrors["bio"])
            NaepTextField(location, onLocationChanged, stringResource(R.string.location), error = fieldErrors["location"])
            CategoryDropdown(categories, categoryId, stringResource(R.string.primary_art_category), onCategoryChanged)
            NaepTextField(whatsapp, onWhatsappChanged, stringResource(R.string.whatsapp_number), keyboardType = KeyboardType.Phone, error = fieldErrors["whatsappNumber"], imeAction = ImeAction.Done, onImeAction = onSubmit)

            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            NaepButton(
                stringResource(R.string.open_a_gallery),
                onSubmit,
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, loading,
                enabled = !loading
            )
        }
    }
}
