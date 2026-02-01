package com.rsa.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.rsa.app.databinding.ActivityLoginBinding
import com.rsa.app.ui.viewmodel.LoginViewModel

/**
 * Login Activity - Professional login screen
 */
class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupObservers() {
        // Observe loading state
        viewModel.isLoading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnLogin.isEnabled = !isLoading
            binding.btnLogin.text = if (isLoading) "" else getString(com.rsa.app.R.string.login)
            binding.etEmail.isEnabled = !isLoading
            binding.etPassword.isEnabled = !isLoading
        })
        
        // Observe login result
        viewModel.loginResult.observe(this, Observer { result ->
            result.onSuccess { response ->
                // Navigate to MainActivity on successful login
                if (response.success) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }.onFailure { exception ->
                // Show error message
                Toast.makeText(
                    this,
                    exception.message ?: "Login failed. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
        
        // Observe validation errors
        viewModel.emailError.observe(this, Observer { error ->
            binding.tilEmail.error = error
        })
        
        viewModel.passwordError.observe(this, Observer { error ->
            binding.tilPassword.error = error
        })
    }
    
    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            
            viewModel.login(email, password)
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
