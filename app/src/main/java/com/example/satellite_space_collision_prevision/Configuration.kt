package com.example.satellite_space_collision_prevision

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.satellite_space_collision_prevision.databinding.ConfigurationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utilities.SSHConnection

class Configuration : AppCompatActivity() {
    private val binding by lazy { ConfigurationBinding.inflate(layoutInflater) }
    private lateinit var ipAddressEditText: EditText
    private lateinit var masterPasswordEditText: EditText
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var sshConnection: SSHConnection
    private lateinit var disconnectButton: Button
    private lateinit var cleanKMLDataButton: Button
    private lateinit var showLogos: Button
    private lateinit var hideLogos: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize views
        ipAddressEditText = binding.IPAddress
        masterPasswordEditText = binding.MasterPassword
        connectButton = binding.ConnectButton
        statusTextView = binding.Status
        disconnectButton = binding.disconnectButton
        cleanKMLDataButton = binding.clearKMLDataButton
        showLogos = binding.showLogo
        hideLogos = binding.hideLogos

        // Set click listeners for buttons
        connectButton.setOnClickListener { onConnectButtonClicked() }
        disconnectButton.setOnClickListener { onDisconnectButtonClicked() }
        binding.powerOffButton.setOnClickListener { onPowerOffButtonClicked() }
        binding.rebootButton.setOnClickListener { onRebootButtonClicked() }
        binding.returnMainPage.setOnClickListener { onReturnMainPageClicked() }
        cleanKMLDataButton.setOnClickListener { onCleanKMLDataButtonClicked() }
        showLogos.setOnClickListener { onShowLogosClicked() }
        hideLogos.setOnClickListener { onHideLogosClicked() }
    }

    private fun onHideLogosClicked() {
        if (sshConnection.isConnected) {
            sshConnection.hideLogos()
        }
    }

    private fun onConnectButtonClicked() {
        val ipAddress = ipAddressEditText.text.toString()
        val masterPassword = masterPasswordEditText.text.toString()
        val numSlaves = binding.numberSlaves.text.toString().toInt()

        saveCredentials(ipAddress, masterPassword, numSlaves)

        CoroutineScope(Dispatchers.Main).launch {
            sshConnection = SSHConnection(ipAddress, masterPassword, numSlaves)
            val isConnected = sshConnection.connect()
            if (isConnected) {
                // Connection established successfully
                // Perform any operations you need on the SSH connection
                updateStatusTextView("Connected")
            } else {
                // Connection failed
                updateStatusTextView("Disconnected")
            }
        }
    }

    private fun onShowLogosClicked() {
        if (sshConnection.isConnected) {
            sshConnection.displayLogos()
        }
    }

    private fun onDisconnectButtonClicked() {
        sshConnection.disconnect()
        updateStatusTextView("Disconnected")
    }

    private fun onPowerOffButtonClicked() {
        sshConnection.poweroffButton()
    }

    private fun onRebootButtonClicked() {
        sshConnection.rebootButton()
    }


    private fun onReturnMainPageClicked() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun onCleanKMLDataButtonClicked() {
        if (sshConnection.isConnected) {
            val command = "chmod 777 /var/www/html/kmls.txt; echo '' > /var/www/html/kmls.txt"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    sshConnection.executeCommand(command)

                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }

    private fun saveCredentials(ipAddress: String, masterPassword: String, numSlaves: Int) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ipAddress", ipAddress)
        editor.putString("masterPassword", masterPassword)
        editor.putInt("numSlaves", numSlaves)
        editor.apply()
    }


    private fun updateStatusTextView(status: String) {
        val statusText = getString(R.string.status_label, status)
        statusTextView.text = statusText
    }
}
