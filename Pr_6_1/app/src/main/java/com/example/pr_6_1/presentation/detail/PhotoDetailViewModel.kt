package com.example.pr_6_1.presentation.detail

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.pr_6_1.data.repository.PhotoRepositoryImpl
import com.example.pr_6_1.domain.usecase.DownloadPhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class DownloadState {
    object Idle : DownloadState()
    object Downloading : DownloadState()
    object Success : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class PhotoDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val downloadPhotoUseCase = DownloadPhotoUseCase(PhotoRepositoryImpl())

    private val _downloadState = MutableLiveData<DownloadState>(DownloadState.Idle)
    val downloadState: LiveData<DownloadState> = _downloadState

    fun downloadPhoto(url: String, uri: Uri) {
        viewModelScope.launch {
            _downloadState.value = DownloadState.Downloading
            try {
                val bytes = downloadPhotoUseCase(url)
                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver
                        .openOutputStream(uri)
                        ?.use { it.write(bytes) }
                        ?: throw Exception("Не удалось открыть файл")
                }
                _downloadState.value = DownloadState.Success
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Error(e.message ?: "Ошибка скачивания")
            }
        }
    }
}
