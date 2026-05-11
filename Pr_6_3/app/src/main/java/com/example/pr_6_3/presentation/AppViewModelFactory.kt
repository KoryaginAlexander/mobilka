package com.example.pr_6_3.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pr_6_3.di.AppContainer
import com.example.pr_6_3.presentation.detail.UserDetailViewModel
import com.example.pr_6_3.presentation.login.LoginViewModel
import com.example.pr_6_3.presentation.users.UsersViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(container.loginUseCase, container.getTokenUseCase) as T

        modelClass.isAssignableFrom(UsersViewModel::class.java) ->
            UsersViewModel(container.getUsersUseCase) as T

        modelClass.isAssignableFrom(UserDetailViewModel::class.java) ->
            UserDetailViewModel(container.getUserDetailUseCase, container.logoutUseCase) as T

        else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}
