import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.satellite_space_collision_prevision.R
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

        val powerOffButton = binding.powerOffButton
        val rebootButton = binding.rebootButton
        val clearKMLDataButton = binding.clearKMLDataButton

        powerOffButton.setOnClickListener {
            if (sshConnection.isConnected) {
                val command = "sudo lg-reboot -p" // Command to power off using lg-reboot with sudo
                sshConnection.executeCommand(command)
                // Handle the output if needed
            }
        }

        rebootButton.setOnClickListener {
            if (sshConnection.isConnected) {
                val command = "sudo lg-reboot -r" // Command to reboot using lg-reboot with sudo
                sshConnection.executeCommand(command)
                // Handle the output if needed
            }
        }


        clearKMLDataButton.setOnClickListener {
            // Perform action to clear KML data
            // You can call a function here to clear the KML data
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sshConnection.disconnect()
        updateStatusTextView("Disconnected")
    }

    private fun updateStatusTextView(status: String) {
        val statusText = getString(R.string.status_label, status)
        statusTextView.text = statusText
    }
}
