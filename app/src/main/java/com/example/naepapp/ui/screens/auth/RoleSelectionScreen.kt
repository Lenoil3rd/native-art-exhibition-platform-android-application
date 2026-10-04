package com.example.naepapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.RoleCard
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind

@Composable
fun RoleSelectionScreen(onDone: (becameArtisan: Boolean) -> Unit) {
    var selected by remember { mutableStateOf("enthusiast") }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.role_prompt_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        RoleCard(stringResource(R.string.role_enthusiast), stringResource(R.string.role_enthusiast_desc), selected == "enthusiast") { selected = "enthusiast" }
        Spacer(Modifier.height(12.dp))
        RoleCard(stringResource(R.string.role_artisan), stringResource(R.string.role_artisan_desc), selected == "artisan") { selected = "artisan" }
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.role_prompt_note), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(24.dp))
        NaepButton(stringResource(R.string.yes), { onDone(selected == "artisan") }, Modifier.fillMaxWidth(), NaepButtonKind.Confirm)
    }
}