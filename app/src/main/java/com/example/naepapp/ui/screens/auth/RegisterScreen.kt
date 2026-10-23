package com.example.naepapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.AvatarImage
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.model.SampleData

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    RegisterContent(
        fullName = viewModel.fullName,
        phone = viewModel.phone,
        email = viewModel.email,
        password = viewModel.password,
        hasPicture = viewModel.hasPicture,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onFullNameChanged = viewModel::updateFullName,
        onPhoneChanged = viewModel::updatePhone,
        onEmailChanged = viewModel::updateEmail,
        onPasswordChanged = viewModel::updatePassword,
        onTogglePicture = viewModel::togglePicture,
        onRegister = { viewModel.register(onSuccess = onRegistered) },
        onBack = onBack
    )
}

@Composable
fun RegisterContent(
    fullName: String,
    phone: String,
    email: String,
    password: String,
    hasPicture: Boolean,
    loading: Boolean,
    errorMessage: String?,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePicture: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    val valid = fullName.length >= 2 && phone.length >= 9 && email.contains("@") && password.length >= 8

    Scaffold(topBar = { NaepTopBar(stringResource(R.string.register), onBack) }) { pad ->
        Column(
            Modifier.padding(pad).padding(24.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarImage(fullName.ifBlank { SampleData.registrationDefaults.name }, size = 72.dp)
                NaepButton(
                    text = stringResource(R.string.profile_picture_optional),
                    onClick = onTogglePicture
                )
            }
            NaepTextField(fullName, onFullNameChanged, stringResource(R.string.full_name))
            NaepTextField(phone, onPhoneChanged, stringResource(R.string.phone_number), keyboardType = KeyboardType.Phone)
            NaepTextField(email, onEmailChanged, stringResource(R.string.email_address), keyboardType = KeyboardType.Email)
            NaepTextField(
                password, onPasswordChanged, stringResource(R.string.password), isPassword = true,
                error = if (password.isNotEmpty() && password.length < 8) "At least 8 characters" else null
            )

            if (!errorMessage.isNullOrBlank()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            NaepButton(
                stringResource(R.string.register),
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                kind = NaepButtonKind.Confirm,
                loading = loading,
                enabled = valid
            )
        }
    }
}
