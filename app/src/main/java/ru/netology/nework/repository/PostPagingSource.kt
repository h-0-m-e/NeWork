package ru.netology.nework.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.netology.nework.api.AppApi
import ru.netology.nework.dto.Post
import ru.netology.nework.error.NetworkError
import java.io.IOException

class PostPagingSource(
    private val api: AppApi
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null

    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try {
            val result = when (params) {
                is LoadParams.Append -> {
                    api.service.postGetBefore(id = params.key, count = params.loadSize)
                }

                is LoadParams.Prepend -> {
                    api.service.postGetAfter(id = params.key, count = params.loadSize)
                }

                is LoadParams.Refresh -> {
                    api.service.postGetLatest(params.loadSize)
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
