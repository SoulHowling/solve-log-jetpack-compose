package com.soulhowling.solvelog_jetpackcompose.feature.settings

import androidx.annotation.StringRes
import com.soulhowling.solvelog_jetpackcompose.R

sealed class SettingItem(
    @get:StringRes val titleResId: Int,
    val action: (() -> Unit)? = null,
) {
    class AppTheme(action: (() -> Unit)? = null) : SettingItem(R.string.setting_app_theme, action)
    class WipeData(action: (() -> Unit)? = null) : SettingItem(R.string.setting_wipe_data, action)
    class AboutThisApp(action: (() -> Unit)? = null) : SettingItem(R.string.setting_about_this_app, action)
    class PrivacyPolicy(action: (() -> Unit)? = null) : SettingItem(R.string.setting_privacy_policy, action)
    class SoftwareLicense(action: (() -> Unit)? = null) : SettingItem(R.string.setting_software_license, action)
}