package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.model.Laureate
import ru.mobilka.pr64.server.domain.repository.NobelPrizeRepository

class GetLaureatesUseCase(
    private val repository: NobelPrizeRepository,
) {
    operator fun invoke(year: String, categorySlug: String): List<Laureate> =
        repository.getLaureates(year, categorySlug)
}
