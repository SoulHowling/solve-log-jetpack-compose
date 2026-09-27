package com.soulhowling.solvelog_jetpackcompose.core

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.MeasurementScreen
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.compose.TimerPageScreen
import com.soulhowling.solvelog_jetpackcompose.feature.memo.compose.MemoEditorScreen
import com.soulhowling.solvelog_jetpackcompose.feature.memo.compose.MemoListScreen
import com.soulhowling.solvelog_jetpackcompose.feature.memo.compose.MemoViewScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.SettingScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.aboutthisapp.AboutThisAppScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.help.HelpScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.privacypolicy.PrivacyPolicyScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.softwarelicense.SoftwareLicenseScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata.WipeDataScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogEditorScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogListScreen
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.compose.SolveLogViewerScreen
import com.soulhowling.solvelog_jetpackcompose.utils.compose.LifecycleTouchGuard

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    innerPadding: PaddingValues,
) {
    val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

    NavHost(
        navController = navController,
        startDestination = Screens.SolveLogRoute.route,
        modifier = modifier.padding(innerPadding)
    ) {
        composable(Screens.SolveLogRoute.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                SolveLogListScreen(
                    onNavigateToViewer = { id ->
                        navController.navigate(Screens.SolveLogViewer.createRoute(id))
                    }
                )
            }
        }
        composable(
            route = Screens.SolveLogViewer.route,
            arguments = listOf(navArgument(Screens.ARG_SOLVE_LOG_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val solveLogId = backStackEntry.arguments?.getInt(Screens.ARG_SOLVE_LOG_ID) ?: -1
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                SolveLogViewerScreen(
                    onNavigateToEditor = { attempt ->
                        navController.navigate(
                            Screens.SolveLogEditor.createRoute(
                                solveLogId,
                                attempt
                            )
                        )
                    }
                )
            }
        }
        composable(
            route = Screens.SolveLogEditor.route,
            arguments = listOf(
                navArgument(Screens.ARG_SOLVE_LOG_ID) { type = NavType.IntType },
                navArgument(Screens.ARG_ATTEMPT) { type = NavType.IntType }
            )
        ) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                SolveLogEditorScreen()
            }
        }
        composable(Screens.Measurement.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                MeasurementScreen(
                    onNavigateToTimerPageScreen = { timerStateId ->
                        navController.navigate(Screens.TimerPage.createRoute(timerStateId))
                    }
                )
            }
        }
        composable(
            route = Screens.TimerPage.route,
            arguments = listOf(navArgument(Screens.ARG_TIMER_STATE_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                TimerPageScreen()
            }
        }
        composable(Screens.MemoList.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                MemoListScreen(
                    onNavigateToView = { memoId ->
                        navController.navigate(Screens.MemoView.createRoute(memoId))
                    },
                    onNavigateToEditor = { memoId, folderId ->
                        navController.navigate(Screens.MemoEditor.createRoute(memoId, folderId))
                    }
                )
            }
        }
        composable(
            route = Screens.MemoEditor.route,
            arguments = listOf(
                navArgument(Screens.ARG_MEMO_ID) { type = NavType.IntType },
                navArgument(Screens.ARG_FOLDER_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getInt(Screens.ARG_MEMO_ID) ?: -1
            val rawFolderId = backStackEntry.arguments?.getInt(Screens.ARG_FOLDER_ID) ?: -1
            val folderId = if (rawFolderId == -1) null else rawFolderId
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                MemoEditorScreen(
                    memoId = memoId,
                    folderId = folderId
                )
            }
        }
        composable(
            route = Screens.MemoView.route,
            arguments = listOf(navArgument(Screens.ARG_MEMO_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getInt(Screens.ARG_MEMO_ID) ?: -1
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                MemoViewScreen(
                    memoId = memoId,
                    onNavigateToEditor = { id ->
                        navController.navigate(Screens.MemoEditor.createRoute(id))
                    }
                )
            }
        }
        composable(Screens.Setting.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                SettingScreen(
                    onNavigateToWipeData = {
                        navController.navigate(Screens.WipeData.route)
                    },
                    onNavigateToAboutThisApp = {
                        navController.navigate(Screens.AboutThisApp.route)
                    },
                    onPrivacyPolicy = {
                        navController.navigate(Screens.PrivacyPolicy.route)
                    },
                    onSoftwareLicense = {
                        navController.navigate(Screens.SoftwareLicense.route)
                    }
                )
            }
        }
        composable(Screens.WipeData.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                WipeDataScreen()
            }
        }
        composable(Screens.AboutThisApp.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                AboutThisAppScreen(
                    onNavigateToHelp = {
                        navController.navigate(Screens.Help.route)
                    }
                )
            }
        }
        composable(Screens.Help.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                HelpScreen()
            }
        }
        composable(Screens.PrivacyPolicy.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                PrivacyPolicyScreen()
            }
        }
        composable(Screens.SoftwareLicense.route) { backStackEntry ->
            LifecycleTouchGuard(isTargetScreen = backStackEntry == currentNavBackStackEntry) {
                SoftwareLicenseScreen()
            }
        }
    }
}