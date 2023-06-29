package com.example.satellite_space_collision_prevision


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.satellite_space_collision_prevision.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.configurationButton.setOnClickListener {
            val intent = Intent(this, Configuration::class.java)
            startActivity(intent)
        }
    }
}