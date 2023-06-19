package com.example.satellite_space_collision_prevision

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.satellite_space_collision_prevision.databinding.ConfigurationBinding
class Configuration : AppCompatActivity(){
    private val binding by lazy { ConfigurationBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}