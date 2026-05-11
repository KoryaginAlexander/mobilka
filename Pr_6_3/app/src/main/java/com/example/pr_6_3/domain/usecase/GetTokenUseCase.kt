package com.example.pr_6_3.domain.usecase

import com.example.pr_6_3.domain.repository.AuthRepository

class GetTokenUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): String? = repository.getToken()
}
