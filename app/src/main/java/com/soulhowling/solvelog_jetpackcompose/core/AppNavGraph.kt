package com.soulhowling.solvelog_jetpackcompose.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.MeasurementScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.SettingScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogEditorScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogListScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogViewerScreen
import com.soulhowling.solvelog_jetpackcompose.feature.statistics.LogStatisticsScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screens.SolveLog.route,
        modifier = modifier.padding(innerPadding)
    ) {
        composable(Screens.SolveLog.route) {
            SolveLogListScreen(
                onNavigateToViewer = { id ->
                    navController.navigate(Screens.SolveLogViewer.createRoute(id))
                }
            )
        }
        composable(
            route = Screens.SolveLogViewer.route,
            arguments = listOf(navArgument(Screens.ARG_SOLVE_LOG_ID) { type = NavType.IntType })
        ) {
            SolveLogViewerScreen(
                onNavigateToEditor = {
                    navController.navigate(Screens.SolveLogEditor.route)
                }
            )
        }
        composable(Screens.SolveLogEditor.route) {
            SolveLogEditorScreen()
        }
        composable(Screens.LogStatistics.route) {
            LogStatisticsScreen()
        }
        composable(Screens.Measurement.route) {
            MeasurementScreen()
        }
        composable(Screens.Setting.route) {
            SettingScreen()
        }
    }
}