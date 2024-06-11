package com.example.androidintermediate.views.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import java.io.File
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.data.remote.ApiConfig
import com.example.androidintermediate.databinding.ActivityStoryBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.helper.ViewModelStoryFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.adapter.StoryAdapter
import com.example.androidintermediate.views.viewmodel.SettingViewModel
import com.example.androidintermediate.views.viewmodel.StoryViewModel
import com.google.android.gms.maps.model.LatLng

class StoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryBinding
    private lateinit var storyViewModel: StoryViewModel

    private val storyAdapter = StoryAdapter()
    private var tempToken = ""
    private var isPicked: Boolean? = false
    private var getResult: ActivityResultLauncher<Intent>? = null
    var location: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val appBar = supportActionBar
        appBar!!.title = getString(R.string.upload_story)
        appBar!!.setDisplayHomeAsUpEnabled(true)

        val pref = SettingPreferences.getInstance(dataStore)

        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(this) { token ->
                tempToken = StringBuilder("Bearer ").append(token).toString()
            }

        storyViewModel =  ViewModelProvider(this, ViewModelStoryFactory(this, ApiConfig.getApiService(), tempToken))[StoryViewModel::class.java]



        val myFile = intent?.getSerializableExtra(EXTRA_PHOTO_RESULT) as File
        val isBackCamera = intent?.getBooleanExtra(EXTRA_CAMERA_MODE, true) as Boolean

        val rotatedBitmap = Helper.rotateBitmap(
            BitmapFactory.decodeFile(myFile.path),
            isBackCamera)

        Log.i("HAHA", "SADSADSA")

        binding.storyImage.setImageBitmap(rotatedBitmap)

        getResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { res ->
                    isPicked = res.getBooleanExtra(Constant.LocationPicker.IsPicked.name, false)
                    storyViewModel!!.isLocationPicked.postValue(isPicked)
                    val lat = res.getDoubleExtra(
                        Constant.LocationPicker.Latitude.name,
                        0.0
                    )
                    val lon = res.getDoubleExtra(
                        Constant.LocationPicker.Longitude.name,
                        0.0
                    )
                    binding.fieldLocation.text = Helper.parseAddressLocation(this, lat, lon)
                    storyViewModel!!.coordinateLatitude.postValue(lat)
                    storyViewModel!!.coordinateLongitude.postValue(lon)
                }
            }
        }

        binding.btnSelectLocation.setOnClickListener {
            if (Helper.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                val intentPickLocation = Intent(this, PickLocationActivity::class.java)
                getResult?.launch(intentPickLocation)
            } else {
                ActivityCompat.requestPermissions(
                    this@StoryActivity,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    Constant.LOCATION_PERMISSION_CODE
                )
            }
        }

        binding.buttonAdd.setOnClickListener {
            if (binding.edAddDescription.text.isNotEmpty()) {
                uploadImage(myFile, binding.edAddDescription.text.toString())
                storyAdapter.refresh()

            } else {
                Helper.showDialogInfo(
                    this,
                    getString(R.string.UI_validation_empty_story_description)
                )
            }
        }

        binding.btnClearLocation.setOnClickListener {
            storyViewModel!!.isLocationPicked.postValue(false)
        }

        storyViewModel.isSuccessUploadStory.observe(this) {
                if (it) {
                    val dialog = Helper.dialogInfoBuilder(
                        this,
                        getString(R.string.API_success_upload_image)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        navigateToMain()
                    }
                    dialog.show()
                }

            }

        storyViewModel.loading.observe(this) {
                binding.pBar.visibility = it
            }

        storyViewModel.error.observe(this) {
                if (it.isNotEmpty()) {
                    Helper.showDialogInfo(this, it)
                }
            }

        storyViewModel.isLocationPicked.observe(this) {
                /* if location picked -> show picked location address, else -> hide address & show pick location button */
                binding.previewLocation.isVisible = it
                binding.btnSelectLocation.isVisible = !it
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun uploadImage(image: File, description: String) {
        if (tempToken != null) {
            if (storyViewModel.isLocationPicked.value != true) {
                /* location not picked -> upload without location */
                storyViewModel.uploadNewStory(
                    this,
                    tempToken,
                    image,
                    description
                )
            } else {
                /* location picked -> upload with location */
                storyViewModel.uploadNewStory(
                    this,
                    tempToken,
                    image,
                    description,
                    true,
                    storyViewModel.coordinateLatitude.value.toString(),
                    storyViewModel.coordinateLongitude.value.toString(),
                )
            }
        } else {
            Helper.showDialogInfo(
                this,
                getString(R.string.API_error_header_token)
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constant.LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.notifyGivePermission(
                        this,
                        getString(R.string.validation_permission_location)
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    companion object {
        const val EXTRA_PHOTO_RESULT = "PHOTO_RESULT"
        const val EXTRA_CAMERA_MODE = "CAMERA_MODE"
    }
}