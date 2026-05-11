package ru.mobilka.pr64.server.infrastructure.database

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.mobilka.pr64.server.infrastructure.config.ServerEnv
import ru.mobilka.pr64.server.infrastructure.security.PasswordHasher

object DatabaseFactory {
    fun connectAndMigrate() {
        val jdbcUrl = resolveJdbcUrlFromEnv()
        val ds = HikariDataSource().apply {
            this.jdbcUrl = jdbcUrl
            // Neon: не держать соединения дольше, чем может жить compute / proxy
            maximumPoolSize = ServerEnv.get("HIKARI_MAX_POOL")?.toIntOrNull() ?: 5
            maxLifetime = ServerEnv.get("HIKARI_MAX_LIFETIME_MS")?.toLongOrNull() ?: 120_000L
            idleTimeout = 60_000L
            connectionTimeout = 60_000L
            keepaliveTime = 30_000L
        }
        Database.connect(ds)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                PrizesTable,
                LaureatesTable,
                UserPrizesTable,
                AppMetadataTable,
            )
        }

        seedBootstrapAdminIfEmpty()
    }

    private fun seedBootstrapAdminIfEmpty() {
        transaction {
            val anyUser = UsersTable.selectAll().limit(1).firstOrNull()
            if (anyUser != null) return@transaction

            val user = ServerEnv.get("BOOTSTRAP_USER") ?: "admin"
            val pass = ServerEnv.get("BOOTSTRAP_PASSWORD") ?: "nobel-admin-secret-min-length!!"
            UsersTable.insert {
                it[UsersTable.username] = user
                it[UsersTable.passwordHash] = PasswordHasher.hash(pass)
                it[UsersTable.role] = "ADMIN"
            }
        }
    }
}
