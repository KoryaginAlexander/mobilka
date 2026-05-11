package ru.mobilka.pr64.server.application

import ru.mobilka.pr64.server.domain.repository.FavoriteRepository
import ru.mobilka.pr64.server.domain.repository.PrizeRepository
import ru.mobilka.pr64.server.presentation.dto.PrizeItemDto

class FavoritesUseCase(
    private val favorites: FavoriteRepository,
    private val prizes: PrizeRepository,
) {
    fun list(userId: Int): List<PrizeItemDto> =
        favorites.listFavoritePrizes(userId)

    fun add(userId: Int, prizeId: Int): FavoriteMutationResult {
        if (!prizes.existsById(prizeId)) return FavoriteMutationResult.PrizeNotFound
        val ok = favorites.add(userId, prizeId)
        return if (ok) FavoriteMutationResult.Ok else FavoriteMutationResult.AlreadyAdded
    }

    fun remove(userId: Int, prizeId: Int): FavoriteMutationResult {
        val ok = favorites.remove(userId, prizeId)
        return if (ok) FavoriteMutationResult.Ok else FavoriteMutationResult.NotFavorite
    }

    enum class FavoriteMutationResult {
        Ok,
        PrizeNotFound,
        AlreadyAdded,
        NotFavorite,
    }
}
