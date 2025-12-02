package com.example.fitness_kzo

import android.content.Context
import com.google.gson.Gson

class SharedPrefManager(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUser(user: UserData) {
        val editor = sharedPreferences.edit()
        editor.putString("user", gson.toJson(user))
        editor.apply()
    }

    fun getUser(): UserData? {
        val userJson = sharedPreferences.getString("user", null)
        return if (userJson != null) {
            gson.fromJson(userJson, UserData::class.java)
        } else {
            null
        }
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}