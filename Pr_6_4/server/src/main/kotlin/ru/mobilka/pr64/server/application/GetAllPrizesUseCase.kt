package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.model.NobelPrizesEnvelope
import ru.mobilka.pr64.server.domain.repository.NobelPrizeRepository

class GetAllPrizesUseCase(
    private val repository: NobelPrizeRepository,
) {
    operator fun invoke(): NobelPrizesEnvelope =
        NobelPrizesEnvelope(repository.getAll())
}
