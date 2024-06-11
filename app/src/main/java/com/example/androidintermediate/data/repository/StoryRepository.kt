package com.example.androidintermediate.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.androidintermediate.data.local.StoryDatabase
import com.example.androidintermediate.data.mediator.StoryRemoteMediator
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.data.remote.ApiService

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token:String
) {
    fun getStory(): LiveData<PagingData<StoryResponse.Story>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(pageSize = 5),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService,token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }
}