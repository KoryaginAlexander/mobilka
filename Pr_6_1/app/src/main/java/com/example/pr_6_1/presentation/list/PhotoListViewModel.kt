package com.example.pr_6_1.presentation.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pr_6_1.data.repository.PhotoRepositoryImpl
import com.example.pr_6_1.domain.model.Photo
import com.example.pr_6_1.domain.usecase.GetPhotosUseCase
import com.example.pr_6_1.presentation.util.UiState
import kotlinx.coroutines.launch

class PhotoListViewModel : ViewModel() {

    private val getPhotosUseCase = GetPhotosUseCase(PhotoRepositoryImpl())

    private val _uiState = MutableLiveData<UiState<List<Photo>>>(UiState.Loading)
    val uiState: LiveData<UiState<List<Photo>>> = _uiState

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val photos = getPhotosUseCase()
                _uiState.value = UiState.Success(photos)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }
}
