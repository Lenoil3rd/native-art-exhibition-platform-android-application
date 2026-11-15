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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.naepapp.R
import com.example.naepapp.ui.components.AvatarImage
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import com.example.naepapp.ui.components.NaepTopBar

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
        confirmPassword = viewModel.confirmPassword,
        fullNameError = viewModel.fullNameError,
        phoneError = viewModel.phoneError,
        emailError = viewModel.emailError,
        passwordError = viewModel.passwordError,
        confirmPasswordError = viewModel.confirmPasswordError,
        loading = viewModel.loading,
        errorMessage = viewModel.errorMessage,
        onFullNameChanged = viewModel::updateFullName,
        onPhoneChanged = viewModel::updatePhone,
        onEmailChanged = viewModel::updateEmail,
        onPasswordChanged = viewModel::updatePassword,
        onConfirmPasswordChanged = viewModel::updateConfirmPassword,
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
    confirmPassword: String,
    fullNameError: String? = null,
    phoneError: String? = null,
    emailError: String? = null,
    passwordError: String? = null,
    confirmPasswordError: String? = null,
    loading: Boolean,
    errorMessage: String?,
    onFullNameChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.register), onBack) }) { pad ->
        Column(
            Modifier.padding(pad).padding(24.dp).imePadding().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarImage(fullName.ifBlank { "User" }, size = 72.dp)
                Text("Enter your account details below", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            NaepTextField(
                fullName, onFullNameChanged, stringResource(R.string.full_name),
                error = fullNameError
            )
            NaepTextField(
                phone, onPhoneChanged, stringResource(R.string.phone_number),
                keyboardType = KeyboardType.Phone,
                error = phoneError
            )
            NaepTextField(
                email, onEmailChanged, stringResource(R.string.email_address),
                keyboardType = KeyboardType.Email,
                error = emailError
            )
            NaepTextField(
                password, onPasswordChanged, stringResource(R.string.password),
                isPassword = true,
                error = passwordError
            )
            NaepTextField(
                confirmPassword, onConfirmPasswordChanged, "Confirm Password",
                isPassword = true,
                error = confirmPasswordError,
                imeAction = ImeAction.Done,
                onImeAction = onRegister
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
                enabled = !loading
            )
        }
    }
}
