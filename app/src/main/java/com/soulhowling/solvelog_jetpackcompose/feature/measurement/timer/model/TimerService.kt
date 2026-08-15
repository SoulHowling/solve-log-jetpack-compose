package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer.model

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import com.soulhowling.solvelog_jetpackcompose.R
import com.soulhowling.solvelog_jetpackcompose.core.SolveLogApplication
import com.soulhowling.solvelog_jetpackcompose.utils.LogUtils
import com.soulhowling.solvelog_jetpackcompose.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    private var ringtone: Ringtone? = null
    private var toneGenerator: ToneGenerator? = null

    private var tickStartTime = 0L
    private var tickStartRemaining = 0

    override fun onCreate() {
        super.onCreate()
        val solveLogApp = application as SolveLogApplication
        repository = TimerRepository(solveLogApp.database.timerDao())
        timerManager = solveLogApp.timerManager

        NotificationUtils.createNotificationChannel(
            this,
            NotificationUtils.TIMER_CHANNEL_ID,
            getString(R.string.measurement_timer_title)
        )

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
                val activeTimer = ActiveTimer(
                    state = state,
                    remainingSeconds = state.workSeconds,
                    isRunning = true,
                    initialSeconds = state.workSeconds
                )
                timerManager.updateTimer(activeTimer)
                timerManager.updateInitialWorkSet(state.workSet)

                updateNotification()
                startTicking()
            } else {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun resumeTimer() {
        val current = timerManager.timer.value ?: return
        if (current.isRunning) return

        timerManager.updateTimer(current.copy(isRunning = true))
        updateNotification()
        startTicking()
    }

    private fun pauseTimer() {
        val current = timerManager.timer.value ?: return
        if (!current.isRunning) return

        timerManager.updateTimer(current.copy(isRunning = false))
        timerJob?.cancel()
        timerJob = null
        stopSounds()
        updateNotification()
    }

    private fun resetTimer() {
        val current = timerManager.timer.value ?: return
        val resetState = current.state.copy(
            workSet = timerManager.initialWorkSet.value,
            phase = TimerPhase.WORK,
        )
        timerManager.updateTimer(ActiveTimer(
            state = resetState,
            initialSeconds = resetState.workSeconds,
            remainingSeconds = resetState.workSeconds,
            isRunning = false
        ))
        timerJob?.cancel()
        timerJob = null
        stopSounds()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun restartTimer() {
        val current = timerManager.timer.value ?: return
        val restartState = current.state.copy(
            workSet = timerManager.initialWorkSet.value,
            phase = TimerPhase.WORK,
        )
        timerManager.updateTimer(ActiveTimer(
            state = restartState,
            initialSeconds = restartState.workSeconds,
            remainingSeconds = restartState.workSeconds,
            isRunning = true
        ))
        timerJob?.cancel()
        stopSounds()
        startTicking()
        updateNotification()
    }

    private fun startTicking() {
        val currentState = timerManager.timer.value ?: return
        tickStartTime = SystemClock.elapsedRealtime()
        tickStartRemaining = currentState.remainingSeconds

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive) {
                delay(500L.milliseconds)
                val current = timerManager.timer.value ?: break
                if (!current.isRunning) break

                val elapsedMillis = SystemClock.elapsedRealtime() - tickStartTime
                val elapsedSeconds = (elapsedMillis / 1000).toInt()
                val newRemaining = (tickStartRemaining - elapsedSeconds).coerceAtLeast(0)

                if (newRemaining != current.remainingSeconds) {
                    val tickedTimer = current.copy(remainingSeconds = newRemaining)

                    if (newRemaining in 1..3) {
                        val currentGenerator = toneGenerator
                        if (currentGenerator != null) {
                            launch(Dispatchers.Main) {
                                try {
                                    currentGenerator.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                                } catch (e: Exception) {
                                    LogUtils.e("Failed to startTone", e)
                                }
                            }
                        }
                    }

                    if (newRemaining <= 0) {
                        launch {
                            playNotificationSound()
                        }

                        val nextTimer = tickedTimer.nextPhase()
                        timerManager.updateTimer(nextTimer)
                        updateNotification()

                        if (nextTimer.isRunning) {
                            tickStartTime = SystemClock.elapsedRealtime()
                            tickStartRemaining = nextTimer.remainingSeconds
                        } else {
                            timerJob = null
                            break
                        }
                    } else {
                        timerManager.updateTimer(tickedTimer)
                        updateNotification()
                    }
                }
            }
        }
    }

    private suspend fun playNotificationSound() {
        val localRingtone = withContext(Dispatchers.IO) {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(applicationContext, notification).apply {
                    audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
            } catch (e: Exception) {
                LogUtils.e("Failed to play notification sound", e)
                null
            }
        } ?: return
        withContext(Dispatchers.Main) {
            ringtone?.stop()
            ringtone = localRingtone
            ringtone?.play()
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
        val timer = timerManager.timer.value
        val isRunning = timer?.isRunning == true
        val phaseText = when (timer?.phase) {
            TimerPhase.WORK -> getString(R.string.timer_page_phase_work)
            TimerPhase.BREAK -> getString(R.string.timer_page_phase_break)
            TimerPhase.COMPLETE -> getString(R.string.timer_page_phase_complete)
            null -> ""
        }

        val content = if (timer != null) {
            val total = timerManager.initialWorkSet.value
            val current = if (timer.phase == TimerPhase.WORK) {
                (total - timer.workSet + 1).coerceAtLeast(1)
            } else {
                (total - timer.workSet).coerceAtLeast(1)
            }
            val setsText = getString(R.string.timer_page_work_set_format, current, total)
            val timeText = String.format(
                Locale.ROOT,
                "%02d:%02d",
                timer.remainingSeconds / 60,
                timer.remainingSeconds % 60
            )
            "$phaseText ($setsText) - $timeText"
        } else ""

        val builder = NotificationUtils.getBaseNotificationBuilder(
            this,
            NotificationUtils.TIMER_CHANNEL_ID,
            timer?.title ?: getString(R.string.measurement_timer_title),
            content
        ).apply {
            setOngoing(isRunning)
            setAutoCancel(false)
        }

        // Add actions
        if (timer != null && timer.phase != TimerPhase.COMPLETE) {
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
        } else if (timer?.phase == TimerPhase.COMPLETE) {
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
        serviceScope.cancel()
        toneGenerator?.release()
        toneGenerator = null
        stopSounds()
        timerManager.updateTimer(null)
        timerManager.updateInitialWorkSet(0)
        super.onDestroy()
    }

    private fun stopSounds() {
        ringtone?.stop()
        ringtone = null
    }
}
