package com.example.pr_6_3.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pr_6_3.App
import com.example.pr_6_3.presentation.AppViewModelFactory
import com.example.pr_6_3.presentation.detail.UserDetailScreen
import com.example.pr_6_3.presentation.detail.UserDetailViewModel
import com.example.pr_6_3.presentation.login.LoginScreen
import com.example.pr_6_3.presentation.login.LoginViewModel
import com.example.pr_6_3.presentation.users.UsersListScreen
import com.example.pr_6_3.presentation.users.UsersViewModel

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val container = (context.applicationContext as App).container
    val factory = remember { AppViewModelFactory(container) }

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Login.route) {
            val vm: LoginViewModel = viewModel(factory = factory)
            LoginScreen(
                viewModel = vm,
                onLoginSuccess = {
                    navController.navigate(Screen.UsersList.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.UsersList.route) {
            val vm: UsersViewModel = viewModel(factory = factory)
            UsersListScreen(
                viewModel = vm,
                onUserClick = { userId ->
                    navController.navigate(Screen.UserDetail.createRoute(userId))
                }
            )
        }

        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: return@composable
            val vm: UserDetailViewModel = viewModel(factory = factory)
            UserDetailScreen(
                userId = userId,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
