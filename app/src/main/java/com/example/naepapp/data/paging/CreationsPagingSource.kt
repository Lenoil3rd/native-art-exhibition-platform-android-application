package com.example.naepapp.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.naepapp.core.common.apiCall
import com.example.naepapp.core.network.ApiService
import com.example.naepapp.core.network.dto.CreationDetailDto
import com.example.naepapp.core.network.dto.CreationSummaryDto
import com.example.naepapp.ui.model.CreationUi

fun CreationSummaryDto.toUi(): CreationUi {
    return CreationUi(
        id = id,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        artisanId = artisanId,
        artisanName = artisanName,
        price = price,
        currency = currency,
        available = availabilityStatus == "available",
        likes = likesCount,
        views = viewsCount,
        imageUrl = coverImageUrl.orEmpty(),
        dimensions = dimensions.orEmpty(),
        description = description.orEmpty(),
        photoCount = 3
    )
}

fun CreationDetailDto.toUi(): CreationUi {
    return CreationUi(
        id = id,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        artisanId = artisanId,
        artisanName = artisanName,
        price = price,
        currency = currency,
        available = availabilityStatus == "available",
        likes = likesCount,
        views = viewsCount,
        imageUrl = coverImageUrl ?: media.firstOrNull()?.url.orEmpty(),
        dimensions = dimensions.orEmpty(),
        description = description.orEmpty(),
        photoCount = media.size.coerceAtLeast(1)
    )
}

class CreationsPagingSource(
    private val apiService: ApiService,
    private val sort: String?,
    private val categoryId: Int?
) : PagingSource<Int, CreationUi>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CreationUi> {
        val page = params.key ?: 1
        val pageSize = params.loadSize.coerceAtMost(50)

        val result = apiCall {
            apiService.getCreations(
                page = page,
                pageSize = pageSize,
                categoryId = categoryId,
                sort = sort
            )
        }

        return result.fold(
            onSuccess = { envelope ->
                val items = envelope.data.map { it.toUi() }
                val meta = envelope.meta
                val hasMore = meta?.hasMore == true
                val nextKey = if (hasMore) page + 1 else null
                LoadResult.Page(
                    data = items,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = nextKey
                )
            },
            onFailure = { throwable ->
                LoadResult.Error(throwable)
            }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, CreationUi>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
