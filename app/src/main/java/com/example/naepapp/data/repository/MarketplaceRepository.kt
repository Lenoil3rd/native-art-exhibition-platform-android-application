package com.example.naepapp.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.db.NaepDatabase
import com.example.naepapp.core.db.dao.CategoryDao
import com.example.naepapp.core.db.dao.CreationDao
import com.example.naepapp.core.db.dao.FeedDao
import com.example.naepapp.core.db.toEntity
import com.example.naepapp.core.db.toUi
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.data.paging.MarketplaceRemoteMediator
import com.example.naepapp.ui.model.CategoryUi
import com.example.naepapp.ui.model.CreationUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketplaceRepository @Inject constructor(
    private val apiService: ApiService,
    private val database: NaepDatabase,
    private val categoryDao: CategoryDao,
    private val creationDao: CreationDao,
    private val feedDao: FeedDao
) {
    @OptIn(ExperimentalPagingApi::class)
    fun feed(sort: String?, categoryId: Int?): Flow<PagingData<CreationUi>> {
        val queryKey = "${sort ?: "likes"}|${categoryId ?: "all"}"
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            remoteMediator = MarketplaceRemoteMediator(
                apiService = apiService,
                database = database,
                sort = sort,
                categoryId = categoryId
            ),
            pagingSourceFactory = { creationDao.getFeed(queryKey) }
        ).flow.map { pagingData ->
            pagingData.map { it.toUi() }
        }
    }

    fun observeCategories(): Flow<List<CategoryUi>> {
        return categoryDao.observeAll().map { list ->
            list.map { it.toUi() }
        }
    }

    suspend fun categories(): Result<List<CategoryUi>> {
        return apiCall {
            val response = apiService.getCategories()
            val entities = response.data.map { it.toEntity() }
            categoryDao.upsertAll(entities)
            entities.map { it.toUi() }
        }
    }
}
