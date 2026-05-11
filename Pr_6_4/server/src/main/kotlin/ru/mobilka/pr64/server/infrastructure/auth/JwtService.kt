package ru.mobilka.pr64.server.infrastructure.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtService(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val validitySeconds: Long = 30 * 60,
) {
    init {
        require(secret.length >= 32) {
            "JWT secret must be at least 32 characters (set JWT_SECRET)."
        }
    }

    private val algorithm: Algorithm = Algorithm.HMAC256(secret)

    fun verifier(): JWTVerifier =
        JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()

    fun generateToken(username: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withIssuedAt(Date(now))
            .withExpiresAt(Date(now + validitySeconds * 1000))
            .sign(algorithm)
    }
}
