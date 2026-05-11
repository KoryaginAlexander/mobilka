package com.example.pr_6_3.domain.usecase

import com.example.pr_6_3.domain.model.User
import com.example.pr_6_3.domain.repository.UserRepository

class GetUsersUseCase(private val repository: UserRepository) {
    suspend operator fun invoke(): Result<List<User>> = repository.getUsers()
}
