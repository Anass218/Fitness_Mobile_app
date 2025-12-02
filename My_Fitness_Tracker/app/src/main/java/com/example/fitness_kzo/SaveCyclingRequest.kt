package com.example.fitness_kzo

data class SaveCyclingRequest(
    val user_id: Int,
    val duration_minutes: Float,
    val distance_km: Float,
    val speed_kmph: Float? = null,
    val calorie_burned_kcal: Float? = null
)
