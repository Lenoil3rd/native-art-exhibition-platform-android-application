package com.example.naepapp.ui.screens.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.components.CreationCard
import com.example.naepapp.ui.components.EmptyView
import com.example.naepapp.ui.components.MarketplaceFilterRow
import com.example.naepapp.ui.components.NaepTopBar
import com.example.naepapp.ui.model.FeedSort
import com.example.naepapp.ui.model.SampleData

@Composable
fun MarketplaceScreen(onOpenCreation: (String) -> Unit) {
    var sort by remember { mutableStateOf(FeedSort.Likes) }
    var categoryId by remember { mutableStateOf<Int?>(null) }
    // UI-only: filter and sort the sample list locally. Later this comes from the backend feed.
    val items = remember(sort, categoryId) {
        SampleData.creations
            .filter { categoryId == null || it.categoryId == categoryId }
            .sortedByDescending { if (sort == FeedSort.Likes) it.likes else it.views }
    }

    Scaffold(topBar = { NaepTopBar(stringResource(R.string.marketplace)) }) { pad ->
        Column(Modifier.padding(pad)) {
            Spacer(Modifier.height(8.dp))
            MarketplaceFilterRow(
                sort = sort,
                onSortSelected = { sort = it },
                categories = SampleData.categories,
                selectedCategoryId = categoryId,
                onCategorySelected = { categoryId = it }
            )
            Spacer(Modifier.height(8.dp))
            if (items.isEmpty()) EmptyView()
            else LazyVerticalGrid(
                GridCells.Adaptive(160.dp), contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(items, key = { it.id }) { c -> CreationCard(c, { onOpenCreation(c.id) }) }
            }
        }
    }
}
