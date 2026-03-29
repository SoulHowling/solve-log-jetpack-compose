package com.soulhowling.solvelog_jetpackcompose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.soulhowling.solvelog_jetpackcompose.feature.editor.compose.SolveLogEditorScreen
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.compose.MeasurementScreen
import com.soulhowling.solvelog_jetpackcompose.feature.settings.compose.SettingScreen
import com.soulhowling.solvelog_jetpackcompose.feature.statistics.compose.LogStatisticsScreen
import com.soulhowling.solvelog_jetpackcompose.ui.theme.SolveLogJetpackComposeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SolveLogJetpackComposeTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screens.SolveLogEditor,
        Screens.LogStatistics,
        Screens.Measurement,
        Screens.Setting
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        topBar = {
            TopAppBar(currentDestination, items)
        },
        bottomBar = {
            BottomNavigation(navController, currentDestination, items)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screens.SolveLogEditor.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screens.SolveLogEditor.route) { SolveLogEditorScreen() }
            composable(Screens.LogStatistics.route) { LogStatisticsScreen() }
            composable(Screens.Measurement.route) { MeasurementScreen() }
            composable(Screens.Setting.route) { SettingScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(currentDestination: NavDestination?, items: List<Screens>) {
    val currentScreen = items.find { it.route == currentDestination?.route }
    val title = currentScreen?.title ?: "Solve Log"

    TopAppBar(
        title = { Text(title) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    )
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    currentDestination: NavDestination?,
    items: List<Screens>
) {

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    SolveLogJetpackComposeTheme {
        MainScreen()
    }
}