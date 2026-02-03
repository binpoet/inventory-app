package com.rsa.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.rsa.app.databinding.ActivityMainBinding
import com.rsa.app.databinding.DialogLogoutConfirmationBinding
import com.rsa.app.databinding.DialogUserInfoBinding
import com.rsa.app.ui.viewmodel.MainViewModel
import com.rsa.app.utils.PreferenceManager

/**
 * Main Activity - View layer in MVVM
 * Observes ViewModel's LiveData and updates UI accordingly
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupObservers()
        setupClickListeners()
    }
    
    private fun setupClickListeners() {
        binding.ivUser.setOnClickListener {
            showUserInfoDialog()
        }
        
        binding.ivLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }
    
    private fun setupObservers() {
        // Observe logout result
        viewModel.logoutResult.observe(this, Observer { result ->
            result.onSuccess {
                navigateToLogin()
            }.onFailure {
                Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show()
            }
        })
        
        // Observe loading state for logout (global loading might need to be specific if multiple things load)
        viewModel.isLoading.observe(this, Observer { isLoading ->
            // This is complex because the dialog is separate. 
            // Better to handle it directly in the dialog click listener for now or use a shared state.
        })
    }
    
    private fun showUserInfoDialog() {
        val user = PreferenceManager.getUser() ?: return
        
        val dialogBinding = DialogUserInfoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.tvFullName.text = user.fullName
        dialogBinding.tvEmail.text = user.email
        dialogBinding.tvUserType.text = user.userType
        
        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showLogoutConfirmationDialog() {
        val dialogBinding = DialogLogoutConfirmationBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()
        
        dialogBinding.btnLogoutConfirm.setOnClickListener {
            // Show loader in dialog
            dialogBinding.btnLogoutConfirm.text = ""
            dialogBinding.btnLogoutConfirm.isEnabled = false
            dialogBinding.btnLogoutCancel.isEnabled = false
            dialogBinding.logoutProgressBar.visibility = android.view.View.VISIBLE
            
            viewModel.logout()
        }
        
        dialogBinding.btnLogoutCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        // Also observe logout result to dismiss dialog if it was still showing (though navigateToLogin handles it)
        viewModel.logoutResult.observe(this, Observer {
            dialog.dismiss()
        })
        
        dialog.show()
    }
    
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    

}
