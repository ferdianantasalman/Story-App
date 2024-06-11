package com.example.androidintermediate.di

import android.content.Context
import com.example.androidintermediate.data.local.StoryDatabase
import com.example.androidintermediate.data.remote.ApiConfig
import com.example.androidintermediate.data.repository.StoryRepository

object Injection {
    fun provideRepository(context: Context, token: String): StoryRepository {
        val database = StoryDatabase.getInstance(context)
        val apiService = ApiConfig.getApiService()
        return StoryRepository(database, apiService, token)
    }
}