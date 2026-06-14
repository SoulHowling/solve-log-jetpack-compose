package com.soulhowling.solvelog_jetpackcompose.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

val LocalBottomBarVisibility = compositionLocalOf { true }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel()
            LaunchedEffect(Unit) {
                mainViewModel.loadThemeMode()
            }
            val themeMode by mainViewModel.themeMode.collectAsState()

            SolveLogJetpackComposeTheme(
                themeMode = themeMode
            ) {
                MainScreen(
                    bottomNaviItems = Screens.getBottomNaviScreens(),
                    allScreens = Screens.getAllScreens()
                )
            }
        }
    }
}