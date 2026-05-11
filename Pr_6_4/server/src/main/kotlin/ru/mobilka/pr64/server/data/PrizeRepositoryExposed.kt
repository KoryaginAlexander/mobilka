package ru.mobilka.pr64.server.data

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import ru.mobilka.pr64.server.domain.categorySlug
import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.domain.repository.PrizeRepository
import ru.mobilka.pr64.server.infrastructure.database.AppMetadataTable
import ru.mobilka.pr64.server.infrastructure.database.LaureatesTable
import ru.mobilka.pr64.server.infrastructure.database.PrizesTable
import ru.mobilka.pr64.server.presentation.dto.PrizeItemDto
import ru.mobilka.pr64.server.presentation.dto.PrizeLaureateDto

private const val META_LAST_SYNC = "last_nobel_sync_ms"

class PrizeRepositoryExposed : PrizeRepository {

    override fun upsertFromNobelApi(prizes: List<NobelPrize>) {
        // По одной премии на транзакцию — длинная одна транзакция на весь каталог рвёт Neon (connection reset).
        for (p in prizes) {
            transaction {
                val year = p.awardYear.toInt()
                val slug = p.categorySlug()
                val detailLink =
                    p.links.firstOrNull { it.rel == "nobelPrize" }?.href
                        ?: p.links.firstOrNull()?.href
                val fullName =
                    p.categoryFullName.en ?: p.category.en ?: ""
                val motivation = p.laureates.firstOrNull()?.motivation?.en

                val existing = PrizesTable.selectAll()
                    .where {
                        (PrizesTable.awardYear eq year) and (PrizesTable.category eq slug)
                    }
                    .limit(1)
                    .singleOrNull()

                val prizeId = if (existing == null) {
                    PrizesTable.insert {
                        it[PrizesTable.awardYear] = year
                        it[PrizesTable.category] = slug
                        it[PrizesTable.fullName] = fullName
                        it[PrizesTable.motivation] = motivation
                        it[PrizesTable.detailLink] = detailLink
                    }
                    PrizesTable.selectAll()
                        .where {
                            (PrizesTable.awardYear eq year) and (PrizesTable.category eq slug)
                        }
                        .single()[PrizesTable.id]
                        .value
                } else {
                    val id = existing[PrizesTable.id].value
                    PrizesTable.update({ PrizesTable.id eq id }) {
                        it[PrizesTable.fullName] = fullName
                        it[PrizesTable.motivation] = motivation
                        it[PrizesTable.detailLink] = detailLink
                    }
                    id
                }

                LaureatesTable.deleteWhere {
                    LaureatesTable.prizeId eq EntityID(prizeId, PrizesTable)
                }

                for (l in p.laureates.sortedBy { it.sortOrder.orEmpty() }) {
                    LaureatesTable.insert {
                        it[LaureatesTable.prizeId] = EntityID(prizeId, PrizesTable)
                        it[LaureatesTable.fullName] =
                            listOfNotNull(
                                l.fullName?.en,
                                l.knownName.en,
                                l.knownName.no,
                                l.knownName.se,
                            ).firstOrNull().orEmpty()
                        it[LaureatesTable.portion] = l.portion
                        it[LaureatesTable.motivation] = l.motivation?.en
                        it[LaureatesTable.portraitUrl] = null
                    }
                }
            }
        }
    }

    override fun findAllDetailed(): List<PrizeItemDto> = transaction {
        val rows = PrizesTable.selectAll()
            .orderBy(PrizesTable.awardYear, SortOrder.ASC)
            .orderBy(PrizesTable.category, SortOrder.ASC)

        rows.map { prow ->
            val prizeId = prow[PrizesTable.id].value
            val laureates = LaureatesTable.selectAll()
                .where { LaureatesTable.prizeId eq EntityID(prizeId, PrizesTable) }
                .orderBy(LaureatesTable.id, SortOrder.ASC)
                .map { l ->
                    PrizeLaureateDto(
                        id = l[LaureatesTable.id].value,
                        fullName = l[LaureatesTable.fullName],
                        portion = l[LaureatesTable.portion],
                        motivation = l[LaureatesTable.motivation],
                        portraitUrl = l[LaureatesTable.portraitUrl],
                    )
                }

            PrizeItemDto(
                id = prizeId,
                awardYear = prow[PrizesTable.awardYear],
                category = prow[PrizesTable.category],
                fullName = prow[PrizesTable.fullName],
                motivation = prow[PrizesTable.motivation],
                detailLink = prow[PrizesTable.detailLink],
                laureates = laureates,
            )
        }
    }

    override fun existsById(id: Int): Boolean = transaction {
        PrizesTable.selectAll()
            .where { PrizesTable.id eq id }
            .limit(1)
            .count() > 0L
    }

    override fun lastRemoteSyncMillis(): Long? = transaction {
        AppMetadataTable.selectAll()
            .where { AppMetadataTable.key eq META_LAST_SYNC }
            .limit(1)
            .map { it[AppMetadataTable.value].toLong() }
            .singleOrNull()
    }

    override fun markSyncedNow() {
        transaction {
            AppMetadataTable.deleteWhere { AppMetadataTable.key eq META_LAST_SYNC }
            AppMetadataTable.insert {
                it[key] = META_LAST_SYNC
                it[value] = System.currentTimeMillis().toString()
            }
        }
    }
}
