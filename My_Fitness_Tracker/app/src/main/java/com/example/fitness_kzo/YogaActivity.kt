package com.example.fitness_kzo

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fitness_kzo.databinding.ActivityYogaBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.roundToInt

class YogaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYogaBinding
    private lateinit var sessionManager: SessionManager
    private var timerHandler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isTimerRunning = false
    private var caloriesBurned = 0f

    // MET values for different intensities
    private val metValues = mapOf(
        "low" to 2.5f,
        "medium" to 4.0f,
        "high" to 6.0f
    )

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimerDisplay()
                updateCalories()
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYogaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        setupUI()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }

        // Initialize button states
        updateButtonStates(
            startEnabled = true,
            stopEnabled = false,
            resetEnabled = false
        )

        // Timer control buttons
        binding.startButton.setOnClickListener { startTimer() }
        binding.stopButton.setOnClickListener { stopTimer() }
        binding.resetButton.setOnClickListener { resetTimer() }

        // Set default intensity
        binding.mediumIntensity.isChecked = true

        // Intensity change listener
        binding.intensityGroup.setOnCheckedChangeListener { _, _ -> updateCalories() }

        // Save button
        binding.saveButton.setOnClickListener { saveSession() }

        // Hide the duration input since we'll use timer
        binding.durationLabel.visibility = View.GONE
        binding.durationInput.visibility = View.GONE
        binding.durationUnit.visibility = View.GONE
    }

    private fun startTimer() {
        if (!isTimerRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isTimerRunning = true
            timerHandler.post(timerRunnable)
            updateButtonStates(startEnabled = false, stopEnabled = true, resetEnabled = true)
        }
    }

    private fun stopTimer() {
        if (isTimerRunning) {
            isTimerRunning = false
            timerHandler.removeCallbacks(timerRunnable)
            updateButtonStates(startEnabled = true, stopEnabled = false, resetEnabled = true)
        }
    }

    private fun resetTimer() {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)
        elapsedTime = 0
        startTime = 0
        caloriesBurned = 0f
        updateTimerDisplay()
        binding.caloriesValue.text = "0"
        updateButtonStates(startEnabled = true, stopEnabled = false, resetEnabled = false)
    }

    private fun updateButtonStates(
        startEnabled: Boolean,
        stopEnabled: Boolean,
        resetEnabled: Boolean
    ) {
        binding.startButton.isEnabled = startEnabled
        binding.stopButton.isEnabled = stopEnabled
        binding.resetButton.isEnabled = resetEnabled
    }

    private fun updateTimerDisplay() {
        val totalSeconds = (elapsedTime / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        binding.timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    private fun updateCalories() {
        try {
            val durationMinutes = elapsedTime / 1000f / 60
            if (durationMinutes <= 0) {
                binding.caloriesValue.text = "0"
                return
            }

            val intensity = when (binding.intensityGroup.checkedRadioButtonId) {
                R.id.lowIntensity -> "low"
                R.id.mediumIntensity -> "medium"
                R.id.highIntensity -> "high"
                else -> "medium"
            }

            val met = metValues[intensity] ?: 4.0f
            val weight = sessionManager.getUserWeight()
            caloriesBurned = met * weight * durationMinutes

            binding.caloriesValue.text = caloriesBurned.roundToInt().toString()
        } catch (e: Exception) {
            binding.caloriesValue.text = "0"
        }
    }

    private fun saveSession() {
        // Validate timer has been used
        if (elapsedTime <= 0) {
            Toast.makeText(this, "Please use the timer to track your session", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMinutes = elapsedTime / 1000f / 60
        val intensity = when (binding.intensityGroup.checkedRadioButtonId) {
            R.id.lowIntensity -> "low"
            R.id.mediumIntensity -> "medium"
            R.id.highIntensity -> "high"
            else -> {
                Toast.makeText(this, "Please select intensity level", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (caloriesBurned <= 0) {
            Toast.makeText(this, "Invalid calorie calculation", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        saveToServer(durationMinutes, intensity)
    }

    private fun saveToServer(duration: Float, intensity: String) {
        val request = SaveYogaRequest(
            user_id = sessionManager.getUserId(),
            duration_minutes = duration,
            intensity_level = intensity,
            calorie_burned_kcal = caloriesBurned
        )

        RetrofitClient.apiService.saveYogaSession(request).enqueue(
            object : Callback<SaveYogaResponse> {
                override fun onResponse(
                    call: Call<SaveYogaResponse>,
                    response: Response<SaveYogaResponse>
                ) {
                    binding.progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.success == true) {
                            Toast.makeText(
                                this@YogaActivity,
                                "Session saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetTimer()
                        } else {
                            Toast.makeText(
                                this@YogaActivity,
                                "Failed to save session: ${body?.message ?: "Unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@YogaActivity,
                            "Server error: ${response.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<SaveYogaResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this@YogaActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacks(timerRunnable)
    }
}