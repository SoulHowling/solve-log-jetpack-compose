package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class StopwatchViewModelTest {

    private lateinit var viewModel: StopwatchViewModel
    private val testDispatcher = UnconfinedTestDispatcher()
    
    private val isRunningFlow = MutableStateFlow(false)
    private val accumulatedTimeFlow = MutableStateFlow(0L)
    private val baseTimeFlow = MutableStateFlow(0L)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StopwatchViewModel(
            ApplicationProvider.getApplicationContext(),
            isRunningFlow,
            accumulatedTimeFlow,
            baseTimeFlow
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `formatTime should format milliseconds correctly - under 1 hour`() {
        val ms = 125430L // 02:05.43
        val formatted = viewModel.formatTime(ms)
        assertEquals("02:05.43", formatted)
    }

    @Test
    fun `formatTime should format milliseconds correctly - over 1 hour`() {
        val ms = 3661000L // 01:01:01.00
        val formatted = viewModel.formatTime(ms)
        assertEquals("01:01:01.00", formatted)
    }

    @Test
    fun `formatTime should handle zero`() {
        val formatted = viewModel.formatTime(0L)
        assertEquals("00:00.00", formatted)
    }

    @Test
    fun `isPaused should be true when accumulatedTime is greater than 0 and isRunning is false`() = runTest {
        isRunningFlow.value = false
        accumulatedTimeFlow.value = 100L

        val isPaused = viewModel.isPaused.first { it }
        assertTrue(isPaused)
    }

    @Test
    fun `isPaused should be false when isRunning is true`() = runTest {
        isRunningFlow.value = true
        accumulatedTimeFlow.value = 100L

        // Since it starts as false, we can't just use first { !it } because it might succeed immediately
        // and we want to ensure it's false AFTER the changes.
        // But here it is false by default.
        
        backgroundScope.launch {
            viewModel.isPaused.collect {}
        }
        
        assertFalse(viewModel.isPaused.value)
    }

    @Test
    fun `isPaused should be false when accumulatedTime is 0`() = runTest {
        isRunningFlow.value = false
        accumulatedTimeFlow.value = 0L

        backgroundScope.launch {
            viewModel.isPaused.collect {}
        }

        assertFalse(viewModel.isPaused.value)
    }
}
