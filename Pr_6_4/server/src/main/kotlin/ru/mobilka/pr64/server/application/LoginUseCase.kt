package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.infrastructure.auth.JwtService

class LoginUseCase(
    private val jwtService: JwtService,
    private val users: Map<String, String>,
) {
    fun execute(username: String, password: String): String? {
        val expected = users[username] ?: return null
        return if (expected == password) jwtService.generateToken(username) else null
    }
}
