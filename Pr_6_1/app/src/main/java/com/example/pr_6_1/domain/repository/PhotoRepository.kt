package com.example.pr_6_1.domain.repository

import com.example.pr_6_1.domain.model.Photo

interface PhotoRepository {
    suspend fun getPhotos(): List<Photo>
    suspend fun downloadPhoto(url: String): ByteArray
}
