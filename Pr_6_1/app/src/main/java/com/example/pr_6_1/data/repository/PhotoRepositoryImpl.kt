package com.example.pr_6_1.data.repository

import com.example.pr_6_1.data.api.RetrofitInstance
import com.example.pr_6_1.domain.model.Photo
import com.example.pr_6_1.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class PhotoRepositoryImpl : PhotoRepository {

    private val api = RetrofitInstance.api
    private val httpClient = RetrofitInstance.httpClient

    override suspend fun getPhotos(): List<Photo> {
        return api.getPhotos().map { dto ->
            Photo(
                id = dto.id,
                author = dto.author,
                width = dto.width,
                height = dto.height,
                url = dto.url,
                downloadUrl = dto.downloadUrl,
                thumbnailUrl = "https://picsum.photos/id/${dto.id}/400/300"
            )
        }
    }

    override suspend fun downloadPhoto(url: String): ByteArray {
        return withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                response.body?.bytes() ?: throw Exception("Пустой ответ сервера")
            }
        }
    }
}
