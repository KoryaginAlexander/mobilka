package com.example.pr_6_3.data.remote

import com.example.pr_6_3.data.local.TokenDataStore
import com.example.pr_6_3.data.remote.dto.LoginRequestDto
import com.example.pr_6_3.data.remote.dto.LoginResponseDto
import com.example.pr_6_3.data.remote.dto.UserDto
import com.example.pr_6_3.data.remote.dto.UsersResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL = "https://dummyjson.com"

class ApiService(private val tokenDataStore: TokenDataStore) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
        install(Logging) {
            logger = Logger.ANDROID
            level = LogLevel.HEADERS
        }
        // Отключаем автоматический выброс исключений для non-2xx ответов,
        // чтобы самостоятельно читать статус ПЕРЕД десериализацией тела
        expectSuccess = false
    }

    suspend fun login(username: String, password: String): LoginResponseDto {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(username, password))
        }
        if (!response.status.isSuccess()) {
            throw when (response.status.value) {
                400, 401 -> Exception("Неверные данные")
                else -> Exception("Ошибка сервера: ${response.status.value}")
            }
        }
        return response.body()
    }

    suspend fun getUsers(): UsersResponseDto {
        val token = tokenDataStore.getToken().orEmpty()
        val response = client.get("$BASE_URL/users") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (!response.status.isSuccess()) {
            throw when (response.status.value) {
                401 -> Exception("Ошибка авторизации. Войдите снова.")
                else -> Exception("Ошибка сервера: ${response.status.value}")
            }
        }
        return response.body()
    }

    suspend fun getUserById(id: Int): UserDto {
        val token = tokenDataStore.getToken().orEmpty()
        val response = client.get("$BASE_URL/users/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        if (!response.status.isSuccess()) {
            throw when (response.status.value) {
                401 -> Exception("Ошибка авторизации. Войдите снова.")
                404 -> Exception("Пользователь не найден")
                else -> Exception("Ошибка сервера: ${response.status.value}")
            }
        }
        return response.body()
    }
}
