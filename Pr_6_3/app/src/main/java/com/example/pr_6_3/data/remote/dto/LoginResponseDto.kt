package com.example.pr_6_3.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val gender: String = "",
    val image: String = "",
    val accessToken: String,
    val refreshToken: String = ""
)
