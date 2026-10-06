package com.example.naepapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

// COMPONENT: NaepFilterChip, MarketplaceFilterRow (Marketplace filters)
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
fun MarketplaceFilterRow(
    sort: FeedSort,
    onSortSelected: (FeedSort) -> Unit,
    categories: List<CategoryUi>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "sort_liked") {
            NaepFilterChip(
                label = stringResource(R.string.most_liked),
                selected = sort == FeedSort.Likes,
                onClick = { onSortSelected(FeedSort.Likes) }
            )
        }
        item(key = "sort_viewed") {
            NaepFilterChip(
                label = stringResource(R.string.most_viewed),
                selected = sort == FeedSort.Views,
                onClick = { onSortSelected(FeedSort.Views) }
            )
        }
        item(key = "divider") {
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .width(1.dp)
                    .height(24.dp)
                    .background(DustGrey)
            )
        }
        item(key = "category_all") {
            NaepFilterChip(
                label = stringResource(R.string.all_categories),
                selected = selectedCategoryId == null,
                onClick = { onCategorySelected(null) }
            )
        }
        items(categories, key = { "category_${it.id}" }) { c ->
            NaepFilterChip(
                label = c.name,
                selected = selectedCategoryId == c.id,
                onClick = { onCategorySelected(c.id) }
            )
        }
    }
}
