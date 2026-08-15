package com.soulhowling.solvelog_jetpackcompose.data

import android.content.Context
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.SharedPrefHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PreferenceRepository {

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _viewType = MutableStateFlow(ViewType.CARD)
    val viewType: StateFlow<ViewType> = _viewType.asStateFlow()

    fun setThemeMode(context: Context, mode: ThemeMode) {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_THEME_MODE, mode.themeId)
        _themeMode.value = mode
    }

    fun loadThemeMode(context: Context) {
        val themeId = SharedPrefHelper.getInt(
            context,
            SharedPrefHelper.KEY_THEME_MODE,
            ThemeMode.SYSTEM.themeId
        )
        _themeMode.value = ThemeMode.getThemeModeBy(themeId)
    }

    fun setViewType(context: Context, viewType: ViewType) {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_VIEW_TYPE, viewType.index)
        _viewType.value = viewType
    }

    fun loadViewType(context: Context) {
        val viewTypeIndex = SharedPrefHelper.getInt(
            context, SharedPrefHelper.KEY_VIEW_TYPE,
            ViewType.CARD.index
        )
        _viewType.value = ViewType.from(viewTypeIndex)
    }
}