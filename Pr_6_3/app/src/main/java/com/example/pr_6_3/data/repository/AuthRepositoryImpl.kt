package com.example.pr_6_3.data.repository

import com.example.pr_6_3.data.local.TokenDataStore
import com.example.pr_6_3.data.remote.ApiService
import com.example.pr_6_3.domain.repository.AuthRepository
import java.net.UnknownHostException

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val tokenDataStore: TokenDataStore
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<String> = try {
        val response = apiService.login(username, password)
        tokenDataStore.saveToken(response.accessToken)
        Result.success(response.accessToken)
    } catch (e: UnknownHostException) {
        Result.failure(Exception("Нет соединения с интернетом"))
    } catch (e: Exception) {
        Result.failure(Exception(e.message ?: "Ошибка входа"))
    }

    override suspend fun logout() {
        tokenDataStore.clearToken()
    }

    override suspend fun getToken(): String? = tokenDataStore.getToken()
}
