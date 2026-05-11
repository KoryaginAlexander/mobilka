package ru.mobilka.pr64.server.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.coroutines.delay
import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.infrastructure.config.ServerEnv

class NobelRemoteClient(
    private val http: HttpClient,
) {
    suspend fun fetchAllNobelPrizes(): List<NobelPrize> {
        val allLaureates = mutableListOf<LaureateWithPrizesDto>()
        var offset = 0
        val limit = (ServerEnv.get("NOBEL_PAGE_LIMIT")?.toIntOrNull() ?: 50).coerceAtLeast(1)
        var safetyPageCounter = 0
        while (true) {
            if (++safetyPageCounter > 1_000) {
                throw IllegalStateException("Too many Nobel API pages while syncing prizes")
            }
            val env = fetchLaureatesPageWithRetry(offset, limit)

            if (env.laureates.isEmpty()) break

            allLaureates += env.laureates

            if (env.laureates.size < limit) break

            val total = env.meta?.count
            if (total != null && allLaureates.size >= total) break

            offset += limit
        }

        return mergeLaureatesResponseToNobelPrizes(
            LaureatesApiEnvelope(laureates = allLaureates, meta = null),
        )
    }

    private suspend fun fetchLaureatesPageWithRetry(offset: Int, limit: Int): LaureatesApiEnvelope {
        var attempt = 0
        var lastError: Throwable? = null
        while (attempt < 3) {
            try {
                return http.get(LAUREATES_ENDPOINT) {
                    parameter("format", "json")
                    parameter("limit", limit)
                    parameter("offset", offset)
                }.body<LaureatesApiEnvelope>()
            } catch (t: Throwable) {
                lastError = t
                attempt += 1
                if (attempt >= 3) break
                delay(1000L * attempt)
            }
        }
        throw IllegalStateException("Failed to fetch laureates page offset=$offset limit=$limit", lastError)
    }

    companion object {
        private const val LAUREATES_ENDPOINT = "https://api.nobelprize.org/2.1/laureates"
    }
}
