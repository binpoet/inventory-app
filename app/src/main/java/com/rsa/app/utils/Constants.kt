package com.rsa.app.utils

/**
 * Application-wide constants
 */
object Constants {
    
    // API Configuration
    const val BASE_URL = "https://inventory.arrayapps.com/"
    
    // Network Configuration
    const val CONNECT_TIMEOUT = 30L // seconds
    const val READ_TIMEOUT = 30L // seconds
    const val WRITE_TIMEOUT = 30L // seconds
    
    // API Endpoints
    object Endpoints {
        const val LOGIN = "api/v1/login"
    }
    
    // SharedPreferences Keys
    object PrefKeys {
        const val AUTH_TOKEN = "auth_token"
        const val USER_ID = "user_id"
        const val IS_LOGGED_IN = "is_logged_in"
    }
    
    // Error Messages
    object ErrorMessages {
        const val NETWORK_ERROR = "Network error. Please check your internet connection."
        const val UNKNOWN_ERROR = "An unknown error occurred."
        const val TIMEOUT_ERROR = "Request timeout. Please try again."
    }
}
