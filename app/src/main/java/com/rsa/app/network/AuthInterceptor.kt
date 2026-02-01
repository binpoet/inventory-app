package com.rsa.app.network

import okhttp3.Interceptor
import okhttp3.Response
import com.rsa.app.utils.PreferenceManager

/**
 * Interceptor to add authentication headers to requests
 */
class AuthInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token from preference manager (if available)
        val token = PreferenceManager.getAuthToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build()
        }
        
        return chain.proceed(newRequest)
    }
}
