package com.rsa.app.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton HTTP Client using Retrofit
 * Provides a single instance of Retrofit for the entire application
 */
object ApiClient {
    
    private const val BASE_URL = com.rsa.app.utils.Constants.BASE_URL
    
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            // Enable logging in debug mode
            // You can change this to Level.BODY for detailed logs
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
    
    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Get Retrofit instance
     */
    fun getRetrofit(): Retrofit = retrofitInstance
    
    /**
     * Create API service instance
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofitInstance.create(serviceClass)
    }
    
    /**
     * Create API service instance (reified version)
     */
    inline fun <reified T> createService(): T {
        return getRetrofit().create(T::class.java)
    }
}
