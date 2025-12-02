package com.example.fitness_kzo

data class UserData(
    val id: Int,
    val name: String,
    val email: String,
    val height: Double,
    val weight: Double,
    val targetCalories: Int,
    val profileImageUrl: String? = null
)
