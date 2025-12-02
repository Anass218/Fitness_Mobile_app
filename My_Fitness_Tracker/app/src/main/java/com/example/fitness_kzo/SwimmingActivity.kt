package com.example.fitness_kzo

import SessionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.Serializable

class SwimmingActivity : AppCompatActivity() {

    // Views
    private lateinit var timerTextView: TextView
    private lateinit var lapsTextView: TextView
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var saveButton: Button
    private lateinit var btnBack: ImageButton
    private lateinit var strokeTypeSpinner: Spinner
    private lateinit var progressBar: ProgressBar

    // Timer state
    private var seconds = 0
    private var isRunning = false
    private var wasRunning = false
    private val handler = Handler(Looper.getMainLooper())

    // Stroke types
    private var selectedStroke = "Freestyle"
    private val strokeTypes = listOf("Freestyle", "Backstroke", "Breaststroke", "Butterfly")

    // Lap tracking
    private val lapRecords = mutableListOf<LapRecord>()

    // Session management
    private lateinit var sessionManager: SessionManager

    // Timer runnable
    private val updateTimer = object : Runnable {
        override fun run() {
            if (isRunning) {
                seconds++
                updateTimerDisplay()
            }
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_swimming)

        // Edge-to-edge display handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize session manager
        sessionManager = SessionManager(this)

        // Initialize views
        timerTextView = findViewById(R.id.timerTextView)
        lapsTextView = findViewById(R.id.lapsTextView)
        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)
        saveButton = findViewById(R.id.saveButton)
        btnBack = findViewById(R.id.btnBack)
        strokeTypeSpinner = findViewById(R.id.strokeTypeSpinner)
        progressBar = findViewById(R.id.progressBar)

        // Setup stroke type spinner
        setupStrokeSpinner()

        // Button click listeners
        setupButtonListeners()

        // Modern back button handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateBackToMain()
            }
        })

        // Restore state if configuration changed
        if (savedInstanceState != null) {
            seconds = savedInstanceState.getInt("seconds")
            isRunning = savedInstanceState.getBoolean("isRunning")
            wasRunning = savedInstanceState.getBoolean("wasRunning")

            val savedLaps = savedInstanceState.getSerializable("lapRecords") as? Array<LapRecord>
            savedLaps?.let {
                lapRecords.clear()
                lapRecords.addAll(it)
                updateLapsDisplay()
            }

            strokeTypeSpinner.setSelection(savedInstanceState.getInt("strokePosition", 0))
        }

        // Initial button states
        updateButtonStates(
            startEnabled = true,
            stopEnabled = false,
            resetEnabled = false,
            saveEnabled = true
        )

        updateTimerDisplay()
    }

    private fun setupStrokeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, strokeTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        strokeTypeSpinner.adapter = adapter

        strokeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedStroke = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedStroke = "Freestyle"
            }
        }
    }

    private fun setupButtonListeners() {
        startButton.setOnClickListener { startTimer() }
        stopButton.setOnClickListener { stopTimer() }
        resetButton.setOnClickListener { resetTimer() }
        saveButton.setOnClickListener { saveSession() }
        btnBack.setOnClickListener { navigateBackToMain() }
    }

    private fun navigateBackToMain() {
        if (isRunning) stopTimer()
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("seconds", seconds)
        outState.putBoolean("isRunning", isRunning)
        outState.putBoolean("wasRunning", wasRunning)
        outState.putSerializable("lapRecords", lapRecords.toTypedArray())
        outState.putInt("strokePosition", strokeTypeSpinner.selectedItemPosition)
    }

    override fun onPause() {
        super.onPause()
        wasRunning = isRunning
        isRunning = false
        handler.removeCallbacks(updateTimer)
    }

    override fun onResume() {
        super.onResume()
        if (wasRunning) {
            isRunning = true
            handler.post(updateTimer)
        }
    }

    private fun startTimer() {
        if (!isRunning) {
            isRunning = true
            updateButtonStates(
                startEnabled = false,
                stopEnabled = true,
                resetEnabled = false,
                saveEnabled = false
            )
            handler.post(updateTimer)
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            updateButtonStates(
                startEnabled = true,
                stopEnabled = false,
                resetEnabled = true,
                saveEnabled = true
            )
            recordLap()
        }
    }

    private fun resetTimer() {
        isRunning = false
        seconds = 0
        lapRecords.clear()
        updateButtonStates(
            startEnabled = true,
            stopEnabled = false,
            resetEnabled = false,
            saveEnabled = false
        )
        updateTimerDisplay()
        updateLapsDisplay()
        handler.removeCallbacks(updateTimer)
    }

    private fun updateButtonStates(
        startEnabled: Boolean,
        stopEnabled: Boolean,
        resetEnabled: Boolean,
        saveEnabled: Boolean
    ) {
        startButton.isEnabled = startEnabled
        stopButton.isEnabled = stopEnabled
        resetButton.isEnabled = resetEnabled
        saveButton.isEnabled = saveEnabled
    }

    private fun recordLap() {
        val lapTime = seconds
        val formattedTime = formatTime(lapTime)
        val lapNumber = lapRecords.size + 1

        lapRecords.add(LapRecord(lapNumber, selectedStroke, lapTime, formattedTime))
        updateLapsDisplay()
    }

    private fun updateTimerDisplay() {
        timerTextView.text = formatTime(seconds)
    }

    private fun updateLapsDisplay() {
        val lapsText = buildString {
            if (lapRecords.isEmpty()) {
                append("No laps recorded")
            } else {
                lapRecords.forEach { record ->
                    append("Lap ${record.lapNumber} (${record.stroke}): ${record.formattedTime}\n")
                }
            }
        }
        lapsTextView.text = lapsText
    }

    private fun formatTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    private fun saveSession() {
        if (lapRecords.isEmpty()) {
            Toast.makeText(this, "No laps recorded to save", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = sessionManager.getUserWeight()
        if (weight <= 0) {
            Toast.makeText(this, "User weight not set", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate stroke type (most frequent in laps)
        val strokeType = if (lapRecords.isNotEmpty()) {
            lapRecords.groupingBy { it.stroke }.eachCount()
                .maxByOrNull { it.value }?.key ?: selectedStroke
        } else {
            selectedStroke
        }

        // Calculate calories
        val totalCalories = calculateTotalCalories(weight)

        // Prepare request
        val request = SaveSwimmingRequest(
            user_id = userId,
            duration_minutes = seconds / 60.0,
            stroke_type = strokeType,
            laps = lapRecords.size,
            calorie_burned_kcal = totalCalories
        )

        // Show progress and disable UI during network call
        progressBar.visibility = View.VISIBLE
        isUiEnabled(false)

        lifecycleScope.launch {
            try {
                val response: Response<com.example.fitness_kzo.SaveSwimmingResponse> = RetrofitClient.apiService.saveSwimmingSession(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.success) {
                            Toast.makeText(
                                this@SwimmingActivity,
                                "Session saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetTimer()
                        } else {
                            showSaveError(it.message ?: "Unknown server error")
                        }
                    } ?: showSaveError("Empty server response")
                } else {
                    showSaveError("Server error: ${response.code()}")
                }
            } catch (e: Exception) {
                showSaveError("Network error: ${e.message}")
            } finally {
                progressBar.visibility = View.GONE
                isUiEnabled(true)
            }
        }
    }

    private fun calculateTotalCalories(weight: Float): Double {
        var totalCalories = 0.0
        if (lapRecords.isNotEmpty()) {
            for (lap in lapRecords) {
                val metValue = getMETValue(lap.stroke)
                val hours = lap.seconds / 3600.0
                totalCalories += metValue * weight * hours
            }
        } else {
            val metValue = getMETValue(selectedStroke)
            val hours = seconds / 3600.0
            totalCalories = metValue * weight * hours
        }
        return "%.2f".format(totalCalories).toDouble()
    }

    private fun getMETValue(stroke: String): Double {
        return when (stroke.lowercase()) {
            "freestyle" -> 8.0
            "backstroke" -> 6.0
            "breaststroke" -> 7.0
            "butterfly" -> 11.0
            else -> 7.0 // Default MET value
        }
    }

    private fun showSaveError(message: String) {
        Toast.makeText(
            this@SwimmingActivity,
            "Save failed: $message",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isUiEnabled(enabled: Boolean) {
        startButton.isEnabled = enabled
        stopButton.isEnabled = enabled
        resetButton.isEnabled = enabled
        saveButton.isEnabled = enabled
        strokeTypeSpinner.isEnabled = enabled
        btnBack.isEnabled = enabled
    }

    data class LapRecord(
        val lapNumber: Int,
        val stroke: String,
        val seconds: Int,
        val formattedTime: String
    ) : Serializable

    data class SaveSwimmingRequest(
        val user_id: Int,
        val duration_minutes: Double,
        val stroke_type: String,
        val laps: Int,
        val calorie_burned_kcal: Double
    )

    data class SaveSwimmingResponse(
        val success: Boolean,
        val message: String
    )
}