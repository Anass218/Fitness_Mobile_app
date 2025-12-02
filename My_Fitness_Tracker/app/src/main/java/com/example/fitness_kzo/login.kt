package com.example.fitness_kzo

import SessionManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fitness_kzo.databinding.ActivityLoginBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager  // Add SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Initialize SessionManager
        sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle Sign Up link click
        binding.signUpText.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        // Handle Login button click
        binding.loginButton.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        // Handle Forgot Password
        binding.forgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Valid email required"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    private fun loginUser() {
        val request = LoginRequest(
            email = binding.emailEditText.text.toString().trim(),
            password = binding.passwordEditText.text.toString().trim()
        )

        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.loginUser(request)
                withContext(Dispatchers.Main) {
                    handleLoginResponse(response)
                }
            } catch (e: java.net.ConnectException) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(
                        this@login,
                        "Connection error: Please check your internet",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: java.net.SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(
                        this@login,
                        "Connection timeout: Server took too long to respond",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(
                        this@login,
                        "Error: ${e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun handleLoginResponse(response: LoginResponse) {
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true

        if (response.status == "success" && response.token != null && response.user != null) {
            // Save user data to SessionManager
            saveUserData(response)

            // Save auth token to SessionManager
            sessionManager.saveAuthToken(response.token)

            // Redirect to MainActivity
            startActivity(Intent(this, MainframeActivity::class.java))
            finish() // Close login activity
        } else {
            val errorMessage = response.message ?: "Invalid credentials"
            Toast.makeText(this, "Login failed: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUserData(response: LoginResponse) {
        // Save user data to SessionManager
        sessionManager.saveUserId(response.user?.id ?: 0)
        sessionManager.saveUserEmail(response.user?.email ?: "")
        sessionManager.saveUserName(response.user?.name ?: "")
        sessionManager.saveUserHeight(response.user?.height?.toFloat() ?: 0f)
        sessionManager.saveUserWeight(response.user?.weight?.toFloat() ?: 0f)
    }
}