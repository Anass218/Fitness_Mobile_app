package com.example.fitness_kzo

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.ActivityWeightliftingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit

class WeightliftingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeightliftingBinding
    private lateinit var sessionManager: SessionManager

    // Timer variables
    private var isTracking = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    // Activity data
    private var sets = 0
    private var reps = 0
    private var weightKg = 0f
    private var caloriesBurned = 0f

    companion object {
        private const val MET_WEIGHTLIFTING = 6.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeightliftingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupButtons()
        setupTimer()
        setupBackPressHandler()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            showExitConfirmationDialog()
        }
    }

    private fun setupButtons() {
        binding.fabStart.setOnClickListener { startTracking() }
        binding.fabPause.setOnClickListener { pauseTracking() }
        binding.fabSave.setOnClickListener { saveActivity() }
        binding.fabReset.setOnClickListener { resetTracking() }
    }

    private fun setupTimer() {
        runnable = object : Runnable {
            override fun run() {
                if (isTracking) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    updateTimerDisplay()
                    calculateCalories()
                }
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateTimerDisplay() {
        val hours = TimeUnit.MILLISECONDS.toHours(elapsedTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
        binding.tvDuration.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun calculateCalories() {
        try {
            val durationHours = elapsedTime.toDouble() / 3600000.0
            val userWeight = sessionManager.getUserWeight()
            caloriesBurned = (MET_WEIGHTLIFTING * userWeight * durationHours).toFloat()
            binding.tvCalories.text = getString(R.string.calories_burned_w, caloriesBurned)
        } catch (e: Exception) {
            binding.tvCalories.text = getString(R.string.calories_burned_w, 0f)
        }
    }

    private fun startTracking() {
        if (!validateInputs()) {
            Toast.makeText(this, "Please enter valid sets, reps and weight", Toast.LENGTH_SHORT).show()
            return
        }

        // Get input values
        sets = binding.etSets.text.toString().toInt()
        reps = binding.etReps.text.toString().toInt()
        weightKg = binding.etWeight.text.toString().toFloat()

        isTracking = true
        startTime = System.currentTimeMillis()
        elapsedTime = 0

        // Update UI
        binding.fabStart.isVisible = false
        binding.fabPause.isVisible = true
        binding.fabSave.isVisible = false
        binding.fabReset.isVisible = false
        binding.etSets.isEnabled = false
        binding.etReps.isEnabled = false
        binding.etWeight.isEnabled = false

        handler.post(runnable)
        Toast.makeText(this, "Tracking started", Toast.LENGTH_SHORT).show()
    }

    private fun pauseTracking() {
        isTracking = false
        calculateFinalMetrics()

        // Update UI
        binding.fabPause.isVisible = false
        binding.fabStart.isVisible = true
        binding.fabSave.isVisible = true
        binding.fabReset.isVisible = true

        Toast.makeText(this, "Tracking paused", Toast.LENGTH_SHORT).show()
    }

    private fun calculateFinalMetrics() {
        val totalVolume = sets * reps * weightKg
        val avgWeight = if (sets > 0) totalVolume / sets else 0f
        val avgReps = if (sets > 0) reps.toFloat() / sets else 0f

        binding.tvTotalVolume.text = getString(R.string.total_volume, totalVolume)
        binding.tvAvgWeight.text = getString(R.string.avg_weight, avgWeight)
        binding.tvAvgReps.text = getString(R.string.avg_reps, avgReps)
    }

    private fun validateInputs(): Boolean {
        return try {
            val sets = binding.etSets.text.toString().toInt()
            val reps = binding.etReps.text.toString().toInt()
            val weight = binding.etWeight.text.toString().toFloat()
            sets > 0 && reps > 0 && weight > 0
        } catch (e: Exception) {
            false
        }
    }

    private fun resetTracking() {
        isTracking = false
        handler.removeCallbacks(runnable)
        elapsedTime = 0

        // Reset inputs
        binding.etSets.setText("")
        binding.etReps.setText("")
        binding.etWeight.setText("")
        binding.tvDuration.text = "00:00:00"
        binding.tvCalories.text = getString(R.string.calories_burned_w, 0f)

        // Reset metrics
        binding.tvTotalVolume.text = getString(R.string.total_volume, 0f)
        binding.tvAvgWeight.text = getString(R.string.avg_weight, 0f)
        binding.tvAvgReps.text = getString(R.string.avg_reps, 0f)

        // Update UI
        binding.fabPause.isVisible = false
        binding.fabSave.isVisible = false
        binding.fabReset.isVisible = false
        binding.fabStart.isVisible = true
        binding.etSets.isEnabled = true
        binding.etReps.isEnabled = true
        binding.etWeight.isEnabled = true
    }

    private fun saveActivity() {
        if (isFinishing || isDestroyed) return // Check if activity is being destroyed

        if (!validateInputs()) {
            showToast("Invalid activity data")
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            showToast("User not logged in")
            return
        }

        // Ensure minimum duration of 1 minute
        val durationMillis = if (elapsedTime > 0) elapsedTime else 60000
        val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis).toFloat().coerceAtLeast(1f)

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        binding.progressBar.isVisible = true
                    }
                }

                val request = SaveWeightliftingRequest(
                    user_id = userId,
                    duration_minutes = durationMinutes,
                    sets = sets,
                    reps_per_set = reps,
                    weight_kg_per_rep = weightKg,
                    calorie_burned_kcal = caloriesBurned
                )

                val response = try {
                    RetrofitClient.apiService.saveWeightliftingSession(request)
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        if (!isFinishing && !isDestroyed) {
                            showToast("Network error: ${e.message ?: "Unknown error"}")
                        }
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    if (isFinishing || isDestroyed) return@withContext
                    binding.progressBar.isVisible = false
                    when {
                        !response.isSuccessful -> {
                            val errorBody = try {
                                response.errorBody()?.string()
                            } catch (e: IOException) {
                                null
                            }
                            showToast("Server error: ${errorBody ?: "HTTP ${response.code()}"}")
                        }
                        response.body() == null -> {
                            showToast("Empty response from server")
                        }
                        response.body()?.success == true -> {
                            showToast("Activity saved successfully")
                            finish()
                        }
                        else -> {
                            showToast(response.body()?.message ?: "Unknown error occurred")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isFinishing && !isDestroyed) {
                        binding.progressBar.isVisible = false
                        showToast("Error: ${e.message ?: "Unknown error"}")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        if (!isFinishing && !isDestroyed) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        if (isTracking) {
            AlertDialog.Builder(this)
                .setTitle("Discard Activity?")
                .setMessage("You're currently tracking an activity. Are you sure you want to exit?")
                .setPositiveButton("Exit") { _, _ -> finish() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        lifecycleScope.cancel() // Cancel ongoing coroutines
        super.onDestroy()
    }
}