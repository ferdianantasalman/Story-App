package com.example.androidintermediate.views.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidintermediate.data.model.HistoryResponse
import com.example.androidintermediate.databinding.ItemHistoryBinding
import com.example.androidintermediate.helper.Helper

class HistoryAdapter(val data: ArrayList<HistoryResponse>) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(historyResponse: HistoryResponse) {
            binding.ivHistory.setImageBitmap(historyResponse.asset)
            binding.ivHistory.setOnClickListener {
                /* when image items clicked -> show preview original image of bitmap from exact path location */
                Helper.loadImageFromStorage(historyResponse.path)?.let {
                    Helper.showDialogPreviewImage(binding.root.context, it, historyResponse.path)
                }

            }
        }
    }
}