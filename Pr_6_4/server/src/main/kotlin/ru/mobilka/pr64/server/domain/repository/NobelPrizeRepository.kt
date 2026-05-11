package ru.mobilka.pr64.server.domain.repository

import ru.mobilka.pr64.server.domain.model.Laureate
import ru.mobilka.pr64.server.domain.model.NobelPrize

interface NobelPrizeRepository {
    fun getAll(): List<NobelPrize>

    fun getByYearAndCategory(year: String, categorySlug: String): NobelPrize?

    fun getLaureates(year: String, categorySlug: String): List<Laureate>
}
