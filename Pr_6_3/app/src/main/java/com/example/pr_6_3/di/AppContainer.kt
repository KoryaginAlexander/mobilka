package com.example.pr_6_3.di

import android.content.Context
import com.example.pr_6_3.data.local.TokenDataStore
import com.example.pr_6_3.data.remote.ApiService
import com.example.pr_6_3.data.repository.AuthRepositoryImpl
import com.example.pr_6_3.data.repository.UserRepositoryImpl
import com.example.pr_6_3.domain.repository.AuthRepository
import com.example.pr_6_3.domain.repository.UserRepository
import com.example.pr_6_3.domain.usecase.GetTokenUseCase
import com.example.pr_6_3.domain.usecase.GetUserDetailUseCase
import com.example.pr_6_3.domain.usecase.GetUsersUseCase
import com.example.pr_6_3.domain.usecase.LoginUseCase
import com.example.pr_6_3.domain.usecase.LogoutUseCase

class AppContainer(context: Context) {

    private val tokenDataStore = TokenDataStore(context)
    private val apiService = ApiService(tokenDataStore)

    private val authRepository: AuthRepository = AuthRepositoryImpl(apiService, tokenDataStore)
    private val userRepository: UserRepository = UserRepositoryImpl(apiService)

    val loginUseCase = LoginUseCase(authRepository)
    val logoutUseCase = LogoutUseCase(authRepository)
    val getTokenUseCase = GetTokenUseCase(authRepository)
    val getUsersUseCase = GetUsersUseCase(userRepository)
    val getUserDetailUseCase = GetUserDetailUseCase(userRepository)
}
