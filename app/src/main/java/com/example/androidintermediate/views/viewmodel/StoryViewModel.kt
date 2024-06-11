package com.example.androidintermediate.views.viewmodel

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.androidintermediate.R
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.data.model.StoryUploadResponse
import com.example.androidintermediate.data.remote.ApiConfig
import com.example.androidintermediate.data.repository.StoryRepository
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.utils.Constant
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class StoryViewModel(val storyRepository: StoryRepository) : ViewModel() {
    var loading = MutableLiveData(View.GONE)
    var isSuccessUploadStory = MutableLiveData(false)
    var isLocationPicked = MutableLiveData(false)
    val storyList = MutableLiveData<List<StoryResponse.Story>>()
    var error = MutableLiveData("")
    val isError = MutableLiveData(true)
    val coordinateLatitude = MutableLiveData(0.0)
    val coordinateLongitude = MutableLiveData(0.0)
    val coordinateTemp = MutableLiveData(Constant.indonesiaLocation)

    val story: LiveData<PagingData<StoryResponse.Story>> =
        storyRepository.getStory().cachedIn(viewModelScope)

    fun loadStoryWithLocationData(context: Context, token: String) {
        val client = ApiConfig.getApiService().getStoryListLocation(token, 50)
        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                if (response.isSuccessful) {
                    isError.postValue(false)
                    storyList.postValue(response.body()?.listStory)
                } else {
                    isError.postValue(true)
                    error.postValue("ERROR ${response.code()} : ${response.message()}")
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                isError.postValue(true)
                Log.e(Constant.TAG_STORY, "onFailure Call: ${t.message}")
                error.postValue("${context.getString(R.string.API_error_fetch_data)} : ${t.message}")
            }
        })
    }

    fun uploadNewStory(
        context: Context,
        token: String,
        image: File,
        description: String,
        withLocation: Boolean = false,
        lat: String? = null,
        lon: String? = null
    ) {
        loading.postValue(View.VISIBLE)
        "${image.length() / 1024 / 1024} MB"

        val storyDescription = description.toRequestBody("text/plain".toMediaType())
        val requestImageFile = image.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "photo",
            image.name,
            requestImageFile
        )

        val client = if (withLocation) {
            val positionLat = lat?.toRequestBody("text/plain".toMediaType())
            val positionLon = lon?.toRequestBody("text/plain".toMediaType())
            ApiConfig.getApiService()
                .doUploadImage(
                    token,
                    imageMultipart,
                    storyDescription,
                    positionLat!!,
                    positionLon!!
                )
        } else {
            ApiConfig.getApiService()
                .doUploadImage(token, imageMultipart, storyDescription)
        }
        client.enqueue(object : Callback<StoryUploadResponse> {
            override fun onResponse(call: Call<StoryUploadResponse>, response: Response<StoryUploadResponse>) {
                when (response.code()) {
                    401 -> error.postValue(context.getString(R.string.API_error_header_token))
                    413 -> error.postValue(context.getString(R.string.API_error_large_payload))
                    201 -> isSuccessUploadStory.postValue(true)
                    else -> error.postValue("Error ${response.code()} : ${response.message()}")
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<StoryUploadResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(Constant.TAG_STORY, "onFailure Call: ${t.message}")
                error.postValue("${context.getString(R.string.API_error_send_payload)} : ${t.message}")
            }
        })

    }
}
