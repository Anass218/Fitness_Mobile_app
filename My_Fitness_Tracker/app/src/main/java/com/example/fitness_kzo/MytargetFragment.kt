package com.example.fitness_kzo

import SessionManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.FragmentMytargetBinding
import kotlinx.coroutines.launch
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.min

class MytargetFragment : Fragment() {
    private var _binding: FragmentMytargetBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    companion object {
        fun newInstance(): MytargetFragment {
            return MytargetFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMytargetBinding.inflate(inflater, container, false)
        sessionManager = SessionManager(requireContext())

        binding.editTargetsButton.setOnClickListener {
            startActivity(Intent(requireContext(), EditTargetsActivity::class.java))
        }

        binding.selectActivityButton.setOnClickListener {
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadTargetsAndProgress()
    }

    private fun loadTargetsAndProgress() {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        //binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Get targets
                val targetsResponse = RetrofitClient.apiService.getTargets(userId)
                if (targetsResponse.isSuccessful && targetsResponse.body()?.success == true) {
                    val targets = targetsResponse.body()?.data

                    // Calculate days left
                    val daysLeft = calculateDaysLeft(targets?.target_end_date)
                    binding.daysLeft.text = if (daysLeft >= 0) "$daysLeft days left" else "Target period ended"

                    // Get calories burned for each activity
                    val runningCal = getActivityCalories(RetrofitClient.apiService.getRunningCalories(userId))
                    val cyclingCal = getActivityCalories(RetrofitClient.apiService.getCyclingCalories(userId))
                    val swimmingCal = getActivityCalories(RetrofitClient.apiService.getSwimmingCalories(userId))
                    val weightliftingCal = getActivityCalories(RetrofitClient.apiService.getWeightliftingCalories(userId))
                    val yogaCal = getActivityCalories(RetrofitClient.apiService.getYogaCalories(userId))

                    // Update UI
                    updateActivityUI(
                        targets?.running_target ?: 0,
                        runningCal,
                        binding.runningTarget,
                        binding.runningCompleted,
                        binding.runningProgress
                    )

                    updateActivityUI(
                        targets?.cycling_target ?: 0,
                        cyclingCal,
                        binding.cyclingTarget,
                        binding.cyclingCompleted,
                        binding.cyclingProgress
                    )

                    updateActivityUI(
                        targets?.swimming_target ?: 0,
                        swimmingCal,
                        binding.swimmingTarget,
                        binding.swimmingCompleted,
                        binding.swimmingProgress
                    )

                    updateActivityUI(
                        targets?.weightlifting_target ?: 0,
                        weightliftingCal,
                        binding.weightliftingTarget,
                        binding.weightliftingCompleted,
                        binding.weightliftingProgress
                    )

                    updateActivityUI(
                        targets?.yoga_target ?: 0,
                        yogaCal,
                        binding.yogaTarget,
                        binding.yogaCompleted,
                        binding.yogaProgress
                    )
                } else {
                    Toast.makeText(requireContext(), "Failed to load targets", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                //binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun calculateDaysLeft(endDate: String?): Long {
        if (endDate.isNullOrEmpty()) return -1

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val end = sdf.parse(endDate)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.time

            TimeUnit.MILLISECONDS.toDays(end.time - today.time)
        } catch (e: Exception) {
            -1
        }
    }

    private suspend fun getActivityCalories(response: Response<ActivityCaloriesResponse>): Int {
        return if (response.isSuccessful && response.body()?.success == true) {
            (response.body()?.data?.sumOf { it.calorie_burned_kcal.toDouble() } ?: 0.0).toInt()
        } else {
            0
        }
    }

    private fun updateActivityUI(
        target: Int,
        completed: Int,
        targetView: TextView,
        completedView: TextView,
        progressBar: ProgressBar
    ) {
        targetView.text = "$target kcal"
        completedView.text = completed.toString()

        val progress = if (target > 0) min((completed * 100) / target, 100) else 0
        progressBar.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}