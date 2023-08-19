package com.example.exoplayer_matomo

import android.content.Context
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.analytics.AnalyticsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.matomo.sdk.extra.TrackHelper

class PlayerAnalyticsListener(private val context: Context, private val player: ExoPlayer?) :
    AnalyticsListener {
    private var currentPositionTracker: Runnable? = null
    private var lastKnownPlaybackPercent: Long = -1
    private var lastKnownPlaybackPos: Long = -1

    private var currentPositionTrackerJob: Job? = null

    private fun registerCurrentPositionTracker() {
        unRegisterCurrentPositionTracker()

        currentPositionTrackerJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                val currentPosition = player!!.currentPosition
                val percent = currentPosition * 100 / player.duration
                if (lastKnownPlaybackPos != currentPosition) {
                    lastKnownPlaybackPos = currentPosition
                    lastKnownPlaybackPercent = percent

                    MainActivity.mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(),(player.currentPosition.toInt() / 1000).toString())

                    TrackHelper.track(MainActivity.mediaEvent)
                        .screen("")
                        .with(PlayerUtils.getTracker(context))

                }
                delay(10000) // Delay for 10 seconds
            }
        }
    }

    private fun unRegisterCurrentPositionTracker() {
        currentPositionTrackerJob?.cancel()
        currentPositionTrackerJob = null
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == ExoPlayer.STATE_READY && player!!.playWhenReady) {
            if (currentPositionTracker == null) {
                registerCurrentPositionTracker()
            }
        } else if (player!!.playWhenReady) {
            unRegisterCurrentPositionTracker()
            if (player.duration != 0L && player.duration <= player.currentPosition) {

            } else {
            }
        } else {
            unRegisterCurrentPositionTracker()
        }
    }
}