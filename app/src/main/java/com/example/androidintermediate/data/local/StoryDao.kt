package com.example.androidintermediate.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.androidintermediate.data.model.StoryResponse

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: List<StoryResponse.Story>)

    @Query("SELECT * FROM story order by createdAt DESC")
    fun getAllStory(): PagingSource<Int, StoryResponse.Story>

    @Query("DELETE FROM story")
    suspend fun deleteAll()
}