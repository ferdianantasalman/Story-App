package com.example.androidintermediate.views.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.androidintermediate.R
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.databinding.ItemStoryBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.DetailActivity


class StoryAdapter : PagingDataAdapter<StoryResponse.Story, StoryAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var data = mutableListOf<StoryResponse.Story>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding =
            ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    inner class ViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(story: StoryResponse.Story) {
            with(binding) {
                tvItemName.text = story.name
                storyUploadTime.text =
                    "🕓 ${itemView.context.getString(R.string.text_uploaded)} ${
                        Helper.getTimelineUpload(
                            itemView.context,
                            story.createdAt
                        )
                    }"
                Glide.with(itemView)
                    .load(story.photoUrl)
                    .into(ivItemPhoto)

                ivShare.setOnClickListener {
                    val userShare = Intent()
                    userShare.action = Intent.ACTION_SEND
                    userShare.putExtra(
                        Intent.EXTRA_TEXT,
                        "${story.photoUrl}"
                    )
                    userShare.type = "text/plain"
                    itemView.context.startActivity(Intent.createChooser(userShare, "asdas"))
                }

                itemView.setOnClickListener {
                    val intent = Intent(itemView.context, DetailActivity::class.java)
                    intent.putExtra(Constant.StoryDetail.UserName.name, story.name)
                    intent.putExtra(Constant.StoryDetail.ImageURL.name, story.photoUrl)
                    intent.putExtra(
                        Constant.StoryDetail.ContentDescription.name,
                        story.description
                    )
                    intent.putExtra(
                        Constant.StoryDetail.UploadTime.name,
                        "${itemView.context.getString(R.string.text_uploaded)} ${
                            itemView.context.getString(
                                R.string.text_time_on
                            )
                        } ${Helper.getUploadStoryTime(story.createdAt)}"

                    )
                    intent.putExtra(
                        Constant.StoryDetail.Longitude.name,
                        story.lon
                    )
                    intent.putExtra(
                        Constant.StoryDetail.Latitude.name,
                        story.lat
                    )
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    val optionsCompat: ActivityOptionsCompat =
                        ActivityOptionsCompat.makeSceneTransitionAnimation(
                            itemView.context as Activity,
                            androidx.core.util.Pair(ivItemPhoto, "story_image"),
                            androidx.core.util.Pair(tvItemName, "user_name"),
                            androidx.core.util.Pair(defaultAvatar, "user_avatar"),
                        )
                    itemView.context.startActivity(intent, optionsCompat.toBundle())
                }
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StoryResponse.Story>() {
            override fun areItemsTheSame(oldItem: StoryResponse.Story, newItem: StoryResponse.Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: StoryResponse.Story, newItem: StoryResponse.Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }

}