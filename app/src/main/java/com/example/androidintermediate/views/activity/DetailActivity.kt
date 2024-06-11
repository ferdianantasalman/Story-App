package com.example.androidintermediate.views.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.androidintermediate.R
import com.example.androidintermediate.databinding.ActivityDetailBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.utils.Constant

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appBar = supportActionBar
        appBar!!.title = getString(R.string.detail_story)
        appBar!!.setDisplayHomeAsUpEnabled(true)

        binding.tvDetailName.text =
            intent.getData(Constant.StoryDetail.UserName.name, "Name")

        Glide.with(binding.root)
            .load(intent.getData(Constant.StoryDetail.ImageURL.name, ""))
            .into(binding.ivDetailPhoto)

        binding.tvDetailDescription.text =
            intent.getData(Constant.StoryDetail.ContentDescription.name, "Caption")

        binding.storyUploadTime.text =
            intent.getData(Constant.StoryDetail.UploadTime.name, "Upload time")

        try {
            val lat = intent.getData(Constant.StoryDetail.Latitude.name)
            val lon = intent.getData(Constant.StoryDetail.Longitude.name)
            Log.i("LOC_STORY", "Lat : ${lat.toDouble()} -- Lon : ${lon.toDouble()}")
            val address =
                Helper.parseAddressLocation(
                    this,
                    lat.toDouble(),
                    lon.toDouble()
                )
            binding.tvDetailLocation.text = address
            binding.tvDetailLocation.isVisible = true
        } catch (e: Exception) {
            Log.i("LOC_STORY_ERROR", "$e")
            binding.tvDetailLocation.isVisible = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun Intent.getData(key: String, defaultValue: String = "None"): String {
        return getStringExtra(key) ?: defaultValue
    }


}