package com.example.androidintermediate.data.remote

import com.example.androidintermediate.data.model.LoginResponse
import com.example.androidintermediate.data.model.RegisterResponse
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.data.model.StoryUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    @FormUrlEncoded
    fun doLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @POST("register")
    @FormUrlEncoded
    fun doRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories")
    suspend fun getStoryList(
        @Header("Authorization") token:String,
        @Query("page") page: Int,
        @Query("size") size:Int
    ): StoryResponse

    @GET("stories?location=1")
    fun getStoryListLocation(
        @Header("Authorization") token: String,
        @Query("size") size: Int
    ): Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun doUploadImage(
        @Header("Authorization") token:String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<StoryUploadResponse>

    @Multipart
    @POST("stories")
    fun doUploadImage(
        @Header("Authorization") token:String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody,
        @Part("lon") lon: RequestBody
    ): Call<StoryUploadResponse>
}