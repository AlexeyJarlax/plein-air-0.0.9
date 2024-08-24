package com.pavlovalexey.pleinair

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pavlovalexey.pleinair.presentation.AuthActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Launch AuthActivity
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish() // Optional: close MainActivity if you don't need it anymore
    }
}