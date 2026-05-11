package com.example.pr_6_3.domain.repository

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<String>
    suspend fun logout()
    suspend fun getToken(): String?
}
