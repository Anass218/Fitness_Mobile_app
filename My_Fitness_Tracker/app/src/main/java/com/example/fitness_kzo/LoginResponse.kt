package com.example.fitness_kzo

data class LoginResponse(
    val status: String,
    val token: String? = null,
    val user: UserData? = null,
    val message: String? = null
)
