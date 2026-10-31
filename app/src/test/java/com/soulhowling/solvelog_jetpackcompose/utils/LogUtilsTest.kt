package com.soulhowling.solvelog_jetpackcompose.utils

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class LogUtilsTest {

    @Test
    fun logUtils_methodsDoNotThrow() {
        LogUtils.v("Verbose test message")
        LogUtils.d("Debug test message")
        LogUtils.i("Info test message")
        LogUtils.w("Warning test message")
        LogUtils.w("Warning with throwable", RuntimeException("Warning exception"))
        LogUtils.e("Error test message")
        LogUtils.e("Error with throwable", RuntimeException("Error exception"))
    }
}
