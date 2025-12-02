package com.example.fitness_kzo

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fitness_kzo.databinding.ActivityMainframeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainframeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainframeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainframeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Fitness App"

        setupSystemBarInsets()
        setupBottomNavigation()
    }

    private fun setupSystemBarInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment.newInstance())
                    true
                }
                R.id.nav_target -> {
                    replaceFragment(MytargetFragment.newInstance())
                    true
                }
                R.id.nav_record -> {
                    replaceFragment(Record.newInstance())
                    true
                }
                else -> false
            }
        }
        binding.bottomNavigation.selectedItemId = R.id.nav_record
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_logout) {
            logoutUser()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)
        val userId = prefs.getInt("USER_ID", 0)

        if (!token.isNullOrEmpty() && userId != 0) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    RetrofitClient.apiService.logoutUser(LogoutRequest(userId, token))
                } catch (e: Exception) {
                    // Log error but proceed with local logout
                }
                withContext(Dispatchers.Main) {
                    completeLogout()
                }
            }
        } else {
            completeLogout()
        }
    }

    private fun completeLogout() {
        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, login::class.java))
        finish()
    }

    override fun onBackPressed() {
        if (binding.bottomNavigation.selectedItemId != R.id.nav_record) {
            binding.bottomNavigation.selectedItemId = R.id.nav_record
        } else {
            super.onBackPressed()
        }
    }
}