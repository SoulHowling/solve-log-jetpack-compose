package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R

sealed class SettingItem(
    @get:StringRes val titleResId: Int
) {
    data object Notification : SettingItem(R.string.setting_notification)
    data object AppTheme : SettingItem(R.string.setting_app_theme)
    data object WipeData: SettingItem(R.string.setting_wipe_data)
    data object AboutThisApp : SettingItem(R.string.setting_about_this_app)
}