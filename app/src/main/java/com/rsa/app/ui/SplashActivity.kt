package com.rsa.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.rsa.app.R
import com.rsa.app.databinding.ActivitySplashBinding
import com.rsa.app.utils.PreferenceManager

/**
 * Splash Screen Activity
 * Shows app logo and navigates to Login screen after delay
 */
class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    companion object {
        private const val SPLASH_DELAY: Long = 2000 // 2 seconds
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Navigate to appropriate screen after delay
        Handler(Looper.getMainLooper()).postDelayed({
            if (PreferenceManager.isLoggedIn()) {
                navigateToMain()
            } else {
                navigateToLogin()
            }
        }, SPLASH_DELAY)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close splash screen so user can't go back
    }
}
