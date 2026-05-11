package ru.mobilka.pr64.server.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class PrizeLaureateDto(
    val id: Int,
    val fullName: String,
    val portion: String?,
    val motivation: String?,
    val portraitUrl: String?,
)

@Serializable
data class PrizeItemDto(
    val id: Int,
    val awardYear: Int,
    val category: String,
    val fullName: String,
    val motivation: String?,
    val detailLink: String?,
    val laureates: List<PrizeLaureateDto>,
)

@Serializable
data class PrizesListResponse(
    val prizes: List<PrizeItemDto>,
    val syncedFromRemote: Boolean,
)
