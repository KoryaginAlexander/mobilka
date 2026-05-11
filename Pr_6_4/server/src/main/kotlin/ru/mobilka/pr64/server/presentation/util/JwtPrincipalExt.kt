package ru.mobilka.pr64.server.presentation.util

import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

fun JWTPrincipal.userId(): Int =
    payload.subject?.toIntOrNull() ?: error("JWT без числового subject")
