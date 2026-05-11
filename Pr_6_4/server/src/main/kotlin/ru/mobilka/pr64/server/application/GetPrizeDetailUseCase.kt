package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.domain.repository.NobelPrizeRepository

class GetPrizeDetailUseCase(
    private val repository: NobelPrizeRepository,
) {
    operator fun invoke(year: String, categorySlug: String): NobelPrize? =
        repository.getByYearAndCategory(year, categorySlug)
}
