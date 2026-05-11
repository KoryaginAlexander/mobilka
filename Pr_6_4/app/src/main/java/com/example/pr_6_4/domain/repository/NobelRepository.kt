package com.example.pr_6_4.domain.repository

import com.example.pr_6_4.domain.model.NobelLaureate

interface NobelRepository {
    suspend fun getLaureates(
        year: Int? = null,
        category: String? = null,
        limit: Int = 25,
        offset: Int = 0
    ): List<NobelLaureate>
}
