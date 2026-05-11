package com.example.pr_6_3.domain.repository

import com.example.pr_6_3.domain.model.User

interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User>
}
