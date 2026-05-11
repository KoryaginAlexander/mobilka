package com.example.pr_6_3.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pr_6_3.domain.model.User
import com.example.pr_6_3.domain.usecase.GetUserDetailUseCase
import com.example.pr_6_3.domain.usecase.LogoutUseCase
import com.example.pr_6_3.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val uiState: StateFlow<UiState<User>> = _uiState.asStateFlow()

    fun loadUser(id: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            getUserDetailUseCase(id).fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Ошибка загрузки") }
            )
        }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onDone()
        }
    }
}
