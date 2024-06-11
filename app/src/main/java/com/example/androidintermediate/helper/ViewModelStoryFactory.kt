@file:Suppress("UNCHECKED_CAST")

package com.example.androidintermediate.helper

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.data.remote.ApiService
import com.example.androidintermediate.di.Injection
import com.example.androidintermediate.views.viewmodel.StoryViewModel

class ViewModelStoryFactory(val context: Context, private val apiService: ApiService,  val token:String) :
    ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoryViewModel(Injection.provideRepository(context, token)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}