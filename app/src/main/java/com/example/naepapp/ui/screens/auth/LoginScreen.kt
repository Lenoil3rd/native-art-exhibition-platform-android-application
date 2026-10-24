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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.core.network.dto.UserDto
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField

@Composable
fun LoginScreen(
    onSignIn: (UserDto) -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    LoginContent(
        identifier = viewModel.identifier,
        password = viewModel.password,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onIdentifierChanged = viewModel::updateIdentifier,
        onPasswordChanged = viewModel::updatePassword,
        onSignIn = { viewModel.login(onSuccess = onSignIn) },
        onRegister = onRegister,
        onGuest = onGuest
    )
}

@Composable
fun LoginContent(
    identifier: String,
    password: String,
    loading: Boolean,
    errorMessage: String?,
    onIdentifierChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSignIn: () -> Unit,
    onRegister: () -> Unit,
    onGuest: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()).imePadding(),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.app_full_name), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        NaepTextField(identifier, onIdentifierChanged, stringResource(R.string.phone_or_email))
        Spacer(Modifier.height(12.dp))
        NaepTextField(password, onPasswordChanged, stringResource(R.string.password), isPassword = true)

        if (!errorMessage.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(20.dp))
        NaepButton(
            stringResource(R.string.sign_in),
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(),
            kind = NaepButtonKind.Confirm,
            loading = loading,
            enabled = identifier.isNotBlank() && password.isNotBlank()
        )
        Spacer(Modifier.height(8.dp))
        NaepButton(stringResource(R.string.register), onRegister, Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        NaepButton(stringResource(R.string.continue_as_guest), onGuest, Modifier.fillMaxWidth())
    }
}
