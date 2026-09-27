package com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R

sealed class WipeDataItem(
    @get:StringRes val titleResId: Int,
    @get:StringRes val descriptionResId: Int,
) {
    data object WipeAllData : WipeDataItem(
        titleResId = R.string.wipe_data_all,
        descriptionResId = R.string.wipe_data_all_description,
    )

    data object WipeAllLogs : WipeDataItem(
        titleResId = R.string.wipe_data_logs,
        descriptionResId = R.string.wipe_data_logs_description,
    )

    data object WipeAllTimers : WipeDataItem(
        titleResId = R.string.wipe_data_timers,
        descriptionResId = R.string.wipe_data_timers_description,
    )

    data object WipeAllMemos : WipeDataItem(
        titleResId = R.string.wipe_data_memos,
        descriptionResId = R.string.wipe_data_memos_description,
    )
}