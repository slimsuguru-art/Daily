package com.example.daily_app

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FocusTimer : ViewModel() {
    private val _totalDurationSeconds = MutableStateFlow(25 * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    private var timerJob: Job? = null

    fun setDurationMinutes(minutes: Int) {
        if (minutes !in 1..180 || _isRunning.value) return
        timerJob?.cancel()
        val seconds = minutes * 60
        _totalDurationSeconds.value = seconds
        _remainingSeconds.value = seconds
        _isFinished.value = false
    }

    fun start() {
        if (_isRunning.value || _remainingSeconds.value <= 0) return
        _isRunning.value = true
        _isFinished.value = false
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000)
                if (_isRunning.value && _remainingSeconds.value > 0) {
                    _remainingSeconds.value -= 1
                }
            }
            if (_remainingSeconds.value <= 0) {
                _isRunning.value = false
                _isFinished.value = true
            }
        }
    }

    fun pause() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun reset() {
        _isRunning.value = false
        timerJob?.cancel()
        timerJob = null
        _remainingSeconds.value = _totalDurationSeconds.value
        _isFinished.value = false
    }

    @SuppressLint("DefaultLocale")
    fun formattedTime(): String {
        val minutes = _remainingSeconds.value / 60
        val seconds = _remainingSeconds.value % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
