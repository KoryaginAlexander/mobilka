package ru.mobilka.pr64.server.presentation.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import ru.mobilka.pr64.server.infrastructure.auth.JwtService

private const val JwtAuthName = "auth-jwt"

fun Application.configureJwtAuthentication(jwtService: JwtService) {
    install(Authentication) {
        jwt(JwtAuthName) {
            realm = "Nobel Prize Mock API"
            verifier(jwtService.verifier())
            validate { credential ->
                val sub = credential.payload.subject
                val username = credential.payload.getClaim("username").asString()
                val userId = sub?.toIntOrNull()
                if (userId != null && !username.isNullOrBlank()) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
