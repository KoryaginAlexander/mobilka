package com.example.pr_6_2.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pr_6_2.data.remote.api.NobelApiService
import com.example.pr_6_2.data.remote.httpClient
import com.example.pr_6_2.data.repository.NobelRepositoryImpl
import com.example.pr_6_2.domain.model.NobelLaureate
import com.example.pr_6_2.domain.usecase.GetLaureatesUseCase
import com.example.pr_6_2.presentation.detail.NobelDetailScreen
import com.example.pr_6_2.presentation.list.NobelListScreen
import com.example.pr_6_2.presentation.list.NobelListViewModel
import com.example.pr_6_2.presentation.list.NobelListViewModelFactory

object Routes {
    const val LIST = "list"
    const val DETAIL = "detail"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    var selectedLaureate by remember { mutableStateOf<NobelLaureate?>(null) }

    val apiService = remember { NobelApiService(httpClient) }
    val repository = remember { NobelRepositoryImpl(apiService) }
    val useCase = remember { GetLaureatesUseCase(repository) }
    val viewModel: NobelListViewModel = viewModel(factory = NobelListViewModelFactory(useCase))

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            NobelListScreen(
                viewModel = viewModel,
                onLaureateClick = { laureate ->
                    selectedLaureate = laureate
                    navController.navigate(Routes.DETAIL)
                }
            )
        }
        composable(Routes.DETAIL) {
            val laureate = selectedLaureate
            if (laureate != null) {
                NobelDetailScreen(
                    laureate = laureate,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
