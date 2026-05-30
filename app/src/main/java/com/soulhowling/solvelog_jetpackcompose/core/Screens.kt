package com.soulhowling.solvelog_jetpackcompose.core

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.soulhowling.solvelog_jetpackcompose.R

private const val SOLVE_LOG_ROUTE = "solve_log"
private const val SOLVE_LOG_VIEWER_ROUTE = "solve_log_viewer"
private const val SOLVE_LOG_EDITOR_ROUTE = "solve_log_editor"
private const val LOG_STATISTICS_ROUTE = "log_statistics"
private const val MEASUREMENT_ROUTE = "measurement"
private const val SETTING_ROUTE = "setting"
private const val SLASH = "/"

sealed class Screens(
    val route: String,
    @get:StringRes val title: Int,
    val icon: ImageVector
) {
    data object SolveLog: Screens(
        route = SOLVE_LOG_ROUTE,
        title = R.string.tab_editor,
        icon = Icons.Default.Edit
        )
    data object SolveLogViewer: Screens(
        route = SOLVE_LOG_VIEWER_ROUTE + SLASH + "{$ARG_SOLVE_LOG_ID}",
        title = R.string.log_view,
        icon = Icons.Default.Preview
    ) {
        fun createRoute(solveLogId: Int) = SOLVE_LOG_VIEWER_ROUTE + SLASH + "$solveLogId"
    }
    data object SolveLogEditor: Screens(
        route = SOLVE_LOG_EDITOR_ROUTE,
        title = R.string.log_edit,
        icon = Icons.Default.Edit
    )
    data object LogStatistics: Screens(
        route = LOG_STATISTICS_ROUTE,
        title = R.string.tab_statistics,
        icon = Icons.Default.BarChart
    )
    data object Measurement: Screens(
        route = MEASUREMENT_ROUTE,
        title = R.string.tab_measurement,
        icon = Icons.Default.Timer
    )
    data object Setting: Screens(
        route = SETTING_ROUTE,
        title = R.string.tab_setting,
        icon = Icons.Default.Settings
    )

    companion object {
        const val ARG_SOLVE_LOG_ID = "solveLogId"

        fun getAllScreens(): List<Screens> = listOf(
            SolveLog,
            SolveLogViewer,
            SolveLogEditor,
            LogStatistics,
            Measurement,
            Setting
        )

        fun getBottomNaviScreens(): List<Screens> = listOf(
            SolveLog,
            LogStatistics,
            Measurement,
            Setting
        )
    }

}
