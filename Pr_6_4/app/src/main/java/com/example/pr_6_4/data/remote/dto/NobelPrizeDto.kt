package com.example.pr_6_4.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NobelPrizesResponse(
    @SerialName("prizes") val prizes: List<NobelPrizeDto> = emptyList()
)

@Serializable
data class NobelPrizeDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("awardYear") val awardYear: Int = 0,
    @SerialName("category") val category: String = "",
    @SerialName("fullName") val fullName: String = "",
    @SerialName("motivation") val motivation: String? = null,
    @SerialName("detailLink") val detailLink: String? = null,
    @SerialName("laureates") val laureates: List<LaureateDto> = emptyList()
)

@Serializable
data class LaureateDto(
    @SerialName("id") val id: Int = 0,
    @SerialName("fullName") val fullName: String = "",
    @SerialName("motivation") val motivation: String? = null,
    @SerialName("portraitUrl") val portraitUrl: String? = null
)
