package com.example.fitness_kzo

import java.io.Serializable

data class SaveYogaResponse(
    val success: Boolean,
    val message: String
) : Serializable
