package com.example.pr_6_3.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pr_6_3.domain.model.User
import com.example.pr_6_3.domain.usecase.GetUsersUseCase
import com.example.pr_6_3.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UsersViewModel(private val getUsersUseCase: GetUsersUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<User>>> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getUsersUseCase().fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Ошибка загрузки") }
            )
        }
    }
}
