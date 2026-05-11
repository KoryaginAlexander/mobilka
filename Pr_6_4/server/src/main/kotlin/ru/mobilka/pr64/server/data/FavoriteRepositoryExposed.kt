package ru.mobilka.pr64.server.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import ru.mobilka.pr64.server.domain.repository.FavoriteRepository
import ru.mobilka.pr64.server.infrastructure.database.PrizesTable
import ru.mobilka.pr64.server.infrastructure.database.UserPrizesTable
import ru.mobilka.pr64.server.infrastructure.database.UsersTable
import ru.mobilka.pr64.server.presentation.dto.PrizeItemDto

class FavoriteRepositoryExposed(
    private val prizeRepository: PrizeRepositoryExposed,
) : FavoriteRepository {

    override fun listFavoritePrizeIds(userId: Int): List<Int> = transaction {
        UserPrizesTable.selectAll()
            .where { UserPrizesTable.userId eq EntityID(userId, UsersTable) }
            .map { it[UserPrizesTable.prizeId].value }
    }

    override fun listFavoritePrizes(userId: Int): List<PrizeItemDto> {
        val ids = listFavoritePrizeIds(userId).toSet()
        if (ids.isEmpty()) return emptyList()
        return prizeRepository.findAllDetailed().filter { it.id in ids }
    }

    override fun add(userId: Int, prizeId: Int): Boolean = transaction {
        val prizeExists = PrizesTable.selectAll()
            .where { PrizesTable.id eq prizeId }
            .limit(1)
            .count() > 0L
        if (!prizeExists) return@transaction false

        val already = UserPrizesTable.selectAll()
            .where {
                (UserPrizesTable.userId eq EntityID(userId, UsersTable)) and
                    (UserPrizesTable.prizeId eq EntityID(prizeId, PrizesTable))
            }
            .limit(1)
            .count() > 0L
        if (already) return@transaction true

        UserPrizesTable.insert {
            it[UserPrizesTable.userId] = EntityID(userId, UsersTable)
            it[UserPrizesTable.prizeId] = EntityID(prizeId, PrizesTable)
            it[UserPrizesTable.addedAt] = System.currentTimeMillis()
        }
        true
    }

    override fun remove(userId: Int, prizeId: Int): Boolean = transaction {
        val removed = UserPrizesTable.deleteWhere {
            (UserPrizesTable.userId eq EntityID(userId, UsersTable)) and
                (UserPrizesTable.prizeId eq EntityID(prizeId, PrizesTable))
        }
        removed > 0
    }
}
