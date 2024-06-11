package com.example.androidintermediate

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.androidintermediate.data.model.StoryResponse

class StoryPagingSource : PagingSource<Int, LiveData<List<StoryResponse.Story>>>() {

    companion object {
        fun snapshot(items: List<StoryResponse.Story>): PagingData<StoryResponse.Story> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<StoryResponse.Story>>>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<StoryResponse.Story>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }

}