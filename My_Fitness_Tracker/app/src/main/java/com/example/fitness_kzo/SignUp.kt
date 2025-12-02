// SignUp.kt
package com.example.fitness_kzo

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitness_kzo.databinding.ActivitySignUpBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUp : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Toast.makeText(this, "write", Toast.LENGTH_SHORT).show()

        binding.loginLink.setOnClickListener { finish() }

        binding.signUpButton.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = binding.usernameEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val weight = binding.weightEditText.text.toString().trim()
        val height = binding.heightEditText.text.toString().trim()

        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username required"
            return false
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Valid email required"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        if (weight.isEmpty() || weight.toDoubleOrNull() == null) {
            binding.weightEditText.error = "Valid weight required"
            return false
        }

        if (height.isEmpty() || height.toDoubleOrNull() == null) {
            binding.heightEditText.error = "Valid height required"
            return false
        }

        return true
    }

    private fun registerUser() {
        val request = RegistrationRequest(
            username = binding.usernameEditText.text.toString().trim(),
            email = binding.emailEditText.text.toString().trim(),
            password = binding.passwordEditText.text.toString().trim(),
            weight = binding.weightEditText.text.toString().trim().toDouble(),
            height = binding.heightEditText.text.toString().trim().toDouble()
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.signUpButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.registerUser(request)
                withContext(Dispatchers.Main) {
                    handleRegistrationResponse(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.signUpButton.isEnabled = true
                    Toast.makeText(
                        this@SignUp,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleRegistrationResponse(response: RegistrationResponse) {
        binding.progressBar.visibility = View.GONE
        binding.signUpButton.isEnabled = true

        if (response.success) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Registration failed: ${response.message}", Toast.LENGTH_LONG).show()
        }
    }
}