package com.example.naepapp.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
import com.example.naepapp.ui.theme.Onyx

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.errorMessage?.let { err ->
        LaunchedEffect(err) {
            snackbarHostState.showSnackbar(err)
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateProfilePictureUri(uri)
        }
    }

    EditProfileContent(
        isArtisan = viewModel.isArtisan,
        userName = viewModel.userName,
        fullName = viewModel.fullName,
        bio = viewModel.bio,
        location = viewModel.location,
        whatsapp = viewModel.whatsapp,
        categoryId = viewModel.categoryId,
        categories = viewModel.categories,
        profilePictureUri = viewModel.profilePictureUri,
        profilePictureUrl = viewModel.profilePictureUrl,
        fieldErrors = viewModel.fieldErrors,
        saving = viewModel.saving,
        snackbarHostState = snackbarHostState,
        onChangePhoto = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        onFullNameChanged = viewModel::updateFullName,
        onBioChanged = viewModel::updateBio,
        onLocationChanged = viewModel::updateLocation,
        onWhatsappChanged = viewModel::updateWhatsapp,
        onCategoryChanged = viewModel::updateCategoryId,
        onSave = { viewModel.save(context, onSuccess = onBack) },
        onBack = onBack
    )
}

@Composable
fun EditProfileContent(
    isArtisan: Boolean,
    userName: String,
    fullName: String,
    bio: String,
    location: String,
    whatsapp: String,
    categoryId: Int?,
    categories: List<CategoryUi>,
    profilePictureUri: Uri?,
    profilePictureUrl: String?,
    fieldErrors: Map<String, String> = emptyMap(),
    saving: Boolean,
    snackbarHostState: SnackbarHostState,
    onChangePhoto: () -> Unit,
    onFullNameChanged: (String) -> Unit,
    onBioChanged: (String) -> Unit,
    onLocationChanged: (String) -> Unit,
    onWhatsappChanged: (String) -> Unit,
    onCategoryChanged: (Int?) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = { NaepTopBar("Edit Profile", onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { pad ->
        Column(
            Modifier.padding(pad).padding(24.dp).imePadding().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onChangePhoto),
                contentAlignment = Alignment.Center
            ) {
                when {
                    profilePictureUri != null -> {
                        ArtImage(imageUrl = profilePictureUri.toString(), width = 300, modifier = Modifier.fillMaxSize())
                    }
                    !profilePictureUrl.isNullOrBlank() -> {
                        ArtImage(imageUrl = profilePictureUrl, width = 300, modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        AvatarImage(userName, size = 96.dp)
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(32.dp)
                        .background(BrightSnow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_photo),
                        contentDescription = "Edit photo",
                        tint = Onyx,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text("Tap to change profile picture", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NaepTextField(
                    fullName, onFullNameChanged, stringResource(R.string.full_name),
                    error = fieldErrors["fullName"],
                    imeAction = if (isArtisan) ImeAction.Next else ImeAction.Done,
                    onImeAction = { if (!isArtisan) onSave() }
                )
                if (isArtisan) {
                    NaepTextField(bio, onBioChanged, stringResource(R.string.bio), singleLine = false, minLines = 4)
                    DistrictDropdown(selected = location.ifBlank { null }, onSelect = onLocationChanged)
                    CategoryDropdown(categories, categoryId, stringResource(R.string.primary_art_category), onCategoryChanged)
                    NaepTextField(
                        whatsapp, onWhatsappChanged, stringResource(R.string.whatsapp_number), keyboardType = KeyboardType.Phone,
                        error = fieldErrors["whatsapp"],
                        imeAction = ImeAction.Done,
                        onImeAction = onSave
                    )
                }
                Spacer(Modifier.height(12.dp))
                NaepButton(
                    stringResource(R.string.save),
                    onSave,
                    Modifier.fillMaxWidth(), NaepButtonKind.Confirm, saving
                )
            }
        }
    }
}
