package com.soulhowling.solvelog_jetpackcompose.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeProviderTest {

    @Test
    fun defaultTimeProvider_returnsCurrentTimeMillis() {
        val before = System.currentTimeMillis()
        val providerTime = DefaultTimeProvider.currentTimeMillis()
        val after = System.currentTimeMillis()

        assertTrue(providerTime in before..after)
    }

    @Test
    fun fakeTimeProvider_returnsFixedTime() {
        val fixedTime = 123456789L
        val fakeTimeProvider = object : TimeProvider {
            override fun currentTimeMillis(): Long = fixedTime
        }

        assertEquals(fixedTime, fakeTimeProvider.currentTimeMillis())
    }
}
