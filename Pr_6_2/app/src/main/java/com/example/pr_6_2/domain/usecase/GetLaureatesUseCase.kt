package com.example.pr_6_2.domain.usecase

import com.example.pr_6_2.domain.model.NobelLaureate
import com.example.pr_6_2.domain.repository.NobelRepository

class GetLaureatesUseCase(private val repository: NobelRepository) {
    suspend operator fun invoke(
        year: Int? = null,
        category: String? = null
    ): List<NobelLaureate> = repository.getLaureates(year = year, category = category)
}
