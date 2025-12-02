package com.example.fitness_kzo

data class RegistrationRequest(
    val username: String,
    val email: String,
    val password: String,
    val weight: Double,
    val height: Double
)