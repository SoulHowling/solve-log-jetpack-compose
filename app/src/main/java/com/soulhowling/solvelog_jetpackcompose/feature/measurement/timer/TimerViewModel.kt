package com.soulhowling.solvelog_jetpackcompose.feature.measurement.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.soulhowling.solvelog_jetpackcompose.SolveLogApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TimerViewModel(application: Application): AndroidViewModel(application) {
    private val repository: TimerRepository
    private val alarm: Alarm
    
    // UI表示用のタイマー状態流 (毎秒更新される)
    private val _displayTimers = MutableStateFlow<List<TimerState>>(emptyList())
    val displayTimers: StateFlow<List<TimerState>> = _displayTimers.asStateFlow()

    // データベースからのマスターデータ
    private val timers: StateFlow<List<TimerState>>

    private var tickerJob: Job? = null

    init {
        val solveLogApp = application as SolveLogApplication
        val db = solveLogApp.database
        repository = TimerRepository(db.timerDao())
        alarm = Alarm(application)

        timers = repository.allTimers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // DBの更新を監視し、表示用リストの項目を同期する
        viewModelScope.launch {
            timers.collectLatest { dbTimers ->
                val currentDisplay = _displayTimers.value
                
                // 初回読み込み時、またはリストの項目（ID）に変化があった場合のみ同期
                // 毎秒のカウントダウン中は、表示用リストの値を優先するため、
                // カウントダウン中（isRunning）の項目の値は上書きしない
                _displayTimers.value = dbTimers.map { dbTimer ->
                    val displayTimer = currentDisplay.find { it.id == dbTimer.id }
                    if (displayTimer != null && displayTimer.isRunning) {
                        // 実行中の場合は、メモリ上の状態を維持しつつDBの最新情報を反映（必要があれば）
                        // ここではカウントのズレを防ぐため、実行中はメモリ側を優先
                        displayTimer
                    } else {
                        dbTimer
                    }
                }
                updateTicker()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
        tickerJob = null
        CoroutineScope(Dispatchers.IO + NonCancellable).launch {
            displayTimers.value.forEach { timer ->
                pause(timer.id)
            }
        }
    }

    private fun updateTicker() {
        val anyRunning = _displayTimers.value.any { it.isTicking() }
        if (anyRunning) {
            if (tickerJob == null || tickerJob?.isActive != true) {
                startTicker()
            }
        } else {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    private fun startTicker() {
        tickerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                tickAll()
            }
        }
    }

    private fun tickAll() {
        val currentList = _displayTimers.value
        var finishedTimer: TimerState? = null
        
        val updatedList = currentList.map { timer ->
            if (timer.isTicking()) {
                val next = timer.tick()
                if (next.isFinished()) {
                    finishedTimer = next
                }
                next
            } else {
                timer
            }
        }
        _displayTimers.value = updatedList
        
        finishedTimer?.let {
            alarm.play()
            // 終了したタイマーの状態をDBに保存
            viewModelScope.launch {
                repository.upsertTimer(it.copy(isRunning = false))
            }
        }

        // 全て止まったらティッカーを止める
        if (updatedList.none { it.isTicking() }) {
            tickerJob?.cancel()
            tickerJob = null
        }
    }

    fun insertTimer(minutes: Int, seconds: Int) {
        viewModelScope.launch {
            val totalSeconds = minutes * 60 + seconds
            if (totalSeconds <= 0) return@launch
            val newTimer = repository.createTimer(totalSeconds = totalSeconds)
            repository.insertTimer(newTimer)
        }
    }

    fun deleteTimer(timerId: Int) {
        viewModelScope.launch {
            repository.deleteTimer(timerId)
        }
    }

    fun start(timerId: Int) {
        updateTimerState(timerId) { it.copy(isRunning = true) }
    }

    fun pause(timerId: Int) {
        updateTimerState(timerId) { it.copy(isRunning = false) }
    }

    fun stop(timerId: Int) {
        alarm.stop()
        updateTimerState(timerId) { it.copy(isRunning = false, remainingSeconds = it.initialSeconds) }
    }

    fun reset(timerId: Int) {
        alarm.stop()
        updateTimerState(timerId) { it.copy(isRunning = false, remainingSeconds = it.initialSeconds) }
    }

    private fun updateTimerState(timerId: Int, transform: (TimerState) -> TimerState) {
        val currentList = _displayTimers.value
        val updatedList = currentList.map { 
            if (it.id == timerId) transform(it) else it 
        }
        _displayTimers.value = updatedList
        
        // 状態変化（開始・一時停止・リセット等）をDBに保存
        updatedList.find { it.id == timerId }?.let { updatedTimer ->
            viewModelScope.launch {
                repository.upsertTimer(updatedTimer)
            }
        }
        
        updateTicker()
    }

}