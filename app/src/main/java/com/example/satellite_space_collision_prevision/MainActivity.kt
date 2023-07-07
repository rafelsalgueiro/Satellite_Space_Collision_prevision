package com.example.satellite_space_collision_prevision


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.satellite_space_collision_prevision.databinding.ActivityMainBinding
import utilities.SSHConnection

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var configurationButton: ImageButton
    private lateinit var checkCollisionButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        configurationButton = binding.configurationButton
        checkCollisionButton = binding.checkCollisionButton

        configurationButton.setOnClickListener { onConfigurationButtonClicked() }
        checkCollisionButton.setOnClickListener { checkCollisionButtonClicked() }
    }

    private fun onConfigurationButtonClicked() {
        val intent = Intent(this, Configuration::class.java)
        startActivity(intent)
    }

    private fun checkCollisionButtonClicked() {
        if (SSHConnection.isConnected()) {
            SSHConnection.testingPrintingSats()
        }

    }


}