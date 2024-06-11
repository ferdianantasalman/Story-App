package com.example.androidintermediate.views.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.data.remote.ApiConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.androidintermediate.databinding.ActivityPickLocationBinding
import com.example.androidintermediate.databinding.CustomTooltipPickLocationBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.helper.ViewModelStoryFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.viewmodel.SettingViewModel
import com.example.androidintermediate.views.viewmodel.StoryViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker

class PickLocationActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityPickLocationBinding

    private lateinit var storyViewModel: StoryViewModel
    private var tempToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val pref = SettingPreferences.getInstance(dataStore)

        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(this) { token ->
                tempToken = StringBuilder("Bearer ").append(token).toString()
            }

        storyViewModel =  ViewModelProvider(this, ViewModelStoryFactory(this, ApiConfig.getApiService(), tempToken))[StoryViewModel::class.java]

        binding.btnCancel.setOnClickListener {
            storyViewModel!!.isLocationPicked.postValue(false)
            finish()
        }
        binding.btnSelectLocation.setOnClickListener {
            /* check is location picked before next step */
            if (storyViewModel!!.isLocationPicked.value == true) {
                val intent = Intent()
                intent.putExtra(
                    Constant.LocationPicker.IsPicked.name,
                    storyViewModel!!.isLocationPicked.value
                )
                intent.putExtra(
                    Constant.LocationPicker.Latitude.name,
                    storyViewModel!!.coordinateLatitude.value
                )
                intent.putExtra(
                    Constant.LocationPicker.Longitude.name,
                    storyViewModel!!.coordinateLongitude.value
                )
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Helper.showDialogInfo(
                    this,
                    getString(R.string.UI_validation_maps_select_area)
                )
            }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
            isTiltGesturesEnabled = true
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constant.indonesiaLocation, 4f))
        mMap.setInfoWindowAdapter(this)
        mMap.setOnInfoWindowClickListener { marker ->
            postLocationSelected(marker.position.latitude, marker.position.longitude)
            marker.hideInfoWindow()
        }
        mMap.setOnMapClickListener {
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
            )?.showInfoWindow()
        }
        mMap.setOnPoiClickListener {
            Toast.makeText(this, it.name, Toast.LENGTH_SHORT).show()
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(
                        LatLng(
                            it.latLng.latitude,
                            it.latLng.longitude
                        )
                    )
            )?.showInfoWindow()
        }
        setMapStyle()
        getMyLastLocation()
    }

    private fun postLocationSelected(lat: Double, lon: Double) {
        val address =
            Helper.parseAddressLocation(
                this,
                lat,
                lon
            )
        binding.addressBar.text = address
        storyViewModel!!.isLocationPicked.postValue(true)
        storyViewModel!!.coordinateLatitude.postValue(lat)
        storyViewModel!!.coordinateLongitude.postValue(lon)
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        this,
                        R.raw.gmaps_style
                    )
                )
            if (!success) {
                Log.e("MAPS", "Style parsing failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e("MAPS", "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            when {
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                    getMyLastLocation()
                }
                else -> {
                }
            }
        }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
            checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(it.latitude, it.longitude))
                    )
                    mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 20f)
                    )
                    postLocationSelected(it.latitude, it.longitude)
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun getInfoContents(marker: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View {
        val bindingTooltips =
            CustomTooltipPickLocationBinding.inflate(LayoutInflater.from(this))
        bindingTooltips.location.text = Helper.parseAddressLocation(
            this,
            marker.position.latitude, marker.position.longitude
        )
        return bindingTooltips.root
    }
}