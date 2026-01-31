package com.rsa.app.utils

import android.content.Context
import android.content.SharedPreferences
import com.rsa.app.App
import com.rsa.app.data.model.User

/**
 * Singleton Preference Manager for storing key-value pairs
 * Handles authentication tokens, user preferences, etc.
 */
object PreferenceManager {
    
    private const val PREF_NAME = "RsaPrefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_FIRST_NAME = "user_first_name"
    private const val KEY_USER_LAST_NAME = "user_last_name"
    private const val KEY_USER_FULL_NAME = "user_full_name"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    private val prefs: SharedPreferences by lazy {
        App.instance.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Save authentication token
     */
    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }
    
    /**
     * Get authentication token
     */
    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }
    
    /**
     * Clear authentication token
     */
    fun clearAuthToken() {
        prefs.edit().remove(KEY_AUTH_TOKEN).apply()
    }
    
    /**
     * Save user info
     */
    fun saveUser(user: User) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_FIRST_NAME, user.firstName)
            putString(KEY_USER_LAST_NAME, user.lastName)
            putString(KEY_USER_FULL_NAME, user.fullName)
            putString(KEY_USER_TYPE, user.userType)
        }.apply()
    }
    
    /**
     * Get user info
     */
    fun getUser(): User? {
        val id = prefs.getInt(KEY_USER_ID, -1)
        if (id == -1) return null
        
        return User(
            id = id,
            email = prefs.getString(KEY_USER_EMAIL, "") ?: "",
            firstName = prefs.getString(KEY_USER_FIRST_NAME, "") ?: "",
            lastName = prefs.getString(KEY_USER_LAST_NAME, "") ?: "",
            fullName = prefs.getString(KEY_USER_FULL_NAME, "") ?: "",
            userType = prefs.getString(KEY_USER_TYPE, "") ?: ""
        )
    }
    
    /**
     * Get user ID
     */
    fun getUserId(): Int {
        return prefs.getInt(KEY_USER_ID, -1)
    }
    
    /**
     * Set login status
     */
    fun setLoggedIn(isLoggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    
    /**
     * Clear all preferences (logout)
     */
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    /**
     * Generic method to save string
     */
    fun saveString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    /**
     * Generic method to get string
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return prefs.getString(key, defaultValue)
    }
    
    /**
     * Generic method to save boolean
     */
    fun saveBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
    
    /**
     * Generic method to get boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    /**
     * Generic method to save int
     */
    fun saveInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    /**
     * Generic method to get int
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }
}
