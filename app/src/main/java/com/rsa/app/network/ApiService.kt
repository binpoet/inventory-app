package com.rsa.app.network

import com.rsa.app.data.model.BaseResponse
import com.rsa.app.data.model.LoginResponse
import com.rsa.app.utils.Constants
import retrofit2.Response
import retrofit2.http.*

/**
 * API Endpoint interface
 */
interface ApiService {
    
    /**
     * Login request
     */
    @FormUrlEncoded
    @POST(Constants.Endpoints.LOGIN)
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    /**
     * Logout request
     */
    @POST("api/v1/logout")
    suspend fun logout(): Response<BaseResponse>
}

