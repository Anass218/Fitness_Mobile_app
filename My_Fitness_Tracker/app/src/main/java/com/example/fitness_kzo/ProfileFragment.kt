package com.example.fitness_kzo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.fitness_kzo.databinding.FragmentProfileBinding
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPrefManager: SharedPrefManager
    private val prefs by lazy { requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefManager = SharedPrefManager(requireContext())
        loadUserData()
        binding.btnSave.setOnClickListener { updateProfile() }
    }

    private fun loadUserData() {
        // Load from SharedPreferences for consistency
        val userId = prefs.getInt("USER_ID", 0)
        val name = prefs.getString("USER_NAME", "") ?: ""
        val email = prefs.getString("USER_EMAIL", "") ?: ""
        val weight = prefs.getFloat("USER_WEIGHT", 0f).toDouble()
        val height = prefs.getFloat("USER_HEIGHT", 0f).toDouble()

        binding.etName.setText(name)
        binding.etEmail.setText(email)
        binding.etWeight.setText(weight.toString())
        binding.etHeight.setText(height.toString())
    }

    private fun updateProfile() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val weightStr = binding.etWeight.text.toString().trim()
        val heightStr = binding.etHeight.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightStr.toDoubleOrNull()
        val height = heightStr.toDoubleOrNull()

        if (weight == null || weight <= 0) {
            binding.etWeight.error = "Invalid weight value"
            return
        }

        if (height == null || height <= 0) {
            binding.etHeight.error = "Invalid height value"
            return
        }

        val userId = prefs.getInt("USER_ID", 0)
        if (userId == 0) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updateProfile(
                    ProfileUpdateRequest(
                        id = userId,
                        name = name,
                        email = email,
                        weight = weight,
                        height = height
                    )
                )

                if (response.status == "success") {
                    // Update local preferences
                    with(prefs.edit()) {
                        putString("USER_NAME", name)
                        putString("USER_EMAIL", email)
                        putFloat("USER_WEIGHT", weight.toFloat())
                        putFloat("USER_HEIGHT", height.toFloat())
                        apply()
                    }

                    // Update SharedPrefManager
                    sharedPrefManager.getUser()?.let { currentUser ->
                        val updatedUser = currentUser.copy(
                            name = name,
                            email = email,
                            weight = weight,
                            height = height
                        )
                        sharedPrefManager.saveUser(updatedUser)
                    }

                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update failed: ${response.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ProfileFragment()
    }
}

data class ProfileUpdateRequest(
    val id: Int,
    val name: String,
    val email: String,
    val weight: Double,
    val height: Double
)

data class ProfileUpdateResponse(
    val status: String,
    val message: String
)