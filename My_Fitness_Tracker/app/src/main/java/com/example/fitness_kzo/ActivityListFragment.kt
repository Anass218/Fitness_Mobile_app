package com.example.fitness_kzo

import SessionManager
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fitness_kzo.databinding.FragmentActivityListBinding
import kotlinx.coroutines.launch

class ActivityListFragment : Fragment() {
    private var _binding: FragmentActivityListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ActivityAdapter
    private val api = RetrofitClient.apiService
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActivityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        setupRecyclerView()
        loadActivities()
    }

    private fun setupRecyclerView() {
        adapter = ActivityAdapter { record ->
            showDeleteConfirmation(record)
        }
        binding.recyclerViewActivities.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ActivityListFragment.adapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun loadActivities() {
        //binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = api.getAllActivities(sessionManager.getUserId())
                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.let { activities ->
                        adapter.submitList(activities)
                    }
                } else {
                    showError("Failed to load activities")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                //binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showDeleteConfirmation(record: Api.ActivityRecordForRF) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Activity")
            .setMessage("Are you sure you want to delete this ${record.type} record?")
            .setPositiveButton("Delete") { _, _ ->
                deleteActivity(record)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteActivity(record: Api.ActivityRecordForRF) {
        //binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val deleteRequest = DeleteRequest(id = record.id)
                val response = when (record.type) {
                    "running" -> api.deleteRunningRecord(deleteRequest)
                    "cycling" -> api.deleteCyclingRecord(deleteRequest)
                    "swimming" -> api.deleteSwimmingRecord(deleteRequest)
                    "weightlifting" -> api.deleteWeightliftingRecord(deleteRequest)
                    "yoga" -> api.deleteYogaRecord(deleteRequest)
                    else -> null
                }

                if (response?.isSuccessful == true && response.body()?.success == true) {
                    loadActivities()
                    Toast.makeText(context, "Activity deleted", Toast.LENGTH_SHORT).show()
                } else {
                    showError("Failed to delete activity")
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
            } finally {
                //binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        fun newInstance() = ActivityListFragment()
    }
}