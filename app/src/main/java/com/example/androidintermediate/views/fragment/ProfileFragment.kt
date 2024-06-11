package com.example.androidintermediate.views.fragment

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.databinding.FragmentProfileBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.MainActivity
import com.example.androidintermediate.views.viewmodel.SettingViewModel

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playAnimation()

        val pref = SettingPreferences.getInstance((activity as MainActivity).dataStore)

        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserName.name)
            .observe(viewLifecycleOwner) {
                binding.tvName.text = it
            }

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserEmail.name)
            .observe(viewLifecycleOwner) {
                binding.tvEmail.text = it
            }

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserUID.name)
            .observe(viewLifecycleOwner) {
                binding.tvUid.text = it
            }

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserLastLogin.name)
            .observe(viewLifecycleOwner) {
                binding.tvLastLogin.text =
                    StringBuilder("Login pada ").append(Helper.getSimpleDateString(it))
            }

        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(viewLifecycleOwner) {
                if (it == Constant.preferenceDefaultValue) {
                    (activity as MainActivity).routeToAuth()
                }
            }

        binding.actionLogout.setOnClickListener {
            settingViewModel.clearUserPreferences()
        }

        binding.btnInfo.setOnClickListener {
            Helper.showDialogInfo(
                (activity as MainActivity),
                (activity as MainActivity).getString(R.string.UI_info_profile), Gravity.START
            )
        }

        binding.btnSetPermission.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri =
                Uri.fromParts("package", (activity as MainActivity).packageName, null)
            intent.data = uri
            (activity as MainActivity).startActivity(intent)
        }

        binding.btnSetLanguage.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        binding.btnSetDisplay.setOnClickListener {
            startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
        }
    }

    private fun playAnimation() {

        val avatarImageView = ObjectAnimator.ofFloat(binding.ivAvatar, View.ALPHA, 1f).setDuration(100)
        val nameTextView = ObjectAnimator.ofFloat(binding.tvName, View.ALPHA, 1f).setDuration(100)
        val emailTextView = ObjectAnimator.ofFloat(binding.tvEmail, View.ALPHA, 1f).setDuration(100)
        val uidTextView = ObjectAnimator.ofFloat(binding.tvUid, View.ALPHA, 1f).setDuration(100)
        val settingTextView = ObjectAnimator.ofFloat(binding.tvSetting, View.ALPHA, 1f).setDuration(100)
        val infoButton = ObjectAnimator.ofFloat(binding.btnInfo, View.ALPHA, 1f).setDuration(100)
        val setLanguageButton = ObjectAnimator.ofFloat(binding.btnSetLanguage, View.ALPHA, 1f).setDuration(100)
        val setPermissionButton = ObjectAnimator.ofFloat(binding.btnSetPermission, View.ALPHA, 1f).setDuration(100)
        val setDisplayButton = ObjectAnimator.ofFloat(binding.btnSetDisplay, View.ALPHA, 1f).setDuration(100)
        val logoutButton = ObjectAnimator.ofFloat(binding.actionLogout, View.ALPHA, 1f).setDuration(100)
        val lastLoginTextView = ObjectAnimator.ofFloat(binding.tvLastLogin, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                avatarImageView,
                nameTextView,
                emailTextView,
                uidTextView,
                settingTextView,
                infoButton,
                setLanguageButton,
                setPermissionButton,
                setDisplayButton,
                logoutButton,
                lastLoginTextView
            )
            startDelay = 100
        }.start()
    }
}