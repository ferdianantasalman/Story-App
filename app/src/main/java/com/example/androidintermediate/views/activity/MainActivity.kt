package com.example.androidintermediate.views.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.data.remote.ApiConfig
import com.example.androidintermediate.databinding.ActivityMainBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.helper.ViewModelStoryFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.adapter.StoryAdapter
import com.example.androidintermediate.views.fragment.HistoryFragment
import com.example.androidintermediate.views.fragment.HomeFragment
import com.example.androidintermediate.views.fragment.ProfileFragment
import com.example.androidintermediate.views.fragment.SearchFragment
import com.example.androidintermediate.views.viewmodel.HistoryViewModel
import com.example.androidintermediate.views.viewmodel.SettingViewModel
import com.example.androidintermediate.views.viewmodel.StoryViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val historyViewModel: HistoryViewModel by viewModels()
    private val pref = SettingPreferences.getInstance(dataStore)
    private val settingViewModel: SettingViewModel by viewModels { ViewModelSettingFactory(pref) }
    private var tempToken = ""
    private var fragmentHome: HomeFragment? = null
    private lateinit var startNewStory: ActivityResultLauncher<Intent>

    @RequiresApi(Build.VERSION_CODES.M)
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragmentProfile = ProfileFragment()
        fragmentHome = HomeFragment()
        val fragmentSeacrh = SearchFragment()
        val fragmentHistory = HistoryFragment()

        startNewStory =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    fragmentHome?.onRefresh()
                }
            }

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(this) {
                tempToken = "Bearer $it"
                switchFragment(fragmentHome!!)
            }

        binding.bottomNavigationView.background = null

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    switchFragment(fragmentHome!!)
                }
                R.id.navigation_profile -> {
                    switchFragment(fragmentProfile)
                }
                R.id.navigation_search -> {
                    if (Helper.isPermissionGranted(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    ) {
                        switchFragment(fragmentSeacrh)
                        true
                    } else {
                        ActivityCompat.requestPermissions(
                            this@MainActivity,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ),
                            Constant.LOCATION_PERMISSION_CODE
                        )
                        false
                    }
                }
                R.id.navigation_history -> {
                    switchFragment(fragmentHistory)
                }
                else -> false
            }
            true
        }


        binding.fab.setOnClickListener {
            /* ask permission for camera first before launch camera */
            if (Helper.isPermissionGranted(this, Manifest.permission.CAMERA)) {
                val intent = Intent(this@MainActivity, CameraActivity::class.java)
                startNewStory.launch(intent)
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    Constant.CAMERA_PERMISSION_CODE
                )

            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            Constant.CAMERA_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.notifyGivePermission(this, "Berikan aplikasi izin mengakses kamera  ")
                }
            }

            Constant.LOCATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Helper.notifyGivePermission(
                        this,
                        "Berikan aplikasi izin lokasi untuk membaca lokasi  "
                    )
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        loadFolderData()
    }

    fun getUserToken() = tempToken

    fun getStoryViewModel(): StoryViewModel {
        val viewModel: StoryViewModel by viewModels {
            ViewModelStoryFactory(
                this,
                ApiConfig.getApiService(),
                getUserToken()
            )
        }
        return viewModel
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

    fun routeToAuth() = startActivity(Intent(this, AuthActivity::class.java))

    @OptIn(DelicateCoroutinesApi::class)
    private fun loadFolderData() {
        GlobalScope.launch {
            historyViewModel.loadImage(this@MainActivity)
        }
    }
}