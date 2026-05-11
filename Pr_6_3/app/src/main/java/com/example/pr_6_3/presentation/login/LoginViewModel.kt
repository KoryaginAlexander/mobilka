package com.example.pr_6_3.presentation.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pr_6_3.domain.usecase.GetTokenUseCase
import com.example.pr_6_3.domain.usecase.LoginUseCase
import com.example.pr_6_3.presentation.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getTokenUseCase: GetTokenUseCase
) : ViewModel() {

    var username by mutableStateOf("")
    var password by mutableStateOf("")

    private val _uiState = MutableStateFlow<UiState<Unit>?>(null)
    val uiState: StateFlow<UiState<Unit>?> = _uiState.asStateFlow()

    init {
        checkExistingToken()
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val token = getTokenUseCase()
            if (!token.isNullOrBlank()) {
                _uiState.value = UiState.Success(Unit)
            }
        }
    }

    fun login() {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = UiState.Error("Заполните все поля")
            return
        }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            loginUseCase(username, password).fold(
                onSuccess = { _uiState.value = UiState.Success(Unit) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Ошибка входа") }
            )
        }
    }

    fun clearError() {
        if (_uiState.value is UiState.Error) _uiState.value = null
    }
}
