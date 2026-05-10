package com.example.pr_6_1.domain.usecase

import com.example.pr_6_1.domain.repository.PhotoRepository

class DownloadPhotoUseCase(private val repository: PhotoRepository) {
    suspend operator fun invoke(url: String): ByteArray = repository.downloadPhoto(url)
}
