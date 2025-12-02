package com.example.fitness_kzo

import java.io.Serializable

data class SaveWeightliftingRequest(
    val user_id: Int,
    val duration_minutes: Float,
    val sets: Int,
    val reps_per_set: Int,
    val weight_kg_per_rep: Float,
    val calorie_burned_kcal: Float
)

