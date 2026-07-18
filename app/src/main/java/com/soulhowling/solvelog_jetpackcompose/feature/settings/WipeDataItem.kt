package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R

sealed class WipeDataItem(
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
    val action: (suspend () -> Boolean)? = null,
) {
    class WipeAllData(action: (suspend () -> Boolean)? = null) : WipeDataItem(
        titleResId = R.string.wipe_data_all,
        descriptionResId = R.string.wipe_data_all_description,
        action = action
    )

    class WipeAllLogs(action: (suspend () -> Boolean)? = null) : WipeDataItem(
        titleResId = R.string.wipe_data_logs,
        descriptionResId = R.string.wipe_data_logs_description,
        action = action
    )

    class WipeAllTimers(action: (suspend () -> Boolean)? = null) : WipeDataItem(
        titleResId = R.string.wipe_data_timers,
        descriptionResId = R.string.wipe_data_timers_description,
        action = action
    )
}