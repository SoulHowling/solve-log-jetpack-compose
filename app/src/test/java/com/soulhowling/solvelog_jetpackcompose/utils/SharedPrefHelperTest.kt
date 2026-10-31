package com.soulhowling.solvelog_jetpackcompose.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [36])
class SharedPrefHelperTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearPreferences()
    }

    @After
    fun tearDown() {
        clearPreferences()
    }

    private fun clearPreferences() {
        context.getSharedPreferences("SolveLog_Preference", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun constants_areCorrect() {
        assertEquals("key_theme_mode", SharedPrefHelper.KEY_THEME_MODE)
        assertEquals("key_is_answer_visible", SharedPrefHelper.KEY_IS_ANSWER_VISIBLE)
        assertEquals("key_view_type", SharedPrefHelper.KEY_VIEW_TYPE)
    }

    @Test
    fun putInt_and_getInt_returnsSavedValue() {
        val defaultValue = 42
        assertEquals(defaultValue, SharedPrefHelper.getInt(context, "custom_int_key", defaultValue))

        SharedPrefHelper.putInt(context, "custom_int_key", 100)
        assertEquals(100, SharedPrefHelper.getInt(context, "custom_int_key", defaultValue))
    }

    @Test
    fun putBoolean_and_getBoolean_returnsSavedValue() {
        assertFalse(SharedPrefHelper.getBoolean(context, "custom_bool_key", false))
        assertTrue(SharedPrefHelper.getBoolean(context, "custom_bool_key", true))

        SharedPrefHelper.putBoolean(context, "custom_bool_key", true)
        assertTrue(SharedPrefHelper.getBoolean(context, "custom_bool_key", false))

        SharedPrefHelper.putBoolean(context, "custom_bool_key", false)
        assertFalse(SharedPrefHelper.getBoolean(context, "custom_bool_key", true))
    }
}
