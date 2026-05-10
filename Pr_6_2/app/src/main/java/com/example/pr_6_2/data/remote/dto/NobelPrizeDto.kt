package com.example.pr_6_2.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NobelPrizesResponse(
    @SerialName("nobelPrizes") val nobelPrizes: List<NobelPrizeDto> = emptyList()
)

@Serializable
data class NobelPrizeDto(
    @SerialName("awardYear") val awardYear: String = "",
    @SerialName("category") val category: CategoryDto? = null,
    @SerialName("laureates") val laureates: List<LaureateDto>? = null
)

@Serializable
data class CategoryDto(
    @SerialName("en") val en: String = ""
)

@Serializable
data class LaureateDto(
    @SerialName("id") val id: String = "",
    @SerialName("fullName") val fullName: LocalizedNameDto? = null,
    @SerialName("motivation") val motivation: LocalizedNameDto? = null,
    @SerialName("birth") val birth: BirthDto? = null,
    @SerialName("links") val links: List<LinkDto>? = null
)

@Serializable
data class LocalizedNameDto(
    @SerialName("en") val en: String = "",
    @SerialName("no") val no: String? = null,
    @SerialName("se") val se: String? = null
)

@Serializable
data class BirthDto(
    @SerialName("place") val place: PlaceDto? = null
)

@Serializable
data class PlaceDto(
    @SerialName("city") val city: LocalizedNameDto? = null,
    @SerialName("country") val country: LocalizedNameDto? = null,
    @SerialName("cityNow") val cityNow: LocalizedNameDto? = null,
    @SerialName("countryNow") val countryNow: LocalizedNameDto? = null
)

@Serializable
data class LinkDto(
    @SerialName("rel") val rel: String = "",
    @SerialName("href") val href: String = "",
    @SerialName("action") val action: String = "",
    @SerialName("types") val types: String = ""
)
