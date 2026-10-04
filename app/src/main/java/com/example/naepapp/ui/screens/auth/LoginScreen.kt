package com.example.naepapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onSignIn: (identifier: String) -> Unit, onRegister: () -> Unit, onGuest: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.app_full_name), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        NaepTextField(identifier, { identifier = it }, stringResource(R.string.phone_or_email))
        Spacer(Modifier.height(12.dp))
        NaepTextField(password, { password = it }, stringResource(R.string.password), isPassword = true)
        Spacer(Modifier.height(20.dp))
        NaepButton(stringResource(R.string.sign_in),
            onClick = { scope.launch { loading = true; delay(600); loading = false; onSignIn(identifier) } },   // delay = fake request
            modifier = Modifier.fillMaxWidth(), kind = NaepButtonKind.Confirm, loading = loading,
            enabled = identifier.isNotBlank() && password.isNotBlank())
        Spacer(Modifier.height(8.dp))
        NaepButton(stringResource(R.string.register), onRegister, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        NaepButton(stringResource(R.string.continue_as_guest), onGuest, Modifier.fillMaxWidth())
    }
}