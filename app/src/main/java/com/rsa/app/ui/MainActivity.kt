package com.rsa.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.rsa.app.R
import com.rsa.app.databinding.ActivityMainBinding
import com.rsa.app.databinding.DialogLogoutConfirmationBinding
import com.rsa.app.databinding.DialogUserInfoBinding
import com.rsa.app.rfid.RfidManager
import com.rsa.app.ui.viewmodel.MainViewModel
import com.rsa.app.utils.PreferenceManager
import java.util.concurrent.Executors

/**
 * Main home screen: user info, logout, and RFID scan on the same page.
 * Scan animation + Start Scan button. If reader unavailable shows error; if tag read, shows EPC value.
 */
class MainActivity : AppCompatActivity(), RfidManager.Callback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private var rfidManager: RfidManager? = null
    private val rfidExecutor = Executors.newSingleThreadExecutor()

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grantResults ->
        if (grantResults.values.all { it }) {
            rfidExecutor.execute { rfidManager?.connect() }
        } else {
            showRfidError(getString(R.string.rfid_reader_not_available))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        rfidManager = RfidManager(this, this).also { it.init() }
        binding.btnRfidStartScan.isEnabled = false
        requestRfidPermissionsAndConnect()

        setupObservers()
        setupClickListeners()
    }

    private fun requestRfidPermissionsAndConnect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val needed = mutableListOf<String>()
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.BLUETOOTH_CONNECT)
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                needed.add(Manifest.permission.ACCESS_FINE_LOCATION)
            if (needed.isNotEmpty()) {
                requestPermissions.launch(needed.toTypedArray())
            } else {
                rfidExecutor.execute { rfidManager?.connect() }
            }
        } else {
            rfidExecutor.execute { rfidManager?.connect() }
        }
    }

    private fun setupClickListeners() {
        binding.ivUser.setOnClickListener { showUserInfoDialog() }
        binding.ivLogout.setOnClickListener { showLogoutConfirmationDialog() }
        binding.btnRfidStartScan.setOnClickListener {
            if (rfidManager?.isConnected() == true) {
                binding.progressRfidScan.visibility = View.VISIBLE
                hideRfidError()
                rfidManager?.startInventory()
            } else {
                showRfidError(getString(R.string.rfid_reader_not_available))
            }
        }
    }

    private fun setupObservers() {
        viewModel.logoutResult.observe(this, Observer { result ->
            result.onSuccess { logoutDialog?.dismiss(); navigateToLogin() }.onFailure {
                Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        rfidManager?.stopInventory()
        binding.progressRfidScan.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        rfidManager?.disconnect()
        rfidManager = null
        rfidExecutor.shutdown()
    }

    // ——— RfidManager.Callback ———
    override fun onConnected() {
        runOnUiThread {
            hideRfidError()
            binding.btnRfidStartScan.isEnabled = true
        }
    }

    override fun onDisconnected() {
        runOnUiThread {
            binding.btnRfidStartScan.isEnabled = false
            showRfidError(getString(R.string.rfid_reader_not_available))
        }
    }

    override fun onConnectionError(message: String) {
        runOnUiThread {
            binding.btnRfidStartScan.isEnabled = false
            showRfidError(message)
        }
    }

    override fun onEpcRead(epcHexString: String) {
        runOnUiThread {
            binding.tvRfidEpcValue.text = epcHexString
            binding.tvRfidEpcValue.visibility = View.VISIBLE
            binding.tvRfidError.visibility = View.GONE
        }
    }

    override fun onInventoryStarted() {}
    override fun onInventoryStopped() {
        runOnUiThread { binding.progressRfidScan.visibility = View.GONE }
    }
    override fun onError(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private fun showRfidError(message: String) {
        binding.tvRfidError.text = message
        binding.tvRfidError.visibility = View.VISIBLE
        binding.tvRfidEpcValue.visibility = View.GONE
    }

    private fun hideRfidError() {
        binding.tvRfidError.visibility = View.GONE
    }

    private fun showUserInfoDialog() {
        val user = PreferenceManager.getUser() ?: return
        val dialogBinding = DialogUserInfoBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()
        dialogBinding.tvFullName.text = user.fullName
        dialogBinding.tvEmail.text = user.email
        dialogBinding.tvUserType.text = user.userType
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private var logoutDialog: AlertDialog? = null
    private fun showLogoutConfirmationDialog() {
        val dialogBinding = DialogLogoutConfirmationBinding.inflate(layoutInflater)
        logoutDialog = AlertDialog.Builder(this).setView(dialogBinding.root).setCancelable(true).create()
        dialogBinding.btnLogoutConfirm.setOnClickListener {
            dialogBinding.btnLogoutConfirm.text = ""
            dialogBinding.btnLogoutConfirm.isEnabled = false
            dialogBinding.btnLogoutCancel.isEnabled = false
            dialogBinding.logoutProgressBar.visibility = View.VISIBLE
            viewModel.logout()
        }
        dialogBinding.btnLogoutCancel.setOnClickListener { logoutDialog?.dismiss() }
        logoutDialog?.show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
