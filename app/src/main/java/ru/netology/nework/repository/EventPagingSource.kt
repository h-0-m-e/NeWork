package ru.netology.nework.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.api.AppApi
import ru.netology.nework.dto.Event
import ru.netology.nework.error.NetworkError
import java.io.IOException

class EventPagingSource(
    private val api: AppApi
) : PagingSource<Long, Event>() {
    override fun getRefreshKey(state: PagingState<Long, Event>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Event> {
        try {
            val result = when (params) {
                is LoadParams.Append -> {
                    api.service.eventGetBefore(id = params.key, count = params.loadSize)
                }

                is LoadParams.Prepend -> {
                    api.service.eventGetAfter(id = params.key, count = params.loadSize)
                }

                is LoadParams.Refresh -> {
                    api.service.eventGetLatest(params.loadSize)
                }
            }

            if (!result.isSuccessful) {
                throw NetworkError
            }

            val data = result.body().orEmpty()
            return LoadResult.Page(
                data = data,
                prevKey = params.key,
                nextKey = data.lastOrNull()?.id?.toLong()
            )
        } catch (e: IOException){
            return LoadResult.Error(e)
        }

    }
}
