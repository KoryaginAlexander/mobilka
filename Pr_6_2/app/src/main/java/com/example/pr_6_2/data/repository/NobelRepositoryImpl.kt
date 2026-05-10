package com.example.pr_6_2.data.repository

import com.example.pr_6_2.data.remote.api.NobelApiService
import com.example.pr_6_2.data.remote.dto.LaureateDto
import com.example.pr_6_2.data.remote.dto.NobelPrizeDto
import com.example.pr_6_2.domain.model.NobelLaureate
import com.example.pr_6_2.domain.repository.NobelRepository

class NobelRepositoryImpl(private val api: NobelApiService) : NobelRepository {

    override suspend fun getLaureates(
        year: Int?,
        category: String?,
        limit: Int,
        offset: Int
    ): List<NobelLaureate> {
        val response = api.getNobelPrizes(year, category, limit, offset)
        return response.nobelPrizes.flatMap { prize ->
            prize.laureates?.map { laureate ->
                laureate.toDomain(prize)
            } ?: emptyList()
        }
    }

    private fun LaureateDto.toDomain(prize: NobelPrizeDto): NobelLaureate {
        val portraitUrl = links
            ?.firstOrNull { it.rel == "laureate" && it.types.contains("image") }
            ?.href

        return NobelLaureate(
            id = "${prize.awardYear}_${prize.category?.en}_$id",
            fullName = fullName?.en ?: "Unknown",
            year = prize.awardYear,
            category = prize.category?.en ?: "",
            motivation = motivation?.en ?: "",
            birthCountry = birth?.place?.country?.en ?: "",
            birthCity = birth?.place?.city?.en ?: "",
            portraitUrl = portraitUrl
        )
    }
}
