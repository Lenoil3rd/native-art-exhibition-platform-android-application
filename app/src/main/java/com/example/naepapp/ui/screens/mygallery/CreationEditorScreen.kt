package com.example.naepapp.ui.screens.mygallery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.model.SampleData
import com.example.naepapp.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CreationEditorScreen(existing: CreationUi?, onBack: () -> Unit) {
    var title by remember { mutableStateOf(existing?.title.orEmpty()) }
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var categoryId by remember { mutableStateOf(existing?.categoryId) }
    var dimensions by remember { mutableStateOf(existing?.dimensions.orEmpty()) }
    var price by remember { mutableStateOf(existing?.price?.toLong()?.toString().orEmpty()) }
    var inventory by remember { mutableStateOf("1") }
    var available by remember { mutableStateOf(existing?.available ?: true) }
    var photos by remember { mutableIntStateOf(existing?.photoCount ?: 0) }       // TODO: real picked photos (PickMultipleVisualMedia)
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val valid = title.isNotBlank() && categoryId != null && photos > 0

    Scaffold(topBar = { NaepTopBar(stringResource(if (existing == null) R.string.add_creation else R.string.edit_creation), onBack) }) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { ArtImage(Modifier.size(88.dp).clip(RoundedCornerShape(12.dp))) }
            }
            NaepButton(stringResource(R.string.add_photos), { if (photos < 8) photos++ }, icon = painterResource(R.drawable.ic_add_photo))
            NaepTextField(title, { title = it }, stringResource(R.string.title))
            NaepTextField(description, { description = it }, stringResource(R.string.description), singleLine = false, minLines = 4)
            CategoryDropdown(SampleData.categories, categoryId, stringResource(R.string.category), { categoryId = it })
            NaepTextField(dimensions, { dimensions = it }, stringResource(R.string.dimensions) + " (e.g. 60 cm x 90 cm)")
            NaepTextField(price, { price = it }, stringResource(R.string.price_mwk), keyboardType = KeyboardType.Number, prefix = "MWK ")
            NaepTextField(inventory, { inventory = it }, stringResource(R.string.inventory_count), keyboardType = KeyboardType.Number)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.availability_status), Modifier.weight(1f))
                AvailabilityToggle(available, { available = !available })
            }
            NaepButton(stringResource(R.string.save),
                { scope.launch { saving = true; delay(600); saving = false; onBack() } },
                Modifier.fillMaxWidth(), NaepButtonKind.Confirm, saving, valid)
        }
    }
}