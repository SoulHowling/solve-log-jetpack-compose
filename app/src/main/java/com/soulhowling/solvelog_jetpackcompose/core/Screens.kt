package com.soulhowling.solvelog_jetpackcompose.core

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import com.soulhowling.solvelog_jetpackcompose.R

private const val SOLVE_LOG_ROUTE = "solve_log"
private const val SOLVE_LOG_VIEWER_ROUTE = "solve_log_viewer"
private const val SOLVE_LOG_EDITOR_ROUTE = "solve_log_editor"
private const val MEASUREMENT_ROUTE = "measurement"
private const val SETTING_ROUTE = "setting"
private const val WIPE_DATA_ROUTE = "wipe_data"
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
        route = SOLVE_LOG_EDITOR_ROUTE + SLASH + "{$ARG_SOLVE_LOG_ID}" + SLASH + "{$ARG_ATTEMPT}",
        title = R.string.log_edit,
        icon = Icons.Default.Edit
    ) {
        fun createRoute(solveLogId: Int, attempt: Int) =
            SOLVE_LOG_EDITOR_ROUTE + SLASH + "$solveLogId" + SLASH + "$attempt"
    }
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
    data object WipeData: Screens(
        route = SETTING_ROUTE + SLASH + WIPE_DATA_ROUTE,
        title = R.string.wipe_data_title,
        icon = Icons.Default.Delete
    )

    companion object {
        const val ARG_SOLVE_LOG_ID = "solveLogId"
        const val ARG_ATTEMPT = "attempt"

        fun getAllScreens(): List<Screens> = listOf(
            SolveLog,
            SolveLogViewer,
            SolveLogEditor,
            Measurement,
            Setting,
            WipeData
        )

        fun getBottomNaviScreens(): List<Screens> = listOf(
            SolveLog,
            Measurement,
            Setting
        )
    }

}
