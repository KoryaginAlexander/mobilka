package ru.mobilka.pr64.server.domain.repository

import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.presentation.dto.PrizeItemDto

data class UserRecord(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val role: String,
)

interface UserRepository {
    fun findByUsername(username: String): UserRecord?

    fun usernameExists(username: String): Boolean

    fun create(username: String, passwordHash: String, role: String): Int
}

interface PrizeRepository {
    fun upsertFromNobelApi(prizes: List<NobelPrize>)

    fun findAllDetailed(): List<PrizeItemDto>

    fun existsById(id: Int): Boolean

    fun lastRemoteSyncMillis(): Long?

    fun markSyncedNow()
}

interface FavoriteRepository {
    fun listFavoritePrizeIds(userId: Int): List<Int>

    fun listFavoritePrizes(userId: Int): List<PrizeItemDto>

    fun add(userId: Int, prizeId: Int): Boolean

    fun remove(userId: Int, prizeId: Int): Boolean
}
