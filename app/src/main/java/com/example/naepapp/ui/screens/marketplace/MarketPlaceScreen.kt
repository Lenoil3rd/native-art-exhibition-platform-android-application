package com.example.naepapp.ui.screens.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.naepapp.R
import com.example.naepapp.core.common.ApiError
import com.example.naepapp.ui.components.*
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.model.FeedSort

@Composable
fun MarketplaceScreen(
    onOpenCreation: (String) -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val categoryId by viewModel.categoryId.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val pagingItems = viewModel.feed.collectAsLazyPagingItems()

    LifecycleResumeEffect(Unit) {
        if (isOnline) {
            pagingItems.refresh()
        }
        onPauseOrDispose {}
    }

    MarketplaceContent(
        sort = sort,
        onSortSelected = viewModel::setSort,
        categories = categories,
        selectedCategoryId = categoryId,
        onCategorySelected = viewModel::setCategory,
        pagingItems = pagingItems,
        isOnline = isOnline,
        onOpenCreation = onOpenCreation
    )
}

@Composable
fun MarketplaceContent(
    sort: FeedSort,
    onSortSelected: (FeedSort) -> Unit,
    categories: List<CategoryUi>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int?) -> Unit,
    pagingItems: LazyPagingItems<CreationUi>,
    isOnline: Boolean = true,
    onOpenCreation: (String) -> Unit
) {
    val refreshState = pagingItems.loadState.refresh

    Scaffold(topBar = { NaepTopBar(stringResource(R.string.marketplace)) }) { pad ->
        Column(Modifier.padding(pad)) {
            if (!isOnline) {
                OfflineBanner()
            }
            Spacer(Modifier.height(8.dp))
            MarketplaceFilterRow(
                sort = sort,
                onSortSelected = onSortSelected,
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected
            )
            Spacer(Modifier.height(8.dp))

            when {
                refreshState is LoadState.Loading && pagingItems.itemCount == 0 -> {
                    LoadingView()
                }
                refreshState is LoadState.Error && pagingItems.itemCount == 0 -> {
                    val e = refreshState.error
                    val message = (e as? ApiError)?.message ?: e.localizedMessage ?: "Failed to load feed"
                    ErrorView(message = message, onRetry = { pagingItems.refresh() })
                }
                pagingItems.itemCount == 0 -> {
                    val message = if (!isOnline) stringResource(R.string.not_available_offline_yet) else stringResource(R.string.nothing_here_yet)
                    EmptyView(message = message)
                }
                else -> {
                    LazyVerticalGrid(
                        GridCells.Adaptive(160.dp),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pagingItems.itemCount, key = { index -> pagingItems[index]?.id ?: index }) { index ->
                            pagingItems[index]?.let { creation ->
                                CreationCard(item = creation, onClick = { onOpenCreation(creation.id) })
                            }
                        }

                        if (pagingItems.loadState.append is LoadState.Loading) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
