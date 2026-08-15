package com.soulhowling.solvelog_jetpackcompose.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefHelper private constructor() {
    companion object {
        private const val PREF_NAME = "SolveLog_Preference"
        const val KEY_THEME_MODE = "key_theme_mode"
        const val KEY_IS_ANSWER_VISIBLE = "key_is_answer_visible"
        const val KEY_VIEW_TYPE = "key_view_type"

        private fun getSharedPref(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        private fun getEditor(context: Context): SharedPreferences.Editor {
            return getSharedPref(context).edit()
        }

        fun putInt(context: Context, key: String, value: Int) {
            getEditor(context).putInt(key, value).apply()
        }

        fun getInt(context: Context, key: String, defaultValue: Int): Int {
            return getSharedPref(context).getInt(key, defaultValue)
        }

        fun putBoolean(context: Context, key: String, value: Boolean) {
            getEditor(context).putBoolean(key, value).apply()
        }

        fun getBoolean(context: Context, key: String, defaultValue: Boolean): Boolean {
            return getSharedPref(context).getBoolean(key, defaultValue)
        }

    }
}