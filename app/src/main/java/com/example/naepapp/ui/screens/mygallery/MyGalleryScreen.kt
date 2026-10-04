package com.example.naepapp.ui.screens.mygallery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.model.SampleData
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.OxBlood

@Composable
fun MyGalleryScreen(onAdd: () -> Unit, onEdit: (String) -> Unit, onOpenCreation: (String) -> Unit) {
    val items = remember { mutableStateListOf<CreationUi>().apply { addAll(SampleData.creationsOf(SampleData.myArtisanId)) } }
    var deletingId by remember { mutableStateOf<String?>(null) }          // id waiting for the Yes/Cancel dialog

    Scaffold(
        topBar = { NaepTopBar(stringResource(R.string.my_gallery)) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, containerColor = BlackForest, contentColor = BrightSnow,
                icon = { Icon(painterResource(R.drawable.ic_add), null) }, text = { Text(stringResource(R.string.add_creation)) })
        },
    ) { pad ->
        if (items.isEmpty()) EmptyView(Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items, key = { it.id }) { c ->
                Card(Modifier.fillMaxWidth().clickable { onOpenCreation(c.id) },
                    colors = CardDefaults.cardColors(containerColor = BrightSnow), border = BorderStroke(1.dp, DustGrey)) {
                    Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ArtImage(Modifier.size(88.dp).clip(RoundedCornerShape(12.dp)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(c.title, style = MaterialTheme.typography.titleSmall)
                            PriceText(c.price, c.currency)
                            AvailabilityToggle(c.available, {                                  // FR-8: one tap
                                val i = items.indexOfFirst { it.id == c.id }
                                if (i >= 0) items[i] = c.copy(available = !c.available)
                            })
                            StatsRow(c.likes, c.views)
                        }
                        Column {
                            IconButton({ onEdit(c.id) }) { Icon(painterResource(R.drawable.ic_edit), stringResource(R.string.edit_creation)) }
                            IconButton({ deletingId = c.id }) { Icon(painterResource(R.drawable.ic_delete), stringResource(R.string.delete), tint = OxBlood) }
                        }
                    }
                }
            }
        }
        deletingId?.let { id ->
            ConfirmDialog(stringResource(R.string.delete), "Delete this creation?",
                onConfirm = { items.removeAll { it.id == id }; deletingId = null }, onDismiss = { deletingId = null })
        }
    }
}