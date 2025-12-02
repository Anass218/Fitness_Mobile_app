package com.example.fitness_kzo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitness_kzo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the toolbar with back button
        setupToolbar()

        // Display welcome message with user's name
        displayWelcomeMessage()

        // Set up click listeners for activity buttons
        setupActivityButtons()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)

        // Enable back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Set back button click listener
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun displayWelcomeMessage() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val userName = prefs.getString("USER_NAME", "User")
        binding.welcomeMessage.text = "Welcome, $userName!"
    }

    private fun setupActivityButtons() {
        binding.btnRunning.setOnClickListener {
            startActivity(Intent(this, RunningActivity::class.java))
        }

        binding.btnCycling.setOnClickListener {
            startActivity(Intent(this, CyclingActivity::class.java))
        }

        binding.btnWeightlifting.setOnClickListener {
            startActivity(Intent(this, WeightliftingActivity::class.java))
        }

        binding.btnSwimming.setOnClickListener {
            startActivity(Intent(this, SwimmingActivity::class.java))
        }

        binding.btnYoga.setOnClickListener {
            startActivity(Intent(this, YogaActivity::class.java))
        }
    }

    // Handle back button press
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}