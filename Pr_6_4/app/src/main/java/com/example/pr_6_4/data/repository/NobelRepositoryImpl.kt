package com.example.pr_6_4.data.repository

import com.example.pr_6_4.data.remote.api.NobelApiService
import com.example.pr_6_4.data.remote.dto.LaureateDto
import com.example.pr_6_4.data.remote.dto.NobelPrizeDto
import com.example.pr_6_4.domain.model.NobelLaureate
import com.example.pr_6_4.domain.repository.NobelRepository

class NobelRepositoryImpl(private val api: NobelApiService) : NobelRepository {

    override suspend fun getLaureates(
        year: Int?,
        category: String?,
        limit: Int,
        offset: Int
    ): List<NobelLaureate> {
        val response = api.getNobelPrizes(year, category, limit, offset)
        val filteredPrizes = response.prizes
            .asSequence()
            .filter { prize -> year == null || prize.awardYear == year }
            .filter { prize -> category.isNullOrBlank() || prize.category.equals(category, ignoreCase = true) }
            .drop(offset)
            .take(limit)
            .toList()

        return filteredPrizes.flatMap { prize ->
            if (prize.laureates.isNotEmpty()) {
                prize.laureates.map { laureate -> laureate.toDomain(prize) }
            } else {
                listOf(
                    NobelLaureate(
                        id = "${prize.id}_single",
                        fullName = prize.fullName,
                        year = prize.awardYear.toString(),
                        category = prize.category,
                        motivation = prize.motivation.orEmpty(),
                        birthCountry = "",
                        birthCity = "",
                        portraitUrl = null
                    )
                )
            }
        }
    }

    private fun LaureateDto.toDomain(prize: NobelPrizeDto): NobelLaureate {
        return NobelLaureate(
            id = "${prize.id}_$id",
            fullName = fullName,
            year = prize.awardYear.toString(),
            category = prize.category,
            motivation = motivation.orEmpty(),
            birthCountry = "",
            birthCity = "",
            portraitUrl = portraitUrl
        )
    }
}
