package ru.mobilka.pr64.server.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
)

@Serializable
data class LoginResponse(
    val token: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 1800,
)

@Serializable
data class RegisterRequest(
    val username: String,
    val password: String,
)

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null,
)
