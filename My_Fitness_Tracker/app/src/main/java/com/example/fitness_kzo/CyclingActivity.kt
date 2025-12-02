package com.example.fitness_kzo

import SessionManager
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.ActivityCyclingBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class CyclingActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCyclingBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var mMap: GoogleMap
    private var currentLocationMarker: Marker? = null
    private var pathPolyline: Polyline? = null
    private val pathPoints = mutableListOf<LatLng>()
    private var lastLocation: Location? = null
    private var totalDistance = 0.0
    private var totalTime = 0L
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var isPaused = false
    private var isTracking = false

    private val timerHandler = android.os.Handler(Looper.getMainLooper())
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTracking && !isPaused) {
                totalTime = System.currentTimeMillis() - startTime
                updateDurationDisplay()
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

    private val sessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCyclingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMap()
        setupLocationServices()
        setupButtonListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        createLocationCallback()
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(3000)
            setMaxUpdateDelayMillis(10000)
        }.build()
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    updateLocation(location)
                    updateSpeedDisplay(location.speed)
                }
            }
        }
    }

    private fun setupButtonListeners() {
        binding.btnStartCycling.setOnClickListener { startCycling() }
        binding.btnPauseCycling.setOnClickListener { togglePause() }
        binding.btnSaveCycling.setOnClickListener { saveSession() }
        binding.btnResetCycling.setOnClickListener { resetSession() }
    }

    private fun startCycling() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        isTracking = true
        isPaused = false
        startTime = System.currentTimeMillis()
        timerHandler.postDelayed(timerRunnable, 1000)
        startLocationUpdates()

        binding.btnStartCycling.visibility = View.GONE
        binding.btnPauseCycling.visibility = View.VISIBLE
        binding.btnSaveCycling.visibility = View.GONE
        binding.btnResetCycling.visibility = View.VISIBLE
        binding.btnPauseCycling.contentDescription = getString(R.string.pause)
        binding.tvDistance.setTextColor(Color.parseColor("#4CAF50"))
        binding.tvSpeed.setTextColor(Color.parseColor("#FF9800"))
        binding.tvDuration.setTextColor(Color.parseColor("#03A9F4"))
    }

    private fun togglePause() {
        isPaused = !isPaused
        if (isPaused) {
            pausedTime = System.currentTimeMillis()
            stopLocationUpdates()
            binding.btnPauseCycling.contentDescription = getString(R.string.resume)
            binding.btnPauseCycling.setImageResource(R.drawable.ic_play) // Change icon to play
            binding.btnSaveCycling.visibility = View.VISIBLE
            binding.tvDistance.setTextColor(Color.GRAY)
            binding.tvSpeed.setTextColor(Color.GRAY)
            binding.tvDuration.setTextColor(Color.GRAY)
        } else {
            startTime += (System.currentTimeMillis() - pausedTime)
            startLocationUpdates()
            binding.btnPauseCycling.contentDescription = getString(R.string.pause)
            binding.btnPauseCycling.setImageResource(R.drawable.ic_pause) // Change back to pause icon
            binding.btnSaveCycling.visibility = View.GONE
            binding.tvDistance.setTextColor(Color.parseColor("#4CAF50"))
            binding.tvSpeed.setTextColor(Color.parseColor("#FF9800"))
            binding.tvDuration.setTextColor(Color.parseColor("#03A9F4"))
            timerHandler.postDelayed(timerRunnable, 1000)
        }
    }

    private fun resetSession() {
        isTracking = false
        isPaused = false
        totalDistance = 0.0
        totalTime = 0L
        pathPoints.clear()
        timerHandler.removeCallbacks(timerRunnable)
        stopLocationUpdates()

        binding.tvDistance.text = "0.00 km"
        binding.tvSpeed.text = "0.0 km/h"
        binding.tvDuration.text = "00:00:00"
        binding.btnStartCycling.visibility = View.VISIBLE
        binding.btnPauseCycling.visibility = View.GONE
        binding.btnSaveCycling.visibility = View.GONE
        binding.btnResetCycling.visibility = View.GONE
        binding.tvDistance.setTextColor(Color.parseColor("#4CAF50"))
        binding.tvSpeed.setTextColor(Color.parseColor("#FF9800"))
        binding.tvDuration.setTextColor(Color.parseColor("#03A9F4"))

        currentLocationMarker?.remove()
        pathPolyline?.remove()
    }

    private fun saveSession() {
        if (!isTracking) return

        // Use fractional minutes instead of integer division
        val durationMinutes = totalTime / 60000.0  // Changed to double division
        val distanceKm = totalDistance / 1000.0
        val avgSpeed = if (durationMinutes > 0) distanceKm / (durationMinutes / 60.0) else 0.0

        // Calculate calories
        val weightKg = sessionManager.getUserWeight()
        val caloriesBurned = 8.5 * weightKg * (durationMinutes / 60.0)

        // Prevent saving sessions with 0 duration/distance
        if (durationMinutes <= 0 || distanceKm <= 0) {
            Toast.makeText(
                this@CyclingActivity,
                "Cannot save session with zero duration or distance",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = SaveCyclingRequest(
                    user_id = sessionManager.getUserId(),
                    duration_minutes = durationMinutes.toFloat(),  // Now includes fractional minutes
                    distance_km = distanceKm.toFloat(),
                    speed_kmph = avgSpeed.toFloat(),
                    calorie_burned_kcal = caloriesBurned.toFloat()
                )

                val response = RetrofitClient.apiService.saveCyclingSession(request)

                if (response.isSuccessful) {
                    response.body()?.let { body ->
                        if (body.success) {
                            Toast.makeText(
                                this@CyclingActivity,
                                "Session saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetSession()
                        } else {
                            showError(body.message ?: "Server error occurred")
                        }
                    } ?: showError("Empty response from server")
                } else {
                    val errorBody = response.errorBody()?.string()
                    showError("Server error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.localizedMessage}")
            }
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(
                this@CyclingActivity,
                "Failed to save: $message",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun updateLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)

        // Create bicycle icon marker
        val bicycleIcon = bitmapDescriptorFromVector(R.drawable.bicycle)

        // Update marker
        if (currentLocationMarker == null) {
            currentLocationMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Current Location")
                    .icon(bicycleIcon)
            )
        } else {
            currentLocationMarker?.position = latLng
        }

        // Move camera to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

        // Update path
        lastLocation?.let { prevLocation ->
            val distance = prevLocation.distanceTo(location).toDouble()
            totalDistance += distance
            binding.tvDistance.text = "%.2f km".format(totalDistance / 1000)
        }

        pathPoints.add(latLng)
        updatePathOnMap()

        lastLocation = location
    }

    private fun updatePathOnMap() {
        pathPolyline?.remove()
        if (pathPoints.size > 1) {
            val options = PolylineOptions().apply {
                width(12f)
                color(ContextCompat.getColor(this@CyclingActivity, R.color.cycling_path))
                geodesic(true)
                addAll(pathPoints)
            }
            pathPolyline = mMap.addPolyline(options)
        }
    }

    private fun updateSpeedDisplay(speed: Float) {
        val speedKmh = speed * 3.6
        binding.tvSpeed.text = "%.1f km/h".format(speedKmh)
    }

    private fun updateDurationDisplay() {
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60
        binding.tvDuration.text = "%02d:%02d:%02d".format(hours, minutes, seconds)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCycling()
        } else {
            Toast.makeText(
                this,
                "Location permission required for tracking",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
    }

    private fun bitmapDescriptorFromVector(vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(this, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(
                intrinsicWidth,
                intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isTracking && !isPaused) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}