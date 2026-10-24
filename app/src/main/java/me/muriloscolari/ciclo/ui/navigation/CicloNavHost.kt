package me.muriloscolari.ciclo.ui.navigation

import android.annotation.SuppressLint
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import me.muriloscolari.ciclo.ui.calculator.CalculatorScreen
import me.muriloscolari.ciclo.ui.calculator.CalculatorViewModel
import me.muriloscolari.ciclo.ui.designsystem.AuroraBackground
import me.muriloscolari.ciclo.ui.designsystem.GlassNavigationBar
import me.muriloscolari.ciclo.ui.designsystem.NavigationTab
import me.muriloscolari.ciclo.ui.history.HistoryScreen
import me.muriloscolari.ciclo.ui.history.HistoryViewModel
import me.muriloscolari.ciclo.ui.settings.SettingsScreen
import me.muriloscolari.ciclo.ui.settings.SettingsViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CicloNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val hazeState = remember { HazeState() }
    val snackbarHostState = remember { SnackbarHostState() }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationTab.CALCULATE.route

    val currentTab = when (currentRoute) {
        NavigationTab.HISTORY.route -> NavigationTab.HISTORY
        NavigationTab.SETTINGS.route -> NavigationTab.SETTINGS
        else -> NavigationTab.CALCULATE
    }

    AuroraBackground(
        modifier = modifier.fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Conteúdo rolável / telas marcado com Modifier.haze para ser capturado pelo Haze
                NavHost(
                    navController = navController,
                    startDestination = NavigationTab.CALCULATE.route,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { ExitTransition.None },
                    modifier = Modifier
                        .fillMaxSize()
                        .haze(state = hazeState)
                ) {
                    composable(NavigationTab.CALCULATE.route) {
                        val viewModel = hiltViewModel<CalculatorViewModel>()
                        CalculatorScreen(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    composable(NavigationTab.HISTORY.route) {
                        val viewModel = hiltViewModel<HistoryViewModel>()
                        HistoryScreen(
                            viewModel = viewModel,
                            snackbarHostState = snackbarHostState
                        )
                    }

                    composable(NavigationTab.SETTINGS.route) {
                        val viewModel = hiltViewModel<SettingsViewModel>()
                        SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }

                // Barra de Navegação Inferior de Vidro Flutuante (aplica hazeChild sobre o NavHost)
                GlassNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab.route != currentRoute) {
                            navController.navigate(tab.route) {
                                popUpTo(NavigationTab.CALCULATE.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
