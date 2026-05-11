package ru.mobilka.pr64.server.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LocalizedStrings(
    val en: String? = null,
    val no: String? = null,
    val se: String? = null,
)

@Serializable
data class ApiLink(
    val rel: String,
    val href: String,
    val action: String? = null,
    val types: String? = null,
)

@Serializable
data class Laureate(
    val id: String,
    val knownName: LocalizedStrings,
    val fullName: LocalizedStrings? = null,
    val portion: String? = null,
    val sortOrder: String? = null,
    val motivation: LocalizedStrings? = null,
    val links: List<ApiLink> = emptyList(),
)

@Serializable
data class NobelPrize(
    val awardYear: String,
    val category: LocalizedStrings,
    val categoryFullName: LocalizedStrings,
    val dateAwarded: String? = null,
    val prizeAmount: Long? = null,
    val prizeAmountAdjusted: Long? = null,
    val links: List<ApiLink> = emptyList(),
    val laureates: List<Laureate> = emptyList(),
)

@Serializable
data class NobelPrizesEnvelope(
    val nobelPrizes: List<NobelPrize>,
)

@Serializable
data class LaureatesEnvelope(
    val laureates: List<Laureate>,
)
