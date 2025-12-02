package com.example.fitness_kzo

import java.io.Serializable

data class SaveYogaRequest(
    val user_id: Int,
    val duration_minutes: Float,
    val intensity_level: String,
    val calorie_burned_kcal: Float
) : Serializable
