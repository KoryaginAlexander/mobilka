package com.example.pr_6_4.domain.model

data class NobelLaureate(
    val id: String,
    val fullName: String,
    val year: String,
    val category: String,
    val motivation: String,
    val birthCountry: String,
    val birthCity: String,
    val portraitUrl: String?
)
