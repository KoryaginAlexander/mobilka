package ru.mobilka.pr64.server.infrastructure.security

import at.favre.lib.crypto.bcrypt.BCrypt

object PasswordHasher {
    private val hasher = BCrypt.withDefaults()

    fun hash(rawPassword: String): String =
        hasher.hashToString(12, rawPassword.toCharArray())

    fun verify(rawPassword: String, hash: String): Boolean =
        BCrypt.verifyer().verify(rawPassword.toCharArray(), hash).verified
}
