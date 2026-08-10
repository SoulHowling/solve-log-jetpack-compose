package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import com.soulhowling.solvelog_jetpackcompose.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

class TimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_RESET = "ACTION_RESET"
        const val ACTION_RESTART = "ACTION_RESTART"

        const val EXTRA_TIMER_ID = "EXTRA_TIMER_ID"

        private const val NOTIFICATION_ID = 1002
    }

    private lateinit var repository: TimerRepository
    private lateinit var timerManager: TimerManager
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var timerJob: Job? = null

    private var toneGenerator: ToneGenerator? = null

    override fun onCreate() {
        super.onCreate()
        val solveLogApp = application as SolveLogApplication
        repository = TimerRepository(solveLogApp.database.timerDao())
        timerManager = solveLogApp.timerManager
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            LogUtils.e("Failed to create ToneGenerator", e)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action != ACTION_RESET) {
            promptToForeground()
        }

        val timerId = intent?.getIntExtra(EXTRA_TIMER_ID, -1) ?: -1
        when (intent?.action) {
            ACTION_START -> {
                if (timerId != -1) startTimer(timerId)
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_RESUME -> resumeTimer()
            ACTION_RESET -> resetTimer()
            ACTION_RESTART -> restartTimer()
        }
        return START_NOT_STICKY
    }

    private fun startTimer(timerId: Int) {
        serviceScope.launch {
            val state = repository.getTimerBy(timerId).first()
            if (state != null) {
                val newState = state.copy(isRunning = true, lastModified = System.currentTimeMillis())
                timerManager.updateTimerState(newState)
                timerManager.updateInitialWorkSet(state.workSet)

                NotificationUtils.createNotificationChannel(
                    this@TimerService,
                    NotificationUtils.TIMER_CHANNEL_ID,
                    getString(R.string.measurement_timer_title),
                    ""
                )

                updateNotification()
                startTicking()
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun resumeTimer() {
        val current = timerManager.timerState.value ?: return
        if (current.isRunning) return

        timerManager.updateTimerState(current.copy(isRunning = true, lastModified = System.currentTimeMillis()))
        updateNotification()
        startTicking()
    }

    private fun pauseTimer() {
        val current = timerManager.timerState.value ?: return
        if (!current.isRunning) return

        timerManager.updateTimerState(current.copy(isRunning = false, lastModified = System.currentTimeMillis()))
        timerJob?.cancel()
        timerJob = null
        updateNotification()
    }

    private fun resetTimer() {
        val current = timerManager.timerState.value ?: return
        timerManager.updateTimerState(current.copy(
            workSet = timerManager.initialWorkSet.value,
            phase = TimerPhase.WORK,
            initialSeconds = current.workSeconds,
            remainingSeconds = current.workSeconds,
            isRunning = false,
            lastModified = System.currentTimeMillis()
        ))
        timerJob?.cancel()
        timerJob = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restartTimer() {
        val current = timerManager.timerState.value ?: return
        timerManager.updateTimerState(current.copy(
            workSet = timerManager.initialWorkSet.value,
            phase = TimerPhase.WORK,
            initialSeconds = current.workSeconds,
            remainingSeconds = current.workSeconds,
            isRunning = true,
            lastModified = System.currentTimeMillis()
        ))
        timerJob?.cancel()
        startTicking()
        updateNotification()
    }

    private fun startTicking() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(1000L.milliseconds)
                val currentState = timerManager.timerState.value ?: break
                if (!currentState.isRunning) break

                val tickedState = currentState.tick()

                if (tickedState.remainingSeconds in 1..3) {
                    launch(Dispatchers.Default) {
                        try {
                            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                        } catch (e: Exception) {
                            LogUtils.e("Failed to startTone", e)
                        }
                    }
                }

                if (tickedState.remainingSeconds <= 0) {
                    launch(Dispatchers.Default) {
                        playNotificationSound()
                    }

                    val nextState = tickedState.nextPhase()
                    timerManager.updateTimerState(nextState)
                    updateNotification()

                    if (!nextState.isRunning) {
                        timerJob = null
                        break
                    }
                } else {
                    timerManager.updateTimerState(tickedState)
                    updateNotification()
                }
            }
        }
    }

    private suspend fun playNotificationSound() {
        withContext(Dispatchers.IO) {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(applicationContext, notification)

                val attributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                r.audioAttributes = attributes

                r.play()
            } catch (e: Exception) {
                LogUtils.e("Failed to play notification sound", e)
            }
        }
    }

    private fun promptToForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, createNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val state = timerManager.timerState.value
        val isRunning = state?.isRunning == true
        val phaseText = when (state?.phase) {
            TimerPhase.WORK -> getString(R.string.timer_page_phase_work)
            TimerPhase.BREAK -> getString(R.string.timer_page_phase_break)
            TimerPhase.COMPLETE -> getString(R.string.timer_page_phase_complete)
            null -> ""
        }

        val content = if (state != null) {
            val total = timerManager.initialWorkSet.value
            val current = if (state.phase == TimerPhase.WORK) {
                (total - state.workSet + 1).coerceAtLeast(1)
            } else {
                (total - state.workSet).coerceAtLeast(1)
            }
            val setsText = getString(R.string.timer_page_work_set_format, current, total)
            val timeText = String.format(
                Locale.ROOT,
                "%02d:%02d",
                state.remainingSeconds / 60,
                state.remainingSeconds % 60
            )
            "$phaseText ($setsText) - $timeText"
        } else ""

        val builder = NotificationUtils.getBaseNotificationBuilder(
            this,
            NotificationUtils.TIMER_CHANNEL_ID,
            state?.title ?: getString(R.string.measurement_timer_title),
            content
        ).apply {
            setOngoing(isRunning)
            setAutoCancel(false)
        }

        // Add actions
        if (state != null && state.phase != TimerPhase.COMPLETE) {
            if (isRunning) {
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
        } else if (state?.phase == TimerPhase.COMPLETE) {
            builder.addAction(
                0, getString(R.string.measurement_restart_button),
                getServicePendingIntent(ACTION_RESTART)
            )
        }

        builder.addAction(
            0, getString(R.string.measurement_reset_button),
            getServicePendingIntent(ACTION_RESET)
        )

        return builder.build()
    }

    private fun getServicePendingIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent, PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        timerJob?.cancel()
        toneGenerator?.release()
        toneGenerator = null
        timerManager.updateTimerState(null)
        timerManager.updateInitialWorkSet(0)
        super.onDestroy()
    }
}