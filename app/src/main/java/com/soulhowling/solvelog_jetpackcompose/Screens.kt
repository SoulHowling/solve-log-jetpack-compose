package com.soulhowling.solvelog_jetpackcompose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screens(val route: String, val title: String, val icon: ImageVector) {
    data object SolveLogEditor: Screens(
        route = "solve_log_editor",
        title = "Editor",
        icon = Icons.Default.Edit
        )
    data object LogStatistics: Screens(
        route = "log_statistics",
        title = "Statistics",
        icon = Icons.Default.BarChart
    )
    data object Measurement: Screens(
        route = "measurement",
        title = "Measurement",
        icon = Icons.Default.Timer
    )
    data object Setting: Screens(
        route = "setting",
        title = "Setting",
        icon = Icons.Default.Settings
    )
}
