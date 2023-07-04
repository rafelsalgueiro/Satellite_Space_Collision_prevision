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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize views
        ipAddressEditText = binding.IPAddress
        masterPasswordEditText = binding.MasterPassword
        connectButton = binding.ConnectButton
        statusTextView = binding.Status
        disconnectButton = binding.disconnectButton

        // Set click listeners for buttons
        connectButton.setOnClickListener { onConnectButtonClicked() }
        disconnectButton.setOnClickListener { onDisconnectButtonClicked() }
        binding.powerOffButton.setOnClickListener { onPowerOffButtonClicked() }
        binding.rebootButton.setOnClickListener { onRebootButtonClicked() }
        binding.returnMainPage.setOnClickListener { onReturnMainPageClicked() }
    }

    private fun onConnectButtonClicked() {
        val ipAddress = ipAddressEditText.text.toString()
        val masterPassword = masterPasswordEditText.text.toString()

        saveCredentials(ipAddress, masterPassword)

        CoroutineScope(Dispatchers.Main).launch {
            sshConnection = SSHConnection(ipAddress, masterPassword)
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

    private fun onDisconnectButtonClicked() {
        sshConnection.disconnect()
        updateStatusTextView("Disconnected")
    }

    private fun onPowerOffButtonClicked() {
        if (sshConnection.isConnected) {
            val command = "/home/lg/bin/lg-reboot > /home/lg/log.txt"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    sshConnection.executeCommand(command)
                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                    e.printStackTrace()
                    updateStatusTextView("Disconnected")
                }
            }
        }
    }

    private fun onRebootButtonClicked() {
        if (sshConnection.isConnected) {
            val command = """lg-reboot""" // Command to reboot the LG
            val scriptPath = "/home/lg/reboot_script.sh"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Write command to script file
                    var exitStatus = sshConnection.executeCommand("echo \"$command\" > $scriptPath")
                    println("Exit status: $exitStatus")
                    // Set execute permissions for the script
                    exitStatus = sshConnection.executeCommand("chmod +x $scriptPath")
                    println("Exit status: $exitStatus")
                    // Execute the script
                    exitStatus = sshConnection.executeCommand("bash $scriptPath; echo \$?")

                    // Print the exit status
                    println("Exit status: $exitStatus")
                } catch (e: Exception) {
                    println("Exception occurred: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }



    private fun onReturnMainPageClicked() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun saveCredentials(ipAddress: String, masterPassword: String) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ipAddress", ipAddress)
        editor.putString("masterPassword", masterPassword)
        editor.apply()
    }


    private fun updateStatusTextView(status: String) {
        val statusText = getString(R.string.status_label, status)
        statusTextView.text = statusText
    }
}
