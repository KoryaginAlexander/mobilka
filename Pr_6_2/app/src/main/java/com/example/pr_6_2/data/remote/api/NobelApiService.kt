package com.example.pr_6_2.data.remote.api

import com.example.pr_6_2.data.remote.dto.NobelPrizesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class NobelApiService(private val client: HttpClient) {

    suspend fun getNobelPrizes(
        year: Int? = null,
        category: String? = null,
        limit: Int = 25,
        offset: Int = 0
    ): NobelPrizesResponse {
        return client.get("https://api.nobelprize.org/2.1/nobelPrizes") {
            parameter("limit", limit)
            parameter("offset", offset)
            year?.let { parameter("nobelPrizeYear", it) }
            category?.let { parameter("nobelPrizeCategory", it) }
        }.body()
    }
}
