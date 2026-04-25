package com.soulhowling.solvelog_jetpackcompose.data

import android.content.Context
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.SharedPrefHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PreferenceRepository {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(context: Context, mode: ThemeMode) {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_THEME_MODE, mode.themeId)
        _themeMode.value = mode
    }

    fun loadThemeMode(context: Context) {
        val themeId = SharedPrefHelper.getInt(context, SharedPrefHelper.KEY_THEME_MODE, ThemeMode.SYSTEM.themeId)
        _themeMode.value = ThemeMode.getThemeModeBy(themeId)
    }
}