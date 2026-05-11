package com.example.pr_6_3.domain.usecase

import com.example.pr_6_3.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<String> =
        repository.login(username, password)
}
