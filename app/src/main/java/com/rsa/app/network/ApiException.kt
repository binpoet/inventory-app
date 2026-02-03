package com.rsa.app.network

/**
 * Custom exception for API errors
 */
class ApiException(
    message: String,
    val code: Int = -1,
    val errorBody: String? = null,
    val throwable: Throwable? = null
) : Exception(message, throwable) {
    
    companion object {
        const val UNKNOWN_ERROR = -1
        const val NETWORK_ERROR = -2
        const val TIMEOUT_ERROR = -3
    }
    
    override fun toString(): String {
        return "ApiException(code=$code, message=$message, errorBody=$errorBody)"
    }
}
