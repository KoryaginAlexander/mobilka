package ru.mobilka.pr64.server.data

import kotlinx.serialization.Serializable
import ru.mobilka.pr64.server.domain.categorySlug
import ru.mobilka.pr64.server.domain.model.ApiLink
import ru.mobilka.pr64.server.domain.model.Laureate
import ru.mobilka.pr64.server.domain.model.LocalizedStrings
import ru.mobilka.pr64.server.domain.model.NobelListMeta
import ru.mobilka.pr64.server.domain.model.NobelPrize

/**
 * Ответ https://api.nobelprize.org/2.1/laureates — у каждого лауреата массив премий.
 */
@Serializable
data class LaureatesApiEnvelope(
    val laureates: List<LaureateWithPrizesDto> = emptyList(),
    val meta: NobelListMeta? = null,
)

@Serializable
data class LaureateWithPrizesDto(
    val id: String,
    val knownName: LocalizedStrings = LocalizedStrings(),
    val fullName: LocalizedStrings? = null,
    val links: List<ApiLink> = emptyList(),
    val nobelPrizes: List<NobelPrizeOnLaureateDto> = emptyList(),
)

@Serializable
data class NobelPrizeOnLaureateDto(
    val awardYear: String,
    val category: LocalizedStrings,
    val categoryFullName: LocalizedStrings,
    val dateAwarded: String? = null,
    val prizeAmount: Long? = null,
    val prizeAmountAdjusted: Long? = null,
    val sortOrder: String? = null,
    val portion: String? = null,
    val motivation: LocalizedStrings? = null,
    val links: List<ApiLink> = emptyList(),
)

private data class LaureatePrizeRow(
    val person: LaureateWithPrizesDto,
    val prize: NobelPrizeOnLaureateDto,
)

/**
 * Собирает уникальные премии (год + категория) и список лауреатов, как в /nobelPrizes.
 */
fun mergeLaureatesResponseToNobelPrizes(envelope: LaureatesApiEnvelope): List<NobelPrize> {
    val buckets = mutableMapOf<Pair<Int, String>, MutableList<LaureatePrizeRow>>()

    for (person in envelope.laureates) {
        for (np in person.nobelPrizes) {
            val key = prizeAggregateKey(np)
            buckets.getOrPut(key, ::mutableListOf).add(LaureatePrizeRow(person, np))
        }
    }

    return buckets.map { (_, rows) ->
        val p0 = rows.first().prize
        val laureates = rows
            .map { r ->
                Laureate(
                    id = r.person.id,
                    knownName = r.person.knownName,
                    fullName = r.person.fullName,
                    portion = r.prize.portion,
                    sortOrder = r.prize.sortOrder,
                    motivation = r.prize.motivation,
                    links = r.person.links,
                )
            }
            .distinctBy { it.id to "${it.sortOrder.orEmpty()}_${it.portion.orEmpty()}" }
            .sortedWith(compareBy({ it.sortOrder.orEmpty() }, { it.id }))

        NobelPrize(
            awardYear = p0.awardYear,
            category = p0.category,
            categoryFullName = p0.categoryFullName,
            dateAwarded = p0.dateAwarded,
            prizeAmount = p0.prizeAmount,
            prizeAmountAdjusted = p0.prizeAmountAdjusted,
            links = p0.links,
            laureates = laureates,
        )
    }
}

private fun prizeAggregateKey(np: NobelPrizeOnLaureateDto): Pair<Int, String> {
    val shell = NobelPrize(
        awardYear = np.awardYear,
        category = np.category,
        categoryFullName = np.categoryFullName,
        dateAwarded = np.dateAwarded,
        prizeAmount = np.prizeAmount,
        prizeAmountAdjusted = np.prizeAmountAdjusted,
        links = np.links,
        laureates = emptyList(),
    )
    return shell.awardYear.toInt() to shell.categorySlug()
}
