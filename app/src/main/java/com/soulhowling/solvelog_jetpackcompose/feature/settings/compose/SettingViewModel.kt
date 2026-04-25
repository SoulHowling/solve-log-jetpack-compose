package com.soulhowling.solvelog_jetpackcompose.feature.settings.compose

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.SharedPrefHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingViewModel(application: Application): AndroidViewModel(application) {

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        saveThemeMode(mode)
    }

    private fun loadThemeMode(): ThemeMode {
        val themeId = SharedPrefHelper.getInt(application, SharedPrefHelper.KEY_THEME_MODE, ThemeMode.SYSTEM.themeId)
        return ThemeMode.getThemeModeBy(themeId)
    }

    private fun saveThemeMode(mode: ThemeMode) {
        SharedPrefHelper.putInt(application, SharedPrefHelper.KEY_THEME_MODE, mode.themeId)
    }

}