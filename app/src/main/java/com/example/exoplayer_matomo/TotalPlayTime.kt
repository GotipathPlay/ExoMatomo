package com.example.exoplayer_matomo

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TotalPlayTime {
    private var startTime = 0L
    private var elapsedTime = 0L
    private var isRunning = false
    private var job: Job? = null

    // Start the timer
    fun start() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            job = CoroutineScope(Dispatchers.Default).launch {
                while (isRunning) {
                    delay(1000) // Update every 10 milliseconds (adjust as needed)
                    elapsedTime = System.currentTimeMillis() - startTime
                }
            }
        }
    }

    // Stop the timer
    fun stop() {
        if (isRunning) {
            job?.cancel()
            isRunning = false
        }
    }

    // Pause the timer
    fun pause() {
        stop()
    }

    // Get the elapsed time in milliseconds
    fun update(): Long {
        return elapsedTime
    }

    fun reset() {
        elapsedTime = 0L
    }
}