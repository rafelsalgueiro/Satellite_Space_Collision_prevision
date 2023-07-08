package com.example.satellite_space_collision_prevision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, MainActivity::class.java))
    }
}