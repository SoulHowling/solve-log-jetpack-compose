package com.soulhowling.solvelog_jetpackcompose.feature.measurement.stopwatch

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.utils.NotificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StopwatchService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_RESET = "ACTION_RESET"

        private const val NOTIFICATION_ID = 1001

        private val _baseTime = MutableStateFlow(0L)
        /**
         * System.currentTimeMillis() when the stopwatch was at 0.
         * Used for Chronometer and UI calculation.
         */
        val baseTime: StateFlow<Long> = _baseTime.asStateFlow()

        private val _accumulatedTime = MutableStateFlow(0L)
        /**
         * Total elapsed time when paused.
         */
        val accumulatedTime: StateFlow<Long> = _accumulatedTime.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private var startTime = 0L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startStopwatch()
            ACTION_PAUSE -> pauseStopwatch()
            ACTION_RESUME -> resumeStopwatch()
            ACTION_RESET -> resetStopwatch()
        }
        return START_NOT_STICKY
    }

    private fun startStopwatch() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTime = System.currentTimeMillis()
        _accumulatedTime.value = 0L
        _baseTime.value = startTime
        
        NotificationUtils.createNotificationChannel(
            this,
            NotificationUtils.STOPWATCH_CHANNEL_ID,
            getString(R.string.stopwatch_notification_channel_name),
            getString(R.string.stopwatch_notification_channel_description)
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    private fun resumeStopwatch() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTime = System.currentTimeMillis()
        _baseTime.value = startTime - _accumulatedTime.value
        updateNotification()
    }

    private fun pauseStopwatch() {
        if (!_isRunning.value) return
        _isRunning.value = false
        _accumulatedTime.value += System.currentTimeMillis() - startTime
        updateNotification()
    }

    private fun resetStopwatch() {
        _isRunning.value = false
        _accumulatedTime.value = 0L
        startTime = 0L
        _baseTime.value = 0L
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotification(): Notification {
        val builder = NotificationUtils.getBaseNotificationBuilder(
            this,
            NotificationUtils.STOPWATCH_CHANNEL_ID,
            getString(R.string.measurement_stopwatch_title),
            ""
        ).apply {
            setOngoing(_isRunning.value)
            setAutoCancel(false)
            
            if (_isRunning.value) {
                // システムに通知内タイマーの更新を任せる
                setUsesChronometer(true)
                setWhen(_baseTime.value)
            } else {
                setUsesChronometer(false)
                setContentText(formatTimeForNotification(_accumulatedTime.value))
            }
        }

        // アクションの追加
        if (_isRunning.value) {
            builder.addAction(
                0, getString(R.string.measurement_stop_button),
                getServicePendingIntent(ACTION_PAUSE)
            )
        } else {
            builder.addAction(
                0, getString(R.string.measurement_resume_button),
                getServicePendingIntent(ACTION_RESUME)
            )
        }

        builder.addAction(
            0, getString(R.string.measurement_reset_button),
            getServicePendingIntent(ACTION_RESET)
        )

        return builder.build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, StopwatchService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatTimeForNotification(ms: Long): String {
        val seconds = (ms / 1000) % 60
        val minutes = (ms / (1000 * 60)) % 60
        val hours = (ms / (1000 * 60 * 60))
        return if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    override fun onDestroy() {
        _isRunning.value = false
        super.onDestroy()
    }
}
