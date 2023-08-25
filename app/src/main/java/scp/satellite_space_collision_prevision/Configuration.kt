@file:Suppress("DEPRECATION")

package scp.satellite_space_collision_prevision

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.satellite_space_collision_prevision.R
import com.satellite_space_collision_prevision.databinding.ConfigurationBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utilities.SSHConnection
import utilities.callToServer.connection
import utilities.callToServer.setter
import java.io.IOException
import java.net.UnknownHostException


class Configuration : AppCompatActivity(), MainActivityObserver {
    private val binding by lazy { ConfigurationBinding.inflate(layoutInflater) }
    private lateinit var ipAddressEditText: EditText
    private lateinit var masterPasswordEditText: EditText
    private lateinit var connectButton: Button
    private lateinit var statusTextView: TextView
    lateinit var sshConnection: SSHConnection
    private lateinit var disconnectButton: Button
    private lateinit var cleanKMLDataButton: Button
    private lateinit var showLogos: Button
    private lateinit var hideLogos: Button
    private lateinit var numSlaves: EditText
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var ipserver: EditText
    private lateinit var relaunch: Button
    private lateinit var collisionPort: EditText
    private lateinit var coordinatesPort: EditText
    private lateinit var connectServerButton: Button
    private lateinit var disconnectServerButton: Button
    private lateinit var serverStatus: TextView
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        var serverConnection = false
        var lgConnection = false
        var firstConnection = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)


        val sharedPreferencies = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        editor = sharedPreferencies.edit()
        // Initialize views
        ipAddressEditText = binding.IPAddress
        masterPasswordEditText = binding.MasterPassword
        connectButton = binding.ConnectButton
        statusTextView = binding.Status
        disconnectButton = binding.disconnectButton
        cleanKMLDataButton = binding.clearKMLDataButton
        showLogos = binding.showLogo
        hideLogos = binding.hideLogos
        numSlaves = binding.numberSlaves
        ipserver = binding.IPAddressServer
        relaunch = binding.relaunchButton
        connectServerButton = binding.ConnectServerButton
        disconnectServerButton = binding.DisconnectServerButton
        serverStatus = binding.statusServer
        collisionPort = binding.detectionPort
        coordinatesPort = binding.orbitsPort

        if (sharedPreferencies.contains("ipAddress")) {
            ipAddressEditText.setText(sharedPreferencies.getString("ipAddress", ""))
            masterPasswordEditText.setText(sharedPreferencies.getString("masterPassword", ""))
            numSlaves.setText(sharedPreferencies.getInt("numSlaves", 0).toString())
            statusTextView.text = sharedPreferencies.getString("status", "Disconnected")
            serverStatus.text = sharedPreferencies.getString("status", "Disconnected")
            ipserver.setText(sharedPreferencies.getString("ipAddressServer", ""))
            collisionPort.setText(sharedPreferencies.getString("collisionPort", ""))
            coordinatesPort.setText(sharedPreferencies.getString("coordinatesPort", ""))
        } else {
            ipAddressEditText.text = null
            masterPasswordEditText.text = null
            numSlaves.text = null
            ipserver.text = null
            collisionPort.text = null
            coordinatesPort.text = null
            statusTextView.text = "Status: Disconnected"
            serverStatus.text = "Status: Disconnected"
        }

        // Set click listeners for buttons
        disconnectButton.setOnClickListener { onDisconnectButtonClicked() }
        binding.powerOffButton.setOnClickListener { onPowerOffButtonClicked() }
        binding.rebootButton.setOnClickListener { onRebootButtonClicked() }
        binding.returnMainPage.setOnClickListener { onReturnMainPageClicked() }
        cleanKMLDataButton.setOnClickListener { onCleanKMLDataButtonClicked() }
        showLogos.setOnClickListener { onShowLogosClicked() }
        hideLogos.setOnClickListener { onHideLogosClicked() }
        connectButton.setOnClickListener { onConnectButtonClicked() }
        relaunch.setOnClickListener { onRelaunchButtonClicked() }

        connectServerButton.setOnClickListener { onConnectServerButtonClicked() }
        disconnectServerButton.setOnClickListener { onDisconnectServerButtonClicked() }

        disconnected()
        handler.postDelayed({}, 500)

    }

    override fun onResume() {
        super.onResume()
        if (firstConnection || ipAddressEditText.text.toString().isBlank()) {
            firstConnection = false
            return
        }
        PingAsyncTask().execute(ipAddressEditText.text.toString())
        PingAsyncTask().execute(ipAddressEditText.text.toString())
        ConnectionAsyncTask().execute()

    }

    private inner class PingAsyncTask : AsyncTask<String, Void, Boolean>() {
        override fun doInBackground(vararg params: String): Boolean {
            return pingHost(params[0])
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                connectButton.performClick()
            }
        }
    }

    private inner class ConnectionAsyncTask : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void): Boolean {
            return connection()
        }

        override fun onPostExecute(result: Boolean) {
            if (result) {
                connectServerButton.performClick()
            }
        }
    }

    private fun disconnected() {
        val redColorSpan = ForegroundColorSpan(Color.RED)

        val serverStatusText = SpannableString("Status: Disconnected")
        serverStatusText.setSpan(
            redColorSpan,
            8,
            serverStatusText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val statusText = SpannableString("Status: Disconnected")
        statusText.setSpan(
            redColorSpan,
            8,
            statusText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        serverStatus.text = serverStatusText
        statusTextView.text = statusText
    }

    private fun onDisconnectServerButtonClicked() {
        val redColorSpan = ForegroundColorSpan(Color.RED)
        val serverStatusText = SpannableString("Status: Disconnected")
        serverStatusText.setSpan(
            redColorSpan,
            8,
            serverStatusText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        serverStatus.text = serverStatusText
        serverConnection = false
    }

    private fun onConnectServerButtonClicked() {
        val serverIP = ipserver.text.toString()
        val collisionPort = collisionPort.text.toString()
        val orbitsPort = coordinatesPort.text.toString()

        setter(serverIP, collisionPort, orbitsPort)
        editor.apply {
            putString("ipAddressServer", serverIP)
            putString("collisionPort", collisionPort)
            putString("coordinatesPort", orbitsPort)
            apply()
        }

        val isConnected = pingHost(serverIP)
        val statusPrefix = "Status: "


        if (isConnected) {
            serverConnection = true
        }
        val statusText = if (isConnected) "Connected" else "Disconnected"
        val coloredStatusText = SpannableString(statusPrefix + statusText)

        val textColor = if (isConnected) Color.GREEN else Color.RED
        val colorSpan = ForegroundColorSpan(textColor)
        coloredStatusText.setSpan(
            colorSpan,
            statusPrefix.length,
            coloredStatusText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        serverStatus.text = coloredStatusText
    }


    fun pingHost(host: String): Boolean {
        val runtime = Runtime.getRuntime()
        try {
            val ipProcess = runtime.exec("/system/bin/ping -c 1 $host")
            val exitValue = ipProcess.waitFor()
            ipProcess.destroy()
            return exitValue == 0
        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return false
    }

    private fun onRelaunchButtonClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.relaunch()
        }
    }

    private fun onConnectButtonClicked() {
        val ipAddress = ipAddressEditText.text.toString()
        val masterPassword = masterPasswordEditText.text.toString()

        if (ipAddress.isBlank() || !pingHost(ipAddress)) {
            updateStatusTextView("Disconnected", Color.RED)
            return
        }

        val numSlaves = numSlaves.text.toString().toInt()
        saveCredentials(ipAddress, masterPassword, numSlaves)

        CoroutineScope(Dispatchers.Main).launch {
            sshConnection = SSHConnection(ipAddress, masterPassword, numSlaves)
            val isConnected = sshConnection.connect()
            if (isConnected) {
                lgConnection = true
            }

            val statusText = if (isConnected) "Connected" else "Disconnected"
            val textColor = if (isConnected) Color.GREEN else Color.RED

            updateStatusTextView(statusText, textColor)

            editor.apply {
                putString("ipAddress", ipAddress)
                putString("masterPassword", masterPassword)
                putInt("numSlaves", numSlaves)
                putString("status", statusText)
                apply()
            }
        }
    }

    private fun updateStatusTextView(status: String, textColor: Int?) {
        val fullStatusText = if (textColor != null) "Status: $status" else status
        statusTextView.text = fullStatusText

        if (textColor != null) {
            val coloredStatusText = SpannableString(fullStatusText)
            val colorSpan = ForegroundColorSpan(textColor)
            coloredStatusText.setSpan(
                colorSpan,
                8,
                coloredStatusText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            statusTextView.text = coloredStatusText
        }
    }


    private fun onHideLogosClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.hideLogos()
        }
    }


    private fun onShowLogosClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.displayLogos()
        }
    }

    private fun onDisconnectButtonClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.disconnect()
        }
        lgConnection = false
        updateStatusTextView("Disconnected", Color.RED)
    }

    private fun onPowerOffButtonClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.poweroffButton()
        }
    }

    private fun onRebootButtonClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.rebootButton()
        }
    }


    private fun onReturnMainPageClicked() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun onCleanKMLDataButtonClicked() {
        if (SSHConnection.isConnected()) {
            sshConnection.cleanKml()
        }
    }

    private fun saveCredentials(ipAddress: String, masterPassword: String, numSlaves: Int) {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("ipAddress", ipAddress)
        editor.putString("masterPassword", masterPassword)
        editor.putInt("numSlaves", numSlaves)
        editor.putString("ipserver", ipserver.text.toString())
        editor.putString("collisionPort", collisionPort.text.toString())
        editor.putString("coordinatesPort", coordinatesPort.text.toString())
        editor.apply()
    }


    fun updateStatusTextView(status: String) {
        val statusText = getString(R.string.status_label, status)
        statusTextView.text = statusText
    }

    override fun onDestroy() {
        updateStatusTextView("Disconnected")
        if (SSHConnection.isConnected()) {
            sshConnection.disconnect()
        }
        editor.apply {
            putString("ipAddress", ipAddressEditText.text.toString())
            putString("masterPassword", masterPasswordEditText.text.toString())
            putInt("numSlaves", numSlaves.text.toString().toInt())
            putString("status", statusTextView.text.toString())
            putString("ipAddressServer", ipserver.text.toString())
            putString("collisionPort", collisionPort.text.toString())
            putString("coordinatesPort", coordinatesPort.text.toString())
            apply()
        }
        super.onDestroy()
    }

    override fun onMainActivityDestroyed() {
        updateStatusTextView("Disconnected")
        if (SSHConnection.isConnected()) {
            sshConnection.disconnect()
        }
    }


}

interface MainActivityObserver {
    fun onMainActivityDestroyed()
}