package com.example.fitness_kzo

import SessionManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.FragmentGraphBinding
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.launch

class GraphFragment : Fragment() {
    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private val api = RetrofitClient.apiService
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupChart()
        loadChartData()
    }

    private fun setupChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            legend.isWordWrapEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            setDrawEntryLabels(true)
            setUsePercentValues(true)
            setExtraOffsets(20f, 0f, 20f, 20f)
        }
    }

    private fun loadChartData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = api.getAllActivities(sessionManager.getUserId())
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { activities ->
                        if (activities.isNotEmpty()) {
                            updateChart(activities)
                        } else {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.pieChart.visibility = View.GONE
                        }
                    }
                } else {
                    showError("Failed to load chart data")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateChart(activities: List<Api.ActivityRecordForRF>) {
        val calorieMap = activities.groupBy { it.type }
            .mapValues { it.value.sumOf { it.calorie_burned_kcal.toDouble() } }

        val entries = calorieMap.map {
            PieEntry(it.value.toFloat(), it.key.replaceFirstChar { char -> char.uppercase() })
        }

        val dataSet = PieDataSet(entries, "Calories Burned").apply {
            colors = listOf(
                Color.parseColor("#FF6384"),
                Color.parseColor("#36A2EB"),
                Color.parseColor("#FFCE56"),
                Color.parseColor("#4BC0C0"),
                Color.parseColor("#9966FF")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 14f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
            visibility = View.VISIBLE
        }
        binding.emptyView.visibility = View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance() = GraphFragment()
    }
}