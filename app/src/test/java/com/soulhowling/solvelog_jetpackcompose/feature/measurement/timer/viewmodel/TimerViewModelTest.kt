package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.Screens
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class TimerViewModelTest {

    private lateinit var viewModel: TimerViewModel
    private lateinit var application: SolveLogApplication
    private val testDispatcher = UnconfinedTestDispatcher()
    private val timerId = 1

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        runBlocking {
            withContext(Dispatchers.IO) {
                application.database.clearAllTables()
                // Insert a test timer
                application.database.timerDao().insert(
                    TimerState(
                        id = timerId,
                        title = "Test Timer",
                        workSet = 4,
                        workSeconds = 60,
                        breakSeconds = 30
                    )
                )
            }
        }

        val savedStateHandle = SavedStateHandle(mapOf(Screens.ARG_TIMER_STATE_ID to timerId))
        viewModel = TimerViewModel(application, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel should load timer from repository on init`() = runTest {
        backgroundScope.launch { viewModel.displayedTimer.collect {} }
        
        val timer = viewModel.displayedTimer.filterNotNull().first()
        
        assertNotNull(timer)
        assertEquals("Test Timer", timer.title)
        assertEquals(timerId, timer.id)
    }

    @Test
    fun `totalSets should reflect timer workSet`() = runTest {
        backgroundScope.launch { viewModel.totalSets.collect {} }
        
        val total = viewModel.totalSets.first { it != 0 }

        assertEquals(4, total)
    }
}
