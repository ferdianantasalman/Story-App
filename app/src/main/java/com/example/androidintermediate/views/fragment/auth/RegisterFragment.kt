package com.example.androidintermediate.views.fragment.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.androidintermediate.R
import com.example.androidintermediate.databinding.FragmentRegisterBinding
import com.example.androidintermediate.helper.Helper
import com.example.androidintermediate.helper.ViewModelFactory
import com.example.androidintermediate.utils.Constant
import com.example.androidintermediate.views.activity.AuthActivity
import com.example.androidintermediate.views.viewmodel.AuthViewModel


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    private var viewModel: AuthViewModel? = null

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
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playAnimation()

        viewModel = ViewModelProvider(
            this,
            ViewModelFactory((activity as AuthActivity))
        )[AuthViewModel::class.java]

        viewModel?.let { vm ->
            vm.registerResult.observe(viewLifecycleOwner) { register ->
                if (!register.error) {
                    val dialog = Helper.dialogInfoBuilder(
                        (activity as AuthActivity),
                        getString(R.string.UI_info_successful_register_user)
                    )
                    val btnOk = dialog.findViewById<Button>(R.id.button_ok)
                    btnOk.setOnClickListener {
                        dialog.dismiss()
                        switchToLoginPage()
                    }
                    dialog.show()
                }
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

        binding.tvLogin.setOnClickListener {
            switchToLoginPage()
        }

        binding.btnRegister.setOnClickListener {
            val nama = binding.edRegisterName.text.toString()
            val email = binding.edRegisterEmail.text.toString()
            val password = binding.edRegisterPassword.text.toString()
            when {
                email.isEmpty() or password.isEmpty() or nama.isEmpty() -> {
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
                    viewModel?.register(nama, email, password)
                }
            }
        }
    }

    private fun switchToLoginPage() {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.container, LoginFragment(), LoginFragment::class.java.simpleName)
            /* shared element transition to main activity */
            addSharedElement(binding.tvRegister, "auth")
            addSharedElement(binding.edRegisterEmail, "email")
            addSharedElement(binding.edRegisterPassword, "password")
            addSharedElement(binding.containerMisc, "misc")
            commit()
        }
    }

    private fun playAnimation() {

        val welcomeTextView = ObjectAnimator.ofFloat(binding.tvWelcome, View.ALPHA, 1f).setDuration(100)
        val appNameTextView = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 1f).setDuration(100)
        val registerTextView = ObjectAnimator.ofFloat(binding.tvRegister, View.ALPHA, 1f).setDuration(100)
        val nameEditText = ObjectAnimator.ofFloat(binding.edRegisterName, View.ALPHA, 1f).setDuration(100)
        val emailEditText = ObjectAnimator.ofFloat(binding.edRegisterEmail, View.ALPHA, 1f).setDuration(100)
        val passwordEditText = ObjectAnimator.ofFloat(binding.edRegisterPassword, View.ALPHA, 1f).setDuration(100)
        val registerButton = ObjectAnimator.ofFloat(binding.btnRegister, View.ALPHA, 1f).setDuration(100)
        val haveTextView = ObjectAnimator.ofFloat(binding.tvHave, View.ALPHA, 1f).setDuration(100)
        val loginTextView = ObjectAnimator.ofFloat(binding.tvLogin, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                welcomeTextView,
                appNameTextView,
                registerTextView,
                nameEditText,
                emailEditText,
                passwordEditText,
                registerButton,
                haveTextView,
                loginTextView
            )
            startDelay = 100
        }.start()
    }

}