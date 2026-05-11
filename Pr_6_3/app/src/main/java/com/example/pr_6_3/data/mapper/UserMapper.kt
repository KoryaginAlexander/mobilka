package com.example.pr_6_3.data.mapper

import com.example.pr_6_3.data.remote.dto.UserDto
import com.example.pr_6_3.domain.model.User

fun UserDto.toDomain() = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    username = username,
    email = email,
    image = image,
    phone = phone,
    age = age,
    role = role
)
