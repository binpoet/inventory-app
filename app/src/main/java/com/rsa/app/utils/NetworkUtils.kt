package com.rsa.app.utils

import com.rsa.app.network.ApiException
import retrofit2.Response

/**
 * Utility functions for network operations
 */
object NetworkUtils {
    
    /**
     * Execute a Retrofit call and handle the response
     * @param apiCall The suspend function that makes the API call
     * @return Result containing either success data or error
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(ApiException("Response body is null", response.code()))
                }
            } else {
                Result.failure(
                    ApiException(
                        "Error: ${response.code()} ${response.message()}",
                        response.code(),
                        response.errorBody()?.string()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                ApiException(
                    "Network error: ${e.message}",
                    -1,
                    null,
                    e
                )
            )
        }
    }
    
    /**
     * Check if device has internet connection
     * Note: This requires INTERNET permission in AndroidManifest
     */
    fun isNetworkAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ping -c 1 google.com")
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Parse error message from response
     */
    fun parseErrorMessage(errorBody: String?): String {
        return errorBody ?: "Unknown error occurred"
    }
}
