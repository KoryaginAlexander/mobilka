package com.example.pr_6_4.data.remote.api

import com.example.pr_6_4.BuildConfig
import com.example.pr_6_4.data.remote.dto.NobelPrizesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class NobelApiService(private val client: HttpClient) {

    suspend fun getNobelPrizes(
        year: Int? = null,
        category: String? = null,
        limit: Int = 25,
        offset: Int = 0
    ): NobelPrizesResponse {
        return client.get("${BuildConfig.SERVER_BASE_URL}/prizes").body()
    }
}
