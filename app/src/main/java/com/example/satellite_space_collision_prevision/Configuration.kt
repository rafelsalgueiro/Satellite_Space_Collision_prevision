package com.example.satellite_space_collision_prevision

import SSHConnection
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.satellite_space_collision_prevision.databinding.ConfigurationBinding

class Configuration : AppCompatActivity() {
    private val binding by lazy { ConfigurationBinding.inflate(layoutInflater) }
    private lateinit var ipAddressEditText: EditText
    private lateinit var masterPasswordEditText: EditText
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView
    private lateinit var sshConnection: SSHConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        ipAddressEditText = binding.IPAddress
        masterPasswordEditText = binding.MasterPassword
        connectButton = binding.ConnectButton
        statusTextView = binding.Status

        connectButton.setOnClickListener {
            val ipAddress = ipAddressEditText.text.toString()
            val masterPassword = masterPasswordEditText.text.toString()

            sshConnection = SSHConnection(ipAddress, masterPassword)

            if (sshConnection.connect()) {
                // Connection established successfully
                // Perform any operations you need on the SSH connection
                updateStatusTextView("Connected")
            } else {
                // Connection failed
                updateStatusTextView("Disconnected")
            }
        }

        binding.powerOffButton.setOnClickListener {
            if (sshConnection.isConnected) {
                val command = "lg-poweroff" // Command to power off
                sshConnection.executeCommand(command)
            }
        }

        binding.rebootButton.setOnClickListener {
            if (sshConnection.isConnected) {
                val command = "lg-reboot" // Command to reboot the LG
                sshConnection.executeCommand(command)
            }
        }

        val disconnectButton: Button = binding.disconnectButton
        disconnectButton.setOnClickListener {
            if (sshConnection.isConnected) {
                sshConnection.disconnect()
            }
            updateStatusTextView("Disconnected")
        }
        binding.returnMainPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateStatusTextView(status: String) {
        val statusText = getString(R.string.status_label, status)
        statusTextView.text = statusText
    }
}
