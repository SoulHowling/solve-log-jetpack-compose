package com.soulhowling.solvelog_jetpackcompose.core

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.soulhowling.solvelog_jetpackcompose.R

sealed class Screens(
    val route: String,
    @get:StringRes val title: Int,
    val icon: ImageVector
) {
    data object SolveLog: Screens(
        route = "solve_log",
        title = R.string.tab_editor,
        icon = Icons.Default.Edit
        )
    data object SolveLogEditor: Screens(
        route = "solve_log_editor",
        title = R.string.editor_detail,
        icon = Icons.Default.Edit
    )
    data object LogStatistics: Screens(
        route = "log_statistics",
        title = R.string.tab_statistics,
        icon = Icons.Default.BarChart
    )
    data object Measurement: Screens(
        route = "measurement",
        title = R.string.tab_measurement,
        icon = Icons.Default.Timer
    )
    data object Setting: Screens(
        route = "setting",
        title = R.string.tab_setting,
        icon = Icons.Default.Settings
    )
}
