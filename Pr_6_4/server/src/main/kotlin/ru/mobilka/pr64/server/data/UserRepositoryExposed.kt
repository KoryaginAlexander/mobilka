package ru.mobilka.pr64.server.data

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.mobilka.pr64.server.domain.repository.UserRecord
import ru.mobilka.pr64.server.domain.repository.UserRepository
import ru.mobilka.pr64.server.infrastructure.database.UsersTable

class UserRepositoryExposed : UserRepository {

    override fun findByUsername(username: String): UserRecord? = transaction {
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .limit(1)
            .map { row ->
                UserRecord(
                    id = row[UsersTable.id].value,
                    username = row[UsersTable.username],
                    passwordHash = row[UsersTable.passwordHash],
                    role = row[UsersTable.role],
                )
            }
            .singleOrNull()
    }

    override fun usernameExists(username: String): Boolean = transaction {
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .limit(1)
            .count() > 0L
    }

    override fun create(username: String, passwordHash: String, role: String): Int = transaction {
        UsersTable.insert {
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role
        }
        UsersTable.selectAll()
            .where { UsersTable.username eq username }
            .single()[UsersTable.id]
            .value
    }
}
