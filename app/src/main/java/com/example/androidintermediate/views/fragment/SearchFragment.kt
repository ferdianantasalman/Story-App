package com.example.androidintermediate.views.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.data.model.StoryResponse
import com.example.androidintermediate.databinding.CustomTooltipMapsSearchBinding
import com.example.androidintermediate.databinding.FragmentSearchBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.DetailActivity
import com.example.androidintermediate.views.activity.MainActivity
import com.example.androidintermediate.views.viewmodel.SettingViewModel
import com.example.androidintermediate.views.viewmodel.StoryViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class SearchFragment : Fragment(), OnMapReadyCallback, GoogleMap.InfoWindowAdapter,
    AdapterView.OnItemSelectedListener {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentSearchBinding

    private lateinit var storyViewModel: StoryViewModel
    private var tempToken = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val pref = SettingPreferences.getInstance((activity as MainActivity).dataStore)

        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(viewLifecycleOwner) { token ->
                tempToken = StringBuilder("Bearer ").append(token).toString()
            }

        storyViewModel = (activity as MainActivity).getStoryViewModel()

        val zoomLevel = arrayOf(
            getString(R.string.text_adapter_maps_default),
            getString(R.string.text_adapter_maps_province),
            getString(R.string.text_adapter_maps_city),
            getString(R.string.text_adapter_maps_district),
            getString(R.string.text_adapter_maps_around)
        )

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item, zoomLevel
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.zoomType.adapter = adapter
        binding.zoomType.onItemSelectedListener = this

        val mapFragment =
            (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true

        storyViewModel!!.storyList.observe(viewLifecycleOwner) { storyList ->
            for (story in storyList) {
                mMap.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            story.lat?.toDouble() ?: 0.0,
                            story.lon?.toDouble() ?: 0.0
                        )
                    )
                )?.tag = story
            }
        }

        mMap.setInfoWindowAdapter(this)
        mMap.setOnInfoWindowClickListener { marker ->
            val data: StoryResponse.Story = marker.tag as StoryResponse.Story
            routeToDetailStory(data)
        }
        getMyLocation()
        setMapStyle()

        storyViewModel!!.loadStoryWithLocationData(
            requireContext(),
            tempToken
        )
        storyViewModel!!.coordinateTemp.observe(this) {
            CameraUpdateFactory.newLatLngZoom(it, 4f)
        }
    }

    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(marker: Marker): View? {
        val bindingTooltips =
            CustomTooltipMapsSearchBinding.inflate(LayoutInflater.from(requireContext()))
        val data: StoryResponse.Story = marker.tag as StoryResponse.Story
        bindingTooltips.labelLocation.text = Helper.parseAddressLocation(
            requireContext(),
            marker.position.latitude, marker.position.longitude
        )
        bindingTooltips.name.text = StringBuilder("Story by ").append(data.name)
        bindingTooltips.image.setImageBitmap(Helper.bitmapFromURL(requireContext(), data.photoUrl))
        bindingTooltips.storyDescription.text = data.description
        bindingTooltips.storyUploadTime.text = Helper.getUploadStoryTime(data.createdAt)
        return bindingTooltips.root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val level: Float = when (position) {
            0 -> 4f
            1 -> 8f
            2 -> 11f
            3 -> 14f
            4 -> 17f
            else -> 4f
        }
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(storyViewModel!!.coordinateTemp.value!!, level)
        )
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(Constant.indonesiaLocation, 4f)
        )
    }

    private fun routeToDetailStory(data: StoryResponse.Story) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra(Constant.StoryDetail.UserName.name, data.name)
        intent.putExtra(Constant.StoryDetail.ImageURL.name, data.photoUrl)
        intent.putExtra(
            Constant.StoryDetail.ContentDescription.name,
            data.description
        )
        intent.putExtra(
            Constant.StoryDetail.UploadTime.name,
            /*
            dynamic set uploaded time locally
                en : uploaded + on + 30 April 2022 00.00
                id : diupload + pada + 30 April 2022 00.00
            */
            "${requireContext().getString(R.string.text_uploaded)} ${
                requireContext().getString(
                    R.string.text_time_on
                )
            } ${Helper.getUploadStoryTime(data.createdAt)}"
        )
        intent.putExtra(Constant.StoryDetail.Latitude.name, data.lat.toString())
        intent.putExtra(Constant.StoryDetail.Longitude.name, data.lon.toString())
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        requireContext().startActivity(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            } else {
                Helper.notifyGivePermission(
                    requireContext(),
                    getString(R.string.validation_permission_location)
                )
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                (activity as MainActivity),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    storyViewModel!!.coordinateTemp.postValue(
                        LatLng(
                            location.latitude,
                            location.longitude
                        )
                    )
                } else {
                    storyViewModel!!.coordinateTemp.postValue(Constant.indonesiaLocation)
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setMapStyle() {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        (activity as MainActivity),
                        R.raw.gmaps_style
                    )
                )
            if (!success) {
                Log.e(Constant.TAG_MAPS, "Style parsing  failed.")
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(Constant.TAG_MAPS, "Can't find style. Error: ", exception)
        }
    }
}