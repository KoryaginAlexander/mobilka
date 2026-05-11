package com.example.pr_6_3.data.repository

import com.example.pr_6_3.data.mapper.toDomain
import com.example.pr_6_3.data.remote.ApiService
import com.example.pr_6_3.domain.model.User
import com.example.pr_6_3.domain.repository.UserRepository
import java.net.UnknownHostException

class UserRepositoryImpl(private val apiService: ApiService) : UserRepository {

    override suspend fun getUsers(): Result<List<User>> = try {
        val response = apiService.getUsers()
        Result.success(response.users.map { it.toDomain() })
    } catch (e: UnknownHostException) {
        Result.failure(Exception("Нет соединения с интернетом"))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки"))
    }

    override suspend fun getUserById(id: Int): Result<User> = try {
        val dto = apiService.getUserById(id)
        Result.success(dto.toDomain())
    } catch (e: UnknownHostException) {
        Result.failure(Exception("Нет соединения с интернетом"))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка загрузки"))
    }
}
