package com.example.androidintermediate.views.viewmodel

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidintermediate.R
import com.example.androidintermediate.data.model.LoginResponse
import com.example.androidintermediate.data.model.RegisterResponse
import com.example.androidintermediate.data.remote.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AuthViewModel(val context: Context) : ViewModel() {

    var loading = MutableLiveData(View.GONE)
    val error = MutableLiveData("")
    val tempEmail = MutableLiveData("") // hold email to saved with user preferences

    val loginResult = MutableLiveData<LoginResponse>()
    val registerResult = MutableLiveData<RegisterResponse>()

    private val TAG = AuthViewModel::class.simpleName

    fun login(email: String, password: String) {
        tempEmail.postValue(email)
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().doLogin(email, password)
        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                when (response.code()) {
                    400 -> error.postValue(context.getString(R.string.API_error_email_invalid))
                    401 -> error.postValue(context.getString(R.string.API_error_unauthorized))
                    200 -> loginResult.postValue(response.body())
                    else -> error.postValue("ERROR ${response.code()} : ${response.message()}")
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(TAG, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }
        })
    }

    fun register(name: String, email: String, password: String) {
        loading.postValue(View.VISIBLE)
        val client = ApiConfig.getApiService().doRegister(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                when (response.code()) {
                    400 -> error.postValue(context.getString(R.string.API_error_email_invalid))
                    201 -> registerResult.postValue(response.body())
                    else -> error.postValue("ERROR ${response.code()} : ${response.errorBody()}")
                }
                loading.postValue(View.GONE)
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                loading.postValue(View.GONE)
                Log.e(TAG, "onFailure Call: ${t.message}")
                error.postValue(t.message)
            }
        })
    }
}