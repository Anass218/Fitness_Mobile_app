package com.example.fitness_kzo

import SessionManager
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.ActivityEditTargetsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditTargetsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditTargetsBinding
    private lateinit var sessionManager: SessionManager
    private val calendar = Calendar.getInstance()
    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditTargetsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)

        setupToolbar()
        setupDatePickers()
        loadCurrentTargets()
        setupSaveButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Targets"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDatePickers() {
        binding.btnSelectStartDate.setOnClickListener { showDatePicker(true) }
        binding.btnSelectEndDate.setOnClickListener { showDatePicker(false) }
    }

    private fun showDatePicker(isStartDate: Boolean) {
        val minDate = if (isStartDate) {
            calendar.apply { add(Calendar.DAY_OF_MONTH, -1) }.timeInMillis
        } else {
            selectedStartDate?.time ?: calendar.timeInMillis
        }

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }

                if (isStartDate) {
                    selectedStartDate = selectedDate.time
                    binding.tvStartDate.text =
                        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
                } else {
                    if (selectedDate.time.after(Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -1) }.time)) {
                        selectedEndDate = selectedDate.time
                        binding.tvEndDate.text =
                            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.time)
                    } else {
                        Toast.makeText(
                            this,
                            "End date must be after start date",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = minDate
            show()
        }
    }

    private fun loadCurrentTargets() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getTargets(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val targets = response.body()?.data
                    runOnUiThread {
                        binding.etRunning.setText(targets?.running_target?.toString() ?: "0")
                        binding.etCycling.setText(targets?.cycling_target?.toString() ?: "0")
                        binding.etSwimming.setText(targets?.swimming_target?.toString() ?: "0")
                        binding.etWeightlifting.setText(targets?.weightlifting_target?.toString() ?: "0")
                        binding.etYoga.setText(targets?.yoga_target?.toString() ?: "0")

                        targets?.target_start_date?.let {
                            binding.tvStartDate.text = it
                            selectedStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                        }

                        targets?.target_end_date?.let {
                            binding.tvEndDate.text = it
                            selectedEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(it)
                        }
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@EditTargetsActivity, "Failed to load targets", Toast.LENGTH_SHORT).show()
                }
            } finally {
                //binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                saveTargets()
            }
        }
    }

    private fun validateInputs(): Boolean {
        if (selectedStartDate == null) {
            Toast.makeText(this, "Please select start date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedEndDate == null) {
            Toast.makeText(this, "Please select end date", Toast.LENGTH_SHORT).show()
            return false
        }

        if (selectedEndDate!!.before(selectedStartDate)) {
            Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun saveTargets() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val request = UpdateTargetsRequest(
            user_id = userId,
            running_target = binding.etRunning.text.toString().toIntOrNull() ?: 0,
            cycling_target = binding.etCycling.text.toString().toIntOrNull() ?: 0,
            swimming_target = binding.etSwimming.text.toString().toIntOrNull() ?: 0,
            weightlifting_target = binding.etWeightlifting.text.toString().toIntOrNull() ?: 0,
            yoga_target = binding.etYoga.text.toString().toIntOrNull() ?: 0,
            target_start_date = sdf.format(selectedStartDate!!),
            target_end_date = sdf.format(selectedEndDate!!)
        )

        //binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateTargets(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    runOnUiThread {
                        Toast.makeText(this@EditTargetsActivity, "Targets updated", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@EditTargetsActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@EditTargetsActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            } finally {
                //binding.progressBar.visibility = View.GONE
            }
        }
    }
}