package com.rsa.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsa.app.data.model.LoginResponse
import com.rsa.app.network.ApiService
import com.rsa.app.network.ApiClient
import com.rsa.app.network.ApiException
import com.rsa.app.utils.NetworkUtils
import com.rsa.app.utils.PreferenceManager
import com.google.gson.Gson
import kotlinx.coroutines.launch

/**
 * ViewModel for Login screen
 * Handles login logic and validation
 */
class LoginViewModel : ViewModel() {
    
    private val apiService: ApiService by lazy {
        ApiClient.createService()
    }
    
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _loginResult = MutableLiveData<Result<LoginResponse>>()
    val loginResult: LiveData<Result<LoginResponse>> = _loginResult
    
    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError
    
    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError
    
    /**
     * Validate and perform login
     */
    fun login(email: String, password: String) {
        // Clear previous errors
        _emailError.value = null
        _passwordError.value = null
        
        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }
        
        // Perform login
        viewModelScope.launch {
            _isLoading.value = true
            val result = NetworkUtils.safeApiCall {
                apiService.login(email, password)
            }
            
            result.onSuccess { response ->
                if (response.success && response.data != null) {
                    // Save token and user info
                    PreferenceManager.saveAuthToken(response.data.accessToken)
                    PreferenceManager.saveUser(response.data.user)
                    PreferenceManager.setLoggedIn(true)
                    _loginResult.value = result
                } else {
                    // This case handles success=false in the 200 response body if any
                    _loginResult.value = Result.failure(Exception(response.message))
                }
            }.onFailure { exception ->
                if (exception is ApiException && exception.errorBody != null) {
                    try {
                        val errorResponse = Gson().fromJson(exception.errorBody, LoginResponse::class.java)
                        handleErrorResponse(errorResponse)
                    } catch (e: Exception) {
                        _loginResult.value = result
                    }
                } else {
                    _loginResult.value = result
                }
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Handle error response from API
     */
    private fun handleErrorResponse(errorResponse: LoginResponse) {
        val errors = errorResponse.errors
        if (errors != null) {
            errors["email"]?.firstOrNull()?.let {
                _emailError.value = it
            }
            errors["password"]?.firstOrNull()?.let {
                _passwordError.value = it
            }
        }
        _loginResult.value = Result.failure(Exception(errorResponse.message))
    }
    
    /**
     * Validate email and password inputs
     */
    private fun validateInputs(email: String, password: String): Boolean {
        var isValid = true
        
        // Validate email
        if (email.isEmpty()) {
            _emailError.value = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailError.value = "Please enter a valid email"
            isValid = false
        }
        
        // Validate password
        if (password.isEmpty()) {
            _passwordError.value = "Password is required"
            isValid = false
        }
        
        return isValid
    }
}
