package com.soulhowling.solvelog_jetpackcompose.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.feature.solvelog.ViewType
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import com.soulhowling.solvelog_jetpackcompose.utils.SharedPrefHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class PreferenceRepositoryTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearPreferences()
        PreferenceRepository.setThemeMode(context, ThemeMode.SYSTEM)
        PreferenceRepository.setViewType(context, ViewType.CARD)
    }

    @After
    fun tearDown() {
        clearPreferences()
        PreferenceRepository.setThemeMode(context, ThemeMode.SYSTEM)
        PreferenceRepository.setViewType(context, ViewType.CARD)
    }

    private fun clearPreferences() {
        context.getSharedPreferences("SolveLog_Preference", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun initialThemeMode_isSystem() {
        assertEquals(ThemeMode.SYSTEM, PreferenceRepository.themeMode.value)
    }

    @Test
    fun initialViewType_isCard() {
        assertEquals(ViewType.CARD, PreferenceRepository.viewType.value)
    }

    @Test
    fun setThemeMode_updatesStateFlowAndSharedPref() {
        PreferenceRepository.setThemeMode(context, ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, PreferenceRepository.themeMode.value)
        val savedThemeId = SharedPrefHelper.getInt(context, SharedPrefHelper.KEY_THEME_MODE, -1)
        assertEquals(ThemeMode.DARK.themeId, savedThemeId)

        PreferenceRepository.setThemeMode(context, ThemeMode.LIGHT)
        assertEquals(ThemeMode.LIGHT, PreferenceRepository.themeMode.value)
        val savedThemeIdLight = SharedPrefHelper.getInt(context, SharedPrefHelper.KEY_THEME_MODE, -1)
        assertEquals(ThemeMode.LIGHT.themeId, savedThemeIdLight)
    }

    @Test
    fun loadThemeMode_loadsSavedValueFromSharedPref() {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_THEME_MODE, ThemeMode.DARK.themeId)

        PreferenceRepository.loadThemeMode(context)

        assertEquals(ThemeMode.DARK, PreferenceRepository.themeMode.value)
    }

    @Test
    fun loadThemeMode_invalidValueInSharedPref_defaultsToSystem() {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_THEME_MODE, 999)

        PreferenceRepository.loadThemeMode(context)

        assertEquals(ThemeMode.SYSTEM, PreferenceRepository.themeMode.value)
    }

    @Test
    fun setViewType_updatesStateFlowAndSharedPref() {
        PreferenceRepository.setViewType(context, ViewType.LIST)

        assertEquals(ViewType.LIST, PreferenceRepository.viewType.value)
        val savedViewTypeIndex = SharedPrefHelper.getInt(context, SharedPrefHelper.KEY_VIEW_TYPE, -1)
        assertEquals(ViewType.LIST.index, savedViewTypeIndex)

        PreferenceRepository.setViewType(context, ViewType.CARD)
        assertEquals(ViewType.CARD, PreferenceRepository.viewType.value)
        val savedViewTypeIndexCard = SharedPrefHelper.getInt(context, SharedPrefHelper.KEY_VIEW_TYPE, -1)
        assertEquals(ViewType.CARD.index, savedViewTypeIndexCard)
    }

    @Test
    fun loadViewType_loadsSavedValueFromSharedPref() {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_VIEW_TYPE, ViewType.LIST.index)

        PreferenceRepository.loadViewType(context)

        assertEquals(ViewType.LIST, PreferenceRepository.viewType.value)
    }

    @Test
    fun loadViewType_invalidValueInSharedPref_defaultsToCard() {
        SharedPrefHelper.putInt(context, SharedPrefHelper.KEY_VIEW_TYPE, 999)

        PreferenceRepository.loadViewType(context)

        assertEquals(ViewType.CARD, PreferenceRepository.viewType.value)
    }
}
