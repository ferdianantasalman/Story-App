package com.example.androidintermediate

import com.example.androidintermediate.data.model.LoginResponse
import com.example.androidintermediate.data.model.LoginResult
import com.example.androidintermediate.data.model.RegisterResponse
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.data.model.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

object DataDummy {

    fun generateDummyListStory(): List<StoryResponse.Story> {
        val items: MutableList<StoryResponse.Story> = arrayListOf()
        for (i in 0..100) {
            val quote = StoryResponse.Story(
                "story-FvU4u0Vp2S3PMsFg",
                "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                "2024-06-01T06:34:18.598Z",
                "Ferdi",
                "Lorem Ipsum",
                "-16.002",
                "-10.212"
            )
            items.add(quote)
        }
        return items
    }

}