package com.example.pr_6_3.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class UsersResponseDto(
    val users: List<UserDto>,
    val total: Int,
    val skip: Int,
    val limit: Int
)
