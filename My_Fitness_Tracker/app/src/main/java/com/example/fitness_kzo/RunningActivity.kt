package com.example.fitness_kzo

import SessionManager
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class RunningActivity : AppCompatActivity(), SensorEventListener {

    // Views
    private lateinit var circularProgressBar: CircularProgressBar
    private lateinit var distanceText: TextView
    private lateinit var goalText: TextView
    private lateinit var calBurnedTV: TextView
    private lateinit var timeTV: TextView
    private lateinit var paceTV: TextView
    private lateinit var startStopBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var changeTargetBtn: Button
    private lateinit var runningAnimation: LottieAnimationView

    // Running variables
    private var isRunning = false
    private var startTime: Long = 0
    private var totalDistance = 0.0 // in km
    private var currentTarget = 5.0
    private var elapsedTime = 0L // in ms
    private var stepCount = 0
    private var initialSteps = 0

    // Step counter
    private lateinit var sensorManager: SensorManager
    private var stepCounterSensor: Sensor? = null
    private val stepLengthMeters = 0.7 // Average step length

    // Timer
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerRunnable: Runnable

    // Session manager
    private lateinit var sessionManager: SessionManager

    companion object {
        private const val ACTIVITY_RECOGNITION_REQUEST_CODE = 1001
        private const val TAG = "RunningActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_running)

        sessionManager = SessionManager(this)
        initViews()
        setupStepCounter()
        setupButtons()
        setupTimer()
        setupBackButton()
    }

    private fun initViews() {
        circularProgressBar = findViewById(R.id.circularProgressBar)
        distanceText = findViewById(R.id.distanceText)
        goalText = findViewById(R.id.goalText)
        calBurnedTV = findViewById(R.id.cal_burned_tv)
        timeTV = findViewById(R.id.time_tv)
        paceTV = findViewById(R.id.pace_tv)
        startStopBtn = findViewById(R.id.startStopBtn)
        saveBtn = findViewById(R.id.saveBtn)
        changeTargetBtn = findViewById(R.id.btnChangeTargetDistance)
        runningAnimation = findViewById(R.id.runningAnimation)

        goalText.text = "Goal: ${currentTarget} km"
        saveBtn.isEnabled = false
    }

    private fun setupStepCounter() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepCounterSensor == null) {
            Toast.makeText(this, "Step counter not available on this device", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            if (isRunning) {
                Toast.makeText(this, "Please stop running before exiting", Toast.LENGTH_SHORT).show()
            } else {
                finish()
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                if (initialSteps == 0) {
                    initialSteps = it.values[0].toInt()
                }
                stepCount = it.values[0].toInt() - initialSteps
                totalDistance = (stepCount * stepLengthMeters) / 1000.0
                updateDistanceUI()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun setupButtons() {
        startStopBtn.setOnClickListener {
            if (isRunning) stopRunning() else startRunning()
        }

        saveBtn.setOnClickListener {
            saveRunningSession()
        }

        changeTargetBtn.setOnClickListener {
            currentTarget = if (currentTarget == 5.0) 10.0 else 5.0
            goalText.text = "Goal: ${currentTarget} km"
            updateProgressBar()
            Toast.makeText(this, "Target changed to $currentTarget km", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    elapsedTime = System.currentTimeMillis() - startTime
                    updateTimeUI()
                    updateCaloriesBurned()
                    updatePace()
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    private fun startRunning() {
        if (!checkActivityRecognitionPermission()) {
            requestActivityRecognitionPermission()
            return
        }

        if (stepCounterSensor == null) {
            Toast.makeText(this, "Step counter not available", Toast.LENGTH_SHORT).show()
            return
        }

        isRunning = true
        startStopBtn.text = "Stop Running"
        saveBtn.isEnabled = false
        changeTargetBtn.isEnabled = false
        runningAnimation.playAnimation()

        // Reset
        startTime = System.currentTimeMillis()
        stepCount = 0
        initialSteps = 0
        totalDistance = 0.0
        elapsedTime = 0

        sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST)
        handler.post(timerRunnable)
        updateDistanceUI()
    }

    private fun stopRunning() {
        isRunning = false
        startStopBtn.text = "Start Running"
        saveBtn.isEnabled = true
        changeTargetBtn.isEnabled = true
        runningAnimation.pauseAnimation()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(timerRunnable)
    }

    private fun updateDistanceUI() {
        distanceText.text = String.format(Locale.getDefault(), "%.2f km", totalDistance)
        updateProgressBar()
    }

    private fun updateProgressBar() {
        val progress = (totalDistance / currentTarget * 100).coerceAtMost(100.0)
        circularProgressBar.setProgressWithAnimation(progress.toFloat(), 1000)
    }

    private fun updateTimeUI() {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
        timeTV.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun updateCaloriesBurned() {
        val calories = (totalDistance * 65).roundToInt()
        calBurnedTV.text = "$calories kcal"
    }

    private fun updatePace() {
        if (totalDistance > 0 && elapsedTime > 0) {
            val pace = (elapsedTime / 60000.0) / totalDistance
            paceTV.text = String.format(Locale.getDefault(), "%.1f min/km", pace)
        } else {
            paceTV.text = "0.0 min/km"
        }
    }

    private fun saveRunningSession() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val durationMinutes = elapsedTime / (1000f * 60f)
        val caloriesBurned = (totalDistance * 65).roundToInt()

        saveBtn.isEnabled = false
        Toast.makeText(this, "Saving session...", Toast.LENGTH_SHORT).show()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.saveRunningSession(
                    SaveRunningRequest(
                        user_id = userId,
                        duration_minutes = durationMinutes,
                        distance_km = totalDistance,
                        calorie_burned_kcal = caloriesBurned
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            if (it.success) {
                                Toast.makeText(
                                    this@RunningActivity,
                                    "Session saved successfully!",
                                    Toast.LENGTH_LONG
                                ).show()
                                resetUI()
                            } else {
                                Toast.makeText(
                                    this@RunningActivity,
                                    "Error: ${it.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                saveBtn.isEnabled = true
                            }
                        } ?: run {
                            Toast.makeText(
                                this@RunningActivity,
                                "Empty response from server",
                                Toast.LENGTH_SHORT
                            ).show()
                            saveBtn.isEnabled = true
                        }
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                        Toast.makeText(
                            this@RunningActivity,
                            "Server error: $errorMsg",
                            Toast.LENGTH_SHORT
                        ).show()
                        saveBtn.isEnabled = true
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@RunningActivity,
                        "Network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(TAG, "Network error", e)
                    saveBtn.isEnabled = true
                }
            }
        }
    }

    private fun resetUI() {
        totalDistance = 0.0
        elapsedTime = 0
        updateDistanceUI()
        timeTV.text = "00:00"
        calBurnedTV.text = "0 kcal"
        paceTV.text = "0.0 min/km"
        circularProgressBar.progress = 0f
    }

    private fun checkActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                ACTIVITY_RECOGNITION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ACTIVITY_RECOGNITION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRunning()
            } else {
                Toast.makeText(this, "Permission required to count steps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRunning) {
            sensorManager.unregisterListener(this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRunning && stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timerRunnable)
        sensorManager.unregisterListener(this)
    }
}