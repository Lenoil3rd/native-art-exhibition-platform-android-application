package com.example.naepapp.ui.screens.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.naepapp.data.repository.MarketplaceRepository
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.CreationUi
import com.example.naepapp.ui.model.FeedSort
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: MarketplaceRepository
) : ViewModel() {

    private val _sort = MutableStateFlow(FeedSort.Likes)
    val sort: StateFlow<FeedSort> = _sort.asStateFlow()

    private val _categoryId = MutableStateFlow<Int?>(null)
    val categoryId: StateFlow<Int?> = _categoryId.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryUi>>(emptyList())
    val categories: StateFlow<List<CategoryUi>> = _categories.asStateFlow()

    val feed: Flow<PagingData<CreationUi>> = combine(_sort, _categoryId) { s, c ->
        Pair(s, c)
    }.flatMapLatest { (s, c) ->
        val sortString = when (s) {
            FeedSort.Likes -> "likes"
            FeedSort.Views -> "views"
        }
        repository.feed(sortString, c)
    }.cachedIn(viewModelScope)

    init {
        loadCategories()
    }

    fun setSort(sort: FeedSort) {
        _sort.value = sort
    }

    fun setCategory(categoryId: Int?) {
        _categoryId.value = categoryId
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.categories().fold(
                onSuccess = { _categories.value = it },
                onFailure = { _categories.value = emptyList() }
            )
        }
    }
}
