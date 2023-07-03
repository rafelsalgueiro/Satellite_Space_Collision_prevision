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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import utilities.SSHConnection

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

            saveCredentials(ipAddress, masterPassword)

            CoroutineScope(Dispatchers.Main).launch {
                sshConnection = SSHConnection(ipAddress, masterPassword)
                val isConnected = sshConnection.connect()
                if (isConnected) {

                    val disconnectButton: Button = binding.disconnectButton
                    disconnectButton.setOnClickListener {
                        if (sshConnection.isConnected) {
                            sshConnection.disconnect()
                        }
                        updateStatusTextView("Disconnected")
                    }
                    // Connection established successfully
                    // Perform any operations you need on the SSH connection
                    updateStatusTextView("Connected")
                } else {
                    // Connection failed
                    updateStatusTextView("Disconnected")
                }
            }
        }

        binding.powerOffButton.setOnClickListener {
            if (sshConnection.isConnected) {
                println("in")
                val command = "lg-poweroff" // Command to power off all displays
                CoroutineScope(Dispatchers.Main).launch{
                    try {
                        val aaa = async {sshConnection.executeCommandAsync(command)}.await()
                        println("aaa")
                    } catch (e: Exception) {
                        println("Exception occurred: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        }

        binding.rebootButton.setOnClickListener {
            if (sshConnection.isConnected) {
                val command = "lg-reboot" // Command to reboot the LG
                CoroutineScope(Dispatchers.Main).launch {
                    sshConnection.executeCommand(command)
                }
            }
        }


        binding.returnMainPage.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
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
