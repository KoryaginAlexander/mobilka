package ru.mobilka.pr64.server.data

import ru.mobilka.pr64.server.domain.categorySlug
import ru.mobilka.pr64.server.domain.model.Laureate
import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.domain.repository.NobelPrizeRepository

class InMemoryNobelPrizeRepository(
    prizes: List<NobelPrize>,
) : NobelPrizeRepository {

    private val all: List<NobelPrize> = prizes.sortedWith(
        compareBy<NobelPrize> { it.awardYear }.thenBy { it.categorySlug() },
    )

    private val byYearCategory: Map<Pair<String, String>, NobelPrize> =
        prizes.associateBy { prize ->
            prize.awardYear to prize.categorySlug().lowercase()
        }

    override fun getAll(): List<NobelPrize> = all

    override fun getByYearAndCategory(year: String, categorySlug: String): NobelPrize? =
        byYearCategory[year to categorySlug.lowercase()]

    override fun getLaureates(year: String, categorySlug: String): List<Laureate> =
        getByYearAndCategory(year, categorySlug)?.laureates.orEmpty()
}
