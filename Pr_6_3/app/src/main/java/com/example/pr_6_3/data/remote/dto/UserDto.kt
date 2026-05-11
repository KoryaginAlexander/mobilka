package com.example.pr_6_3.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val image: String,
    val phone: String = "",
    val age: Int = 0,
    val role: String = ""
)
