package com.example.naepapp.ui.screens.artisan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CreationUi

@Composable
fun GalleryScreen(creations: List<CreationUi>, onBack: () -> Unit, onOpenCreation: (String) -> Unit) {
    val grouped = remember(creations) { creations.groupBy { it.categoryName } }     // grouped by category
    Scaffold(topBar = { NaepTopBar(stringResource(R.string.gallery), onBack) }) { pad ->
        if (creations.isEmpty()) EmptyView(Modifier.padding(pad))
        else LazyColumn(Modifier.padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            grouped.forEach { (category, list) ->
                item { SectionHeader(category) }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(list, key = { it.id }) { c -> CreationCard(c, { onOpenCreation(c.id) }, Modifier.width(180.dp)) }
                    }
                }
            }
        }
    }
}