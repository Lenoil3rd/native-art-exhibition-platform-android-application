package com.example.naepapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.data.paging.CreationsPagingSource
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.CreationUi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val apiService: ApiService
) {
    fun feed(sort: String?, categoryId: Int?): Flow<PagingData<CreationUi>> {
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { CreationsPagingSource(apiService, sort, categoryId) }
        ).flow
    }

    suspend fun categories(): Result<List<CategoryUi>> {
        return apiCall {
            val response = apiService.getCategories()
            response.data.map { dto ->
                CategoryUi(
                    id = dto.id,
                    name = dto.name,
                    bio = dto.description.orEmpty()
                )
            }
        }
    }
}
