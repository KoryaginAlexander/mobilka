package com.example.pr_6_1.domain.usecase

import com.example.pr_6_1.domain.model.Photo
import com.example.pr_6_1.domain.repository.PhotoRepository

class GetPhotosUseCase(private val repository: PhotoRepository) {
    suspend operator fun invoke(): List<Photo> = repository.getPhotos()
}
