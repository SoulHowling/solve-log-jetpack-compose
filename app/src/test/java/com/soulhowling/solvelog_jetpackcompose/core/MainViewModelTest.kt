package com.soulhowling.solvelog_jetpackcompose.core

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.data.PreferenceRepository
import com.soulhowling.solvelog_jetpackcompose.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class MainViewModelTest {

    private lateinit var application: SolveLogApplication
    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = MainViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() {
        assertTrue(viewModel.isBottomBarVisible.value)
        assertNull(viewModel.appBarActions.value)
    }

    @Test
    fun setTitle_updatesTitle() = runTest {
        backgroundScope.launch { viewModel.title.collect {} }
        viewModel.setTitle("Test Title")
        val currentTitle = viewModel.title.first { it == "Test Title" }
        assertEquals("Test Title", currentTitle)
    }

    @Test
    fun setDefaultTitle_updatesTitleWhenCustomTitleIsNull() = runTest {
        backgroundScope.launch { viewModel.title.collect {} }
        val expectedTitle = application.getString(R.string.app_name)
        viewModel.setDefaultTitle(R.string.app_name)
        val currentTitle = viewModel.title.first { it == expectedTitle }
        assertEquals(expectedTitle, currentTitle)
    }

    @Test
    fun customTitle_overridesDefaultTitle() = runTest {
        backgroundScope.launch { viewModel.title.collect {} }
        viewModel.setDefaultTitle(R.string.app_name)
        viewModel.setTitle("Custom Overriding Title")
        val currentTitle = viewModel.title.first { it == "Custom Overriding Title" }
        assertEquals("Custom Overriding Title", currentTitle)
    }

    @Test
    fun clearingCustomTitle_revertsToDefaultTitle() = runTest {
        backgroundScope.launch { viewModel.title.collect {} }
        val expectedDefault = application.getString(R.string.app_name)
        viewModel.setDefaultTitle(R.string.app_name)
        viewModel.setTitle("Custom Title")
        assertEquals("Custom Title", viewModel.title.first { it == "Custom Title" })

        viewModel.setTitle(null)
        assertEquals(expectedDefault, viewModel.title.first { it == expectedDefault })
    }

    @Test
    fun setActions_updatesAppBarActions() {
        assertNull(viewModel.appBarActions.value)

        viewModel.setActions {}
        assertNotNull(viewModel.appBarActions.value)

        viewModel.setActions(null)
        assertNull(viewModel.appBarActions.value)
    }

    @Test
    fun setBottomBarVisible_updatesVisibilityState() {
        assertTrue(viewModel.isBottomBarVisible.value)

        viewModel.setBottomBarVisible(false)
        assertEquals(false, viewModel.isBottomBarVisible.value)

        viewModel.setBottomBarVisible(true)
        assertEquals(true, viewModel.isBottomBarVisible.value)
    }

    @Test
    fun loadThemeMode_loadsFromPreferenceRepository() {
        PreferenceRepository.setThemeMode(application, ThemeMode.DARK)
        viewModel.loadThemeMode()
        assertEquals(ThemeMode.DARK, viewModel.themeMode.value)

        PreferenceRepository.setThemeMode(application, ThemeMode.LIGHT)
        viewModel.loadThemeMode()
        assertEquals(ThemeMode.LIGHT, viewModel.themeMode.value)
    }
}
