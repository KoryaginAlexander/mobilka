package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.repository.UserRepository
import ru.mobilka.pr64.server.infrastructure.security.PasswordHasher

class RegisterUseCase(
    private val users: UserRepository,
) {
    fun execute(username: String, password: String): RegisterResult {
        val u = username.trim()
        if (u.length < 3 || password.length < 8) {
            return RegisterResult.ValidationError
        }
        if (users.usernameExists(u)) {
            return RegisterResult.UsernameTaken
        }
        val hash = PasswordHasher.hash(password)
        users.create(u, hash, role = "USER")
        return RegisterResult.Created
    }

    sealed interface RegisterResult {
        data object Created : RegisterResult

        data object UsernameTaken : RegisterResult

        data object ValidationError : RegisterResult
    }
}
