package com.example.pr_6_3.domain.usecase

import com.example.pr_6_3.domain.model.User
import com.example.pr_6_3.domain.repository.UserRepository

class GetUserDetailUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(id: Int): Result<User> = repository.getUserById(id)
}
