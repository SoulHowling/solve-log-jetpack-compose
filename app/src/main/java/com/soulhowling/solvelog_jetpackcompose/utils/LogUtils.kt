package com.soulhowling.solvelog_jetpackcompose.utils

import android.util.Log
import com.soulhowling.solvelog_jetpackcompose.BuildConfig

/**
 * 汎用的なログ出力ユーティリティ。
 *
 * 要件:
 * 1. デバッグビルドでは全レベル、リリースビルドではINFO以上を出力する。
 * 2. Throwable().stackTrace を利用して呼び出し元のクラス名からTAGを動的に生成する。
 * 3. メッセージに呼び出し元のクラス名と行番号を含め、Logcatからクリックでジャンプ可能にする。
 */
object LogUtils {

    private val isDebug = BuildConfig.DEBUG

    fun v(msg: String) {
        if (isDebug) {
            val (tag, info) = getCallerInfo()
            Log.v(tag, "$info: $msg")
        }
    }

    fun d(msg: String) {
        if (isDebug) {
            val (tag, info) = getCallerInfo()
            Log.d(tag, "$info: $msg")
        }
    }

    fun i(msg: String) {
        val (tag, info) = getCallerInfo()
        Log.i(tag, "$info: $msg")
    }

    fun w(msg: String, tr: Throwable? = null) {
        val (tag, info) = getCallerInfo()
        if (tr != null) {
            Log.w(tag, "$info: $msg", tr)
        } else {
            Log.w(tag, "$info: $msg")
        }
    }

    fun e(msg: String, tr: Throwable? = null) {
        val (tag, info) = getCallerInfo()
        if (tr != null) {
            Log.e(tag, "$info: $msg", tr)
        } else {
            Log.e(tag, "$info: $msg")
        }
    }

    private fun getCallerInfo(): Pair<String, String> {
        val stackTrace = Throwable().stackTrace
        val caller = stackTrace.firstOrNull { it.className != LogUtils::class.java.name }
        
        return if (caller != null) {
            val tag = caller.className.substringAfterLast('.')
            val info = "(${caller.fileName}:${caller.lineNumber})"
            tag to info
        } else {
            "LogUtils" to "unknown"
        }
    }
}
