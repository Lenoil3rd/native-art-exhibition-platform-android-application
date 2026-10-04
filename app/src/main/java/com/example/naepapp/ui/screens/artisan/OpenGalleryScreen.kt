package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.model.SampleData
import com.example.naepapp.ui.components.CategoryDropdown
import com.example.naepapp.ui.components.NaepButton
import com.example.naepapp.ui.components.NaepButtonKind
import com.example.naepapp.ui.components.NaepTextField
import com.example.naepapp.ui.components.NaepTopBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OpenGalleryScreen(phone: String, onBack: () -> Unit, onDone: () -> Unit) {
    var bio by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    var whatsapp by remember { mutableStateOf(phone) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(topBar = { NaepTopBar(stringResource(R.string.open_a_gallery), onBack) }) { pad ->
        Column(Modifier.padding(pad).padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NaepTextField(bio, { bio = it }, stringResource(R.string.bio), singleLine = false, minLines = 4)
            NaepTextField(location, { location = it }, stringResource(R.string.location))
            CategoryDropdown(SampleData.categories, categoryId, stringResource(R.string.primary_art_category), { categoryId = it })
            NaepTextField(whatsapp, { whatsapp = it }, stringResource(R.string.whatsapp_number), keyboardType = KeyboardType.Phone)
            NaepButton(stringResource(R.string.open_a_gallery),
                { scope.launch { loading = true; delay(600); loading = false; onDone() } },
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, loading,
                enabled = bio.isNotBlank() && location.isNotBlank() && categoryId != null)
        }
    }
}