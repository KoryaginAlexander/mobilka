package com.example.pr_6_4.domain.usecase

import com.example.pr_6_4.domain.model.NobelLaureate
import com.example.pr_6_4.domain.repository.NobelRepository

class GetLaureatesUseCase(private val repository: NobelRepository) {
    suspend operator fun invoke(
        year: Int? = null,
        category: String? = null
    ): List<NobelLaureate> = repository.getLaureates(year = year, category = category)
}
