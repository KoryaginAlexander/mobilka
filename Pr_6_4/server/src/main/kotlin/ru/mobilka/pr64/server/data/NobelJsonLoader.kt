package ru.mobilka.pr64.server.data

import kotlinx.serialization.json.Json
import kotlin.text.Charsets
import ru.mobilka.pr64.server.domain.model.NobelPrize
import ru.mobilka.pr64.server.domain.model.NobelPrizesEnvelope

object NobelJsonLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun loadFromResource(path: String = "/data/nobel-prizes.json"): List<NobelPrize> {
        val stream = checkNotNull(javaClass.getResourceAsStream(path)) {
            "Resource not found: $path"
        }
        return stream.bufferedReader(Charsets.UTF_8).use { reader ->
            json.decodeFromString<NobelPrizesEnvelope>(reader.readText()).nobelPrizes
        }
    }
}
