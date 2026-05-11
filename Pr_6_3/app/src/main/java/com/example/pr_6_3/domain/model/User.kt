package com.example.pr_6_3.domain.model

data class User(
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
