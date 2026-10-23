package com.example.naepapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.RoleCard
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind

@Composable
fun RoleSelectionScreen(
    onDone: (becameArtisan: Boolean) -> Unit,
    viewModel: RoleViewModel = hiltViewModel()
) {
    RoleSelectionContent(
        selectedMode = viewModel.selectedMode,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onSelectMode = viewModel::selectMode,
        onSubmit = { viewModel.submit(onSuccess = onDone) }
    )
}

@Composable
fun RoleSelectionContent(
    selectedMode: String,
    loading: Boolean,
    errorMessage: String?,
    onSelectMode: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.role_prompt_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        RoleCard(stringResource(R.string.role_enthusiast), stringResource(R.string.role_enthusiast_desc), selectedMode == "enthusiast") { onSelectMode("enthusiast") }
        Spacer(Modifier.height(12.dp))
        RoleCard(stringResource(R.string.role_artisan), stringResource(R.string.role_artisan_desc), selectedMode == "artisan") { onSelectMode("artisan") }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.role_prompt_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))
        NaepButton(stringResource(R.string.yes), onSubmit, Modifier.fillMaxWidth(), NaepButtonKind.Confirm, loading = loading)
    }
}
