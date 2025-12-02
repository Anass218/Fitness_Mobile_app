package com.example.fitness_kzo

data class SaveSwimmingRequest(
    val user_id: Int,
    val duration_minutes: Double,
    val stroke_type: String,
    val laps: Int,
    val calorie_burned_kcal: Double
)
