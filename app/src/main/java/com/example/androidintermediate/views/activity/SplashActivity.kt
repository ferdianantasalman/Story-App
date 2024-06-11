package com.example.androidintermediate.views.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.databinding.ActivitySplashBinding
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.viewmodel.SettingViewModel
import java.util.Timer
import kotlin.concurrent.schedule

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    private val settingViewModel by viewModels<SettingViewModel> {
        SettingViewModel.Factory(SettingPreferences.getInstance((dataStore)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(this) { token ->
                if (token == Constant.preferenceDefaultValue) Timer().schedule(2000) {
                    startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                    finish()
                } else Timer().schedule(2000) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
    }
}