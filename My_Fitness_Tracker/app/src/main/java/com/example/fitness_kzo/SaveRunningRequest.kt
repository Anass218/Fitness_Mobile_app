package com.example.fitness_kzo

data class SaveRunningRequest(
    val user_id: Int,
    val duration_minutes: Float,
    val distance_km: Double,
    val calorie_burned_kcal: Int
)
