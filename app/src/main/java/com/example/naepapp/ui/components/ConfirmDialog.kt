package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.theme.BrightSnow

// COMPONENT: ConfirmDialog (Yes = blackForest, Cancel = oxBlood) and SignInPromptSheet (for Guests)
@Composable
fun ConfirmDialog(title: String, message: String, onConfirm: () -> Unit, onDismiss: () -> Unit, loading: Boolean = false) {
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = BrightSnow,
        title = { Text(title) }, text = { Text(message) },
        confirmButton = { NaepButton(stringResource(R.string.yes), onConfirm, kind = NaepButtonKind.Confirm, loading = loading) },
        dismissButton = { NaepButton(stringResource(R.string.cancel), onDismiss, kind = NaepButtonKind.Destructive) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInPromptSheet(message: String, onSignIn: () -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = BrightSnow) {
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.sign_in_to_continue), style = MaterialTheme.typography.titleLarge)
            Text(message)
            NaepButton(stringResource(R.string.sign_in), onSignIn, Modifier.fillMaxWidth(), NaepButtonKind.Confirm)
            NaepButton(stringResource(R.string.cancel), onDismiss, Modifier.fillMaxWidth(), NaepButtonKind.Destructive)
        }
    }
}