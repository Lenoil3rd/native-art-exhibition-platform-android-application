package com.example.naepapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.naepapp.R
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.FeedSort
import com.example.naepapp.ui.theme.BlackForest
import com.example.naepapp.ui.theme.BrightSnow
import com.example.naepapp.ui.theme.DustGrey
import com.example.naepapp.ui.theme.Onyx

// COMPONENT: NaepFilterChip, CategoryTabs, SortTabs (Marketplace filters)
@Composable
fun NaepFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick, label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = BrightSnow, labelColor = Onyx,
            selectedContainerColor = BlackForest, selectedLabelColor = BrightSnow),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
            borderColor = DustGrey, selectedBorderColor = BlackForest),
    )
}

@Composable
fun CategoryTabs(categories: List<CategoryUi>, selectedId: Int?, onSelect: (Int?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
        item { NaepFilterChip(stringResource(R.string.all_categories), selectedId == null) { onSelect(null) } }
        items(categories, key = { it.id }) { c -> NaepFilterChip(c.name, selectedId == c.id) { onSelect(c.id) } }
    }
}

@Composable
fun SortTabs(selected: FeedSort, onSelect: (FeedSort) -> Unit) {
    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NaepFilterChip(stringResource(R.string.most_liked), selected == FeedSort.Likes) { onSelect(FeedSort.Likes) }
        NaepFilterChip(stringResource(R.string.most_viewed), selected == FeedSort.Views) { onSelect(FeedSort.Views) }
    }
}