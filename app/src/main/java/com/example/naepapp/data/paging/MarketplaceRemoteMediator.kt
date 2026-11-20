package com.example.naepapp.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.example.naepapp.core.db.NaepDatabase
import com.example.naepapp.core.db.entity.CreationEntity
import com.example.naepapp.core.db.entity.FeedEntryEntity
import com.example.naepapp.core.db.entity.FeedRemoteKeyEntity
import com.example.naepapp.core.network.ApiService
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class MarketplaceRemoteMediator(
    private val apiService: ApiService,
    private val database: NaepDatabase,
    private val sort: String?,
    private val categoryId: Int?
) : RemoteMediator<Int, CreationEntity>() {

    private val queryKey = "${sort ?: "likes"}|${categoryId ?: "all"}"

    override suspend fun initialize(): InitializeAction {
        val remoteKey = database.feedDao().getRemoteKey(queryKey)
        val cacheTimeout = TimeUnit.MINUTES.toMillis(30)
        return if (remoteKey != null && (System.currentTimeMillis() - remoteKey.fetchedAt < cacheTimeout)) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CreationEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val remoteKey = database.feedDao().getRemoteKey(queryKey)
                val nextPage = remoteKey?.nextPage
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                nextPage
            }
        }

        try {
            val response = apiService.getCreations(
                page = page,
                pageSize = state.config.pageSize,
                categoryId = categoryId,
                sort = sort
            )
            val items = response.data
            val meta = response.meta
            val endOfPaginationReached = meta?.hasMore != true
            val nextPage = if (endOfPaginationReached) null else page + 1

            val now = System.currentTimeMillis()
            val creationEntities = mutableListOf<CreationEntity>()
            val feedEntries = mutableListOf<FeedEntryEntity>()

            val currentFeedCount = if (loadType == LoadType.REFRESH) 0 else database.feedDao().getFeedEntriesCount(queryKey)

            items.forEachIndexed { index, dto ->
                val existing = database.creationDao().getById(dto.id)
                val creationEntity = CreationEntity(
                    id = dto.id,
                    artisanId = dto.artisanId,
                    categoryId = dto.categoryId,
                    categoryName = dto.categoryName,
                    title = dto.title,
                    description = existing?.description ?: dto.description.orEmpty(),
                    dimensions = existing?.dimensions ?: dto.dimensions.orEmpty(),
                    price = dto.price,
                    currency = dto.currency,
                    availabilityStatus = dto.availabilityStatus,
                    status = existing?.status ?: "published",
                    viewsCount = dto.viewsCount,
                    likesCount = dto.likesCount,
                    createdAt = existing?.createdAt ?: dto.createdAt,
                    updatedAt = existing?.updatedAt ?: dto.updatedAt,
                    coverImageUrl = dto.coverImageUrl,
                    artisanName = dto.artisanName,
                    artisanProfilePictureUrl = existing?.artisanProfilePictureUrl ?: dto.artisanProfilePictureUrl,
                    isLikedByMe = existing?.isLikedByMe ?: false,
                    isOwn = existing?.isOwn ?: false,
                    fetchedAt = now,
                    syncState = existing?.syncState ?: "SYNCED"
                )
                creationEntities.add(creationEntity)

                val position = currentFeedCount + index
                feedEntries.add(FeedEntryEntity(queryKey = queryKey, position = position, creationId = dto.id))
            }

            database.creationDao().upsertAll(creationEntities)
            database.feedDao().saveFeedPage(
                queryKey = queryKey,
                isRefresh = loadType == LoadType.REFRESH,
                feedEntries = feedEntries,
                remoteKey = FeedRemoteKeyEntity(
                    queryKey = queryKey,
                    nextPage = nextPage,
                    fetchedAt = now
                )
            )

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }
}
