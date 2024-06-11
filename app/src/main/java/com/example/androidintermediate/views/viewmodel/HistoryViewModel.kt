package com.example.androidintermediate.views.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidintermediate.data.model.HistoryResponse
import com.example.androidintermediate.helper.Helper
import java.io.File

class HistoryViewModel : ViewModel() {

    val error = MutableLiveData("")

    val assetImageStory = MutableLiveData<ArrayList<HistoryResponse>>()

    val loadingStory = MutableLiveData(true)

    @Suppress("DEPRECATION")
    fun loadImage(context: Context) {
        assetImageStory.postValue(fetchImageData(context))
    }

    private fun fetchImageData(context: Context, mode: String = "story"): ArrayList<HistoryResponse> {
        loadingState(mode, true)
        val folderData = ArrayList<HistoryResponse>()

        @Suppress("DEPRECATION")
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, mode).apply { mkdirs() }
        }

        val files = mediaDir?.listFiles()
        for (i in files!!.indices) {
            val path = "${mediaDir.absolutePath}/${files[i].name}"
            val bitmap =
                Helper.loadImageFromStorage(path)?.let { Helper.resizeBitmap(it, 200, 200) }
            bitmap?.let {
                val folder = HistoryResponse(it, path)
                folderData.add(folder)
            }
        }
        folderData.reverse() // sort by recent files
        loadingState(mode, false)
        return folderData
    }

    private fun loadingState(mode: String, state: Boolean) {
        when (mode) {
            "story" -> loadingStory.postValue(state)
        }
    }
}