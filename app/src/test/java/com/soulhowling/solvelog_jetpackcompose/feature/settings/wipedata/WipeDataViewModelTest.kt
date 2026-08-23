package com.soulhowling.solvelog_jetpackcompose.feature.settings.wipedata

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.core.database.SolveLogDatabase
import com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model.TimerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
class WipeDataViewModelTest {

    private lateinit var viewModel: WipeDataViewModel
    private lateinit var application: SolveLogApplication
    private lateinit var database: SolveLogDatabase
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(
            application,
            SolveLogDatabase::class.java
        ).allowMainThreadQueries().build()
        SolveLogDatabase.setTestInstance(database)
        
        viewModel = WipeDataViewModel(application)
    }

    @After
    fun tearDown() {
        database.close()
        SolveLogDatabase.setTestInstance(null)
        Dispatchers.resetMain()
    }

    @Test
    fun wipeStatus_initialStateIsIdle() {
        assertEquals(WipeDataViewModel.WipeStatus.Idle, viewModel.wipeStatus.value)
    }

    @Test
    fun onWipeConfirm_WipeAllData_transitionsToSuccess() = runTest(testDispatcher) {
        viewModel.onWipeConfirm(WipeDataItem.WipeAllData)
        
        // Wait for status change to Success
        viewModel.wipeStatus.first { it == WipeDataViewModel.WipeStatus.Success }
        
        assertEquals(WipeDataViewModel.WipeStatus.Success, viewModel.wipeStatus.value)
    }

    @Test
    fun onWipeConfirm_WipeAllTimers_actuallyWipesTimers() = runTest(testDispatcher) {
        val db = application.database
        val timerDao = db.timerDao()
        
        // Insert a timer
        timerDao.insert(
            TimerState(
                title = "Test Timer",
                workSet = 0,
                workSeconds = 60,
                breakSeconds = 30,
            )
        )
        var timers = timerDao.getAllTimerStates().first()
        assertTrue(timers.isNotEmpty())
        
        viewModel.onWipeConfirm(WipeDataItem.WipeAllTimers)
        
        // Wait for status change to Success
        viewModel.wipeStatus.first { it == WipeDataViewModel.WipeStatus.Success }
        
        assertEquals(WipeDataViewModel.WipeStatus.Success, viewModel.wipeStatus.value)
        
        timers = timerDao.getAllTimerStates().first()
        assertTrue(timers.isEmpty())
    }

    @Test
    fun resetWipeStatus_resetsToIdle() = runTest(testDispatcher) {
        viewModel.onWipeConfirm(WipeDataItem.WipeAllData)
        
        // Wait for status change to Success
        viewModel.wipeStatus.first { it == WipeDataViewModel.WipeStatus.Success }
        
        assertEquals(WipeDataViewModel.WipeStatus.Success, viewModel.wipeStatus.value)
        
        viewModel.resetWipeStatus()
        assertEquals(WipeDataViewModel.WipeStatus.Idle, viewModel.wipeStatus.value)
    }
}
