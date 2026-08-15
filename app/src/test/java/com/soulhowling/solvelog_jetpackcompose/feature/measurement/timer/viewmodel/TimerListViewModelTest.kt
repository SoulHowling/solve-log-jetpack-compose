package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.viewmodel

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class TimerListViewModelTest {

    private lateinit var viewModel: TimerListViewModel
    private lateinit var application: SolveLogApplication
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        runBlocking {
            withContext(Dispatchers.IO) {
                application.database.clearAllTables()
            }
        }
        
        viewModel = TimerListViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `insertTimer should add a new timer to the list`() = runTest {
        backgroundScope.launch { viewModel.displayTimers.collect {} }
        
        viewModel.insertTimer("New Timer", 4, 25, 0, 5, 0)
        
        val timers = viewModel.displayTimers.first { it.isNotEmpty() }

        assertEquals(1, timers.size)
        assertEquals("New Timer", timers[0].title)
    }

    @Test
    fun `deleteTimer should remove the timer from the list`() = runTest {
        backgroundScope.launch { viewModel.displayTimers.collect {} }
        
        viewModel.insertTimer("To Delete", 4, 25, 0, 5, 0)
        
        val initialTimers = viewModel.displayTimers.first { it.isNotEmpty() }
        val id = initialTimers[0].id
        
        viewModel.deleteTimer(id)
        
        val finalTimers = viewModel.displayTimers.first { it.isEmpty() }
        assertTrue(finalTimers.isEmpty())
    }

    @Test
    fun `updateTimer should change timer settings`() = runTest {
        backgroundScope.launch { viewModel.displayTimers.collect {} }
        
        viewModel.insertTimer("Original", 4, 25, 0, 5, 0)
        
        val timers = viewModel.displayTimers.first { it.isNotEmpty() }
        val originalTimer = timers[0]
        
        viewModel.updateTimer(originalTimer.state, "Updated", 8, 10, 0, 2, 0)
        
        val updatedTimers =
            viewModel.displayTimers.first { it.isNotEmpty() && it[0].title == "Updated" }

        assertEquals(1, updatedTimers.size)
        assertEquals("Updated", updatedTimers[0].title)
        assertEquals(8, updatedTimers[0].workSet)
    }
}
