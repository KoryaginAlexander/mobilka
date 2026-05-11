package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.repository.UserRepository
import ru.mobilka.pr64.server.infrastructure.auth.JwtService
import ru.mobilka.pr64.server.infrastructure.security.PasswordHasher

class LoginUseCase(
    private val users: UserRepository,
    private val jwtService: JwtService,
) {
    fun execute(username: String, password: String): String? {
        val user = users.findByUsername(username.trim()) ?: return null
        if (!PasswordHasher.verify(password, user.passwordHash)) return null
        return jwtService.generateToken(user.id, user.username, user.role)
    }
}
