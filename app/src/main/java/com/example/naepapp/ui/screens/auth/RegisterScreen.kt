package com.example.naepapp.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.AvatarImage
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.model.DemoConfig
import com.example.naepapp.ui.model.SampleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(onRegistered: (name: String, email: String, phone: String) -> Unit, onBack: () -> Unit) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var hasPicture by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val valid = fullName.length >= 2 && phone.length >= 9 && email.contains("@") && password.length >= 8

    Scaffold(topBar = { NaepTopBar(stringResource(R.string.register), onBack) }) { pad ->
        Column(Modifier.padding(pad).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AvatarImage(fullName.ifBlank { SampleData.registrationDefaults.name }, size = 72.dp)
                NaepButton(
                    text = stringResource(R.string.profile_picture_optional),
                    onClick = { hasPicture = !hasPicture })
            }
            NaepTextField(fullName, { fullName = it }, stringResource(R.string.full_name))
            NaepTextField(phone, { phone = it }, stringResource(R.string.phone_number), keyboardType = KeyboardType.Phone)
            NaepTextField(email, { email = it }, stringResource(R.string.email_address), keyboardType = KeyboardType.Email)
            NaepTextField(password, { password = it }, stringResource(R.string.password), isPassword = true,
                error = if (!DemoConfig.SKIP_AUTH_VALIDATION && password.isNotEmpty() && password.length < 8) "At least 8 characters" else null)
            NaepButton(stringResource(R.string.register),
                {
                    scope.launch {
                        loading = true
                        delay(600)
                        loading = false
                        onRegistered(
                            fullName.ifBlank { SampleData.registrationDefaults.name },
                            email.ifBlank { SampleData.registrationDefaults.email },
                            phone.ifBlank { SampleData.registrationDefaults.phone }
                        )
                    }
                },
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, loading,
                enabled = DemoConfig.SKIP_AUTH_VALIDATION || valid)
        }
    }
}
