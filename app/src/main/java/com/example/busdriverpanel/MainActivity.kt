package com.example.busdriverpanel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.busdriverpanel.ui.screen.DashboardScreen
import com.example.busdriverpanel.ui.screen.LoginScreen
import com.example.busdriverpanel.ui.theme.BusdriverpanelTheme
import com.example.busdriverpanel.ui.viewmodel.LoginViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BusdriverpanelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BusDriverApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun BusDriverApp(
    modifier: Modifier = Modifier,
    loginViewModel: LoginViewModel = viewModel()
) {
    val navController = rememberNavController()
    val loginState by loginViewModel.uiState.collectAsState()
    
    // Navigate based on login state
    LaunchedEffect(loginState.isLoggedIn) {
        if (loginState.isLoggedIn) {
            navController.navigate("dashboard") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("dashboard") { inclusive = true }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = if (loginState.isLoggedIn) "dashboard" else "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // Navigation handled by LaunchedEffect above
                },
                viewModel = loginViewModel
            )
        }
        
        composable("dashboard") {
            DashboardScreen(
                onLogout = {
                    // Navigation handled by LaunchedEffect above
                }
            )
        }
    }
}