package com.rsa.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Base API Response
 */
data class BaseResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

/**
 * Data models for Login API
 */

data class LoginResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: LoginData? = null,
    @SerializedName("errors") val errors: Map<String, List<String>>? = null
)

data class LoginData(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("user") val user: User
)

data class User(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("user_type") val userType: String
)
