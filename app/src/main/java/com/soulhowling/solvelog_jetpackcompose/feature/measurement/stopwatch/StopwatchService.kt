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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class StopwatchService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_RESET = "ACTION_RESET"

        private const val NOTIFICATION_ID = 1001

        private val _elapsedTime = MutableStateFlow(0L)
        val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private var startTime = 0L
        private var accumulatedTime = 0L
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
        accumulatedTime = 0L
        
        NotificationUtils.createNotificationChannel(
            this,
            NotificationUtils.STOPWATCH_CHANNEL_ID,
            getString(R.string.stopwatch_notification_channel_name),
            getString(R.string.stopwatch_notification_channel_description)
        )
        
        startTicker()
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
        startTicker()
        updateNotification()
    }

    private fun pauseStopwatch() {
        if (!_isRunning.value) return
        _isRunning.value = false
        accumulatedTime += System.currentTimeMillis() - startTime
        _elapsedTime.value = accumulatedTime
        timerJob?.cancel()
        updateNotification()
    }

    private fun resetStopwatch() {
        _isRunning.value = false
        accumulatedTime = 0L
        startTime = 0L
        _elapsedTime.value = 0L
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTicker() {
        timerJob?.cancel()
        timerJob = serviceScope.launch(Dispatchers.Default) {
            var lastNotificationUpdateTime = 0L
            while (true) {
                val currentElapsed = accumulatedTime + (System.currentTimeMillis() - startTime)
                _elapsedTime.value = currentElapsed
                
                if (currentElapsed - lastNotificationUpdateTime >= 1000L) {
                    updateNotification()
                    lastNotificationUpdateTime = currentElapsed
                }
                delay(10.milliseconds)
            }
        }
    }

    private fun createNotification(): Notification {
        val formatTime = formatTimeForNotification(_elapsedTime.value)

        val builder = NotificationUtils.getBaseNotificationBuilder(
            this,
            NotificationUtils.STOPWATCH_CHANNEL_ID,
            getString(R.string.measurement_stopwatch_title),
            formatTime
        ).apply {
            setOngoing(_isRunning.value)
            setAutoCancel(false)
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
        serviceScope.cancel()
        super.onDestroy()
    }
}
