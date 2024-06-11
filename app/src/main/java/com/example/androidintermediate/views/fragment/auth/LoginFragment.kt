package com.example.androidintermediate.views.fragment.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.R
import com.example.androidintermediate.data.local.SettingPreferences
import com.example.androidintermediate.data.local.dataStore
import com.example.androidintermediate.databinding.FragmentLoginBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelFactory
import com.example.androidintermediate.helper.ViewModelSettingFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.AuthActivity
import com.example.androidintermediate.views.viewmodel.AuthViewModel
import com.example.androidintermediate.views.viewmodel.SettingViewModel


class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private var viewModel: AuthViewModel? = null
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext())
            .inflateTransition(android.R.transition.move)
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playAnimation()

        val pref = SettingPreferences.getInstance((activity as AuthActivity).dataStore)
        val settingViewModel =
            ViewModelProvider(this, ViewModelSettingFactory(pref))[SettingViewModel::class.java]

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory((activity as AuthActivity))
        )[AuthViewModel::class.java]

        viewModel?.let { vm ->
            vm.loginResult.observe(viewLifecycleOwner) { login ->
                settingViewModel.setUserPreferences(
                    login.loginResult.token,
                    login.loginResult.userId,
                    login.loginResult.name,
                    viewModel!!.tempEmail.value ?: Constant.preferenceDefaultValue
                )
            }
            vm.error.observe(viewLifecycleOwner) { error ->
                if (error.isNotEmpty()) {
                    Helper.showDialogInfo(requireContext(), error)
                }
            }
            vm.loading.observe(viewLifecycleOwner) { state ->
                binding.pBar.visibility = state
            }
        }
        settingViewModel.getUserPreferences(Constant.UserPreferences.UserToken.name)
            .observe(viewLifecycleOwner) { token ->
                if (token != Constant.preferenceDefaultValue) (activity as AuthActivity).routeToMainActivity()
            }
        binding.btnLogin.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
            when {
                email.isEmpty() or password.isEmpty() -> {
                    Helper.showDialogInfo(
                        requireContext(),
                        getString(R.string.UI_validation_empty_email_password)
                    )
                }
                !email.matches(Constant.emailPattern) -> {
                    Helper.showDialogInfo(
                        requireContext(),
                        getString(R.string.UI_validation_invalid_email)
                    )
                }
                password.length <= 6 -> {
                    Helper.showDialogInfo(
                        requireContext(),
                        getString(R.string.UI_validation_password_rules)
                    )
                }
                else -> {
                    viewModel?.login(email, password)
                }
            }
        }
        binding.tvRegister.setOnClickListener {
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.container, RegisterFragment(), RegisterFragment::class.java.simpleName)
                addSharedElement(binding.tvLogin, "auth")
                addSharedElement(binding.edLoginEmail, "email")
                addSharedElement(binding.edLoginPassword, "password")
                addSharedElement(binding.containerMisc, "misc")
                commit()
            }
        }
    }

    private fun playAnimation() {
        val welcomeTextView = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(100)
        val appNameTextView = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 1f).setDuration(100)
        val loginTextView = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(100)
        val emailEditText = ObjectAnimator.ofFloat(binding.edLoginEmail, View.ALPHA, 1f).setDuration(100)
        val passwordEditText = ObjectAnimator.ofFloat(binding.edLoginPassword, View.ALPHA, 1f).setDuration(100)
        val loginButton = ObjectAnimator.ofFloat(binding.btnLogin, View.ALPHA, 1f).setDuration(100)
        val haveTextView = ObjectAnimator.ofFloat(binding.tvHave, View.ALPHA, 1f).setDuration(100)
        val registerTextView = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                welcomeTextView,
                appNameTextView,
                loginTextView,
                emailEditText,
                passwordEditText,
                loginButton,
                haveTextView,
                registerTextView
            )
            startDelay = 100
        }.start()
    }
}