package com.rsa.app.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsa.app.network.ApiClient
import com.rsa.app.network.ApiService
import com.rsa.app.utils.PreferenceManager
import kotlinx.coroutines.launch

/**
 * ViewModel - manages UI-related data and business logic
 * Survives configuration changes
 */
class MainViewModel : ViewModel() {
    
    private val apiService: ApiService by lazy {
        ApiClient.createService()
    }
    
    private val _logoutResult = MutableLiveData<Result<Unit>>()
    val logoutResult: LiveData<Result<Unit>> = _logoutResult
    
    private val _isLoading = MutableLiveData<Boolean>().apply { value = false }
    val isLoading: LiveData<Boolean> = _isLoading

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Call API (ignore result for simplicity as we clear locally anyway)
                apiService.logout()
                PreferenceManager.clearAll()
                _logoutResult.value = Result.success(Unit)
            } catch (e: Exception) {
                // Even if API fails, we should probably clear local data for safety
                PreferenceManager.clearAll()
                _logoutResult.value = Result.success(Unit)
            }
            _isLoading.value = false
        }
    }
}
