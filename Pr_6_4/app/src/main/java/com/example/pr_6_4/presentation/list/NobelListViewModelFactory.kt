package com.example.pr_6_4.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pr_6_4.domain.usecase.GetLaureatesUseCase

class NobelListViewModelFactory(
    private val getLaureatesUseCase: GetLaureatesUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NobelListViewModel(getLaureatesUseCase) as T
    }
}
