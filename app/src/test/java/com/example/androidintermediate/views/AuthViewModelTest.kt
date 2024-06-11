package com.example.androidintermediate.views

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.androidintermediate.MockLog
import com.example.androidintermediate.R
import com.example.androidintermediate.data.model.LoginResponse
import com.example.androidintermediate.data.model.LoginResult
import com.example.androidintermediate.data.model.RegisterResponse
import com.example.androidintermediate.data.remote.ApiConfig
import com.example.androidintermediate.data.remote.ApiService
import com.example.androidintermediate.views.viewmodel.AuthViewModel
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuthViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    private lateinit var apiService: ApiService

    private lateinit var loginObserver: Observer<LoginResponse>

    private lateinit var registerObserver: Observer<RegisterResponse>

    private lateinit var errorObserver: Observer<String>

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        MockLog

        context = mockk(relaxed = true)
        apiService = mockk()

        loginObserver = mockk(relaxed = true)
        registerObserver = mockk(relaxed = true)
        errorObserver = mockk(relaxed = true)

        every { context.getString(R.string.API_error_email_invalid) } returns "Invalid email"
        every { context.getString(R.string.API_error_unauthorized) } returns "Unauthorized"

        viewModel = AuthViewModel(context)
        viewModel.loginResult.observeForever(loginObserver)
        viewModel.registerResult.observeForever(registerObserver)
        viewModel.error.observeForever(errorObserver)

        mockkObject(ApiConfig)
        every { ApiConfig.getApiService() } returns apiService
    }

    @After
    fun tearDown() {
        viewModel.loginResult.removeObserver(loginObserver)
        viewModel.registerResult.removeObserver(registerObserver)
        viewModel.error.removeObserver(errorObserver)
        unmockkAll()
    }

    @Test
    fun login_success() {
        val email = "test@example.com"
        val password = "password"
        val loginResult = LoginResult(
            name = "mockUserName",
            userId = "mockUserId123",
            token = "mockToken123"
        )
        val response = LoginResponse(
            loginResult = loginResult,
            error = false,
            message = "success"
        )
        val call = mockk<Call<LoginResponse>>()

        every { apiService.doLogin(email, password) } returns call

        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback<LoginResponse>>()
            callback.onResponse(call, Response.success(response))
        }

        viewModel.login(email, password)

        verify { loginObserver.onChanged(response) }
        verify(exactly = 1) { errorObserver.onChanged(any()) }
    }

    @Test
    fun login_failure() {
        val email = "test@example.com"
        val password = "password"
        val errorMessage = "Login failed"
        val call = mockk<Call<LoginResponse>>()

        every { apiService.doLogin(email, password) } returns call

        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback<LoginResponse>>()
            callback.onFailure(call, Throwable(errorMessage))
        }

        viewModel.login(email, password)

        verify { errorObserver.onChanged(errorMessage) }
        verify(exactly = 0) { loginObserver.onChanged(any()) }
    }

    @Test
    fun register_failure() {
        val name = "Test User"
        val email = "test@example.com"
        val password = "password"
        val errorMessage = "Registration failed"
        val call = mockk<Call<RegisterResponse>>()

        every { apiService.doRegister(name, email, password) } returns call

        every { call.enqueue(any()) } answers {
            val callback = firstArg<Callback<RegisterResponse>>()
            callback.onFailure(call, Throwable(errorMessage))
        }

        viewModel.register(name, email, password)

        verify { errorObserver.onChanged(errorMessage) }
        verify(exactly = 0) { registerObserver.onChanged(any()) }
    }
}