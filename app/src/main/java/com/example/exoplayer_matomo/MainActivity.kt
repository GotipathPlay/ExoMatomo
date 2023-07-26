package com.example.exoplayer_matomo

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.example.exoplayer_matomo.databinding.ActivityMainBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import org.matomo.sdk.TrackMe

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding
    lateinit var simpleExoPlayer: ExoPlayer
    lateinit var player: ExoPlayer
    lateinit var adsLoader: ImaAdsLoader
    lateinit var analyticsListener : PlayerAnalyticsListener

    companion object{
        var mediaEvent = TrackMe()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        simpleExoPlayer = ExoPlayer.Builder(this).build()
        adsLoader = ImaAdsLoader.Builder(this).build()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            setupExoWithAdd()
            binding.playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()

        if (Util.SDK_INT <= 23) {
            setupExoWithAdd()
            binding.playerView.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            binding.playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23){
            binding.playerView.onPause()
            releasePlayer()
        }
    }

    override fun onDestroy() {
        adsLoader.release()
        player.release()
        super.onDestroy()
    }

    private fun releasePlayer() {
        adsLoader.setPlayer(null)
        binding.playerView.player = null
        player.removeAnalyticsListener(analyticsListener)
        player.removeListener(playBackStateListener())
        player.release()
    }

    private fun setupExoWithAdd() {
        // Set up the factory for media sources, passing the ads loader and ad view providers.
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(dataSourceFactory)
                .setLocalAdInsertionComponents(
                    { adsLoader }, binding.playerView
                )

        // Create an ExoPlayer and set it as the player for content and ads.
        player = ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory).build()
        binding.playerView.player = player
        adsLoader.setPlayer(player)

        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        val contentUri = Uri.parse(PlayerUtils.SAMPLE_VIDEO_URL)
        val adTagUri = Uri.parse("")
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMimeType(MimeTypes.APPLICATION_MP4)
            .setAdsConfiguration(MediaItem.AdsConfiguration.Builder(adTagUri).build())
            .build()

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        player.setMediaItem(mediaItem)
        player.addListener(playBackStateListener())
        //MATOMO
        analyticsListener = PlayerAnalyticsListener(this,player)
        player.addAnalyticsListener(analyticsListener)
        player.prepare()
        // Set PlayWhenReady. If true, content and ads will autoplay.
        player.playWhenReady = true
    }

    private fun playBackStateListener() = object : Player.Listener{
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_BUFFERING) {

            }
            else if (playbackState == ExoPlayer.STATE_READY){

                mediaEvent.set(PlayerAnalyticsUnit.ma_id.toString(), "goPlayID_FLUTTER")
                mediaEvent.set(PlayerAnalyticsUnit.ma_re.toString(), "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                mediaEvent.set(PlayerAnalyticsUnit.ma_mt.toString(), "video")
                mediaEvent.set(PlayerAnalyticsUnit.ma_ti.toString(), "BigBuckBunny")
                mediaEvent.set(PlayerAnalyticsUnit.ma_pn.toString(), "Media3_Flutter")

                mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(), "11")
                mediaEvent.set(PlayerAnalyticsUnit.ma_le.toString(), "596")
                mediaEvent.set(PlayerAnalyticsUnit.ma_ps.toString(), "2")
                mediaEvent.set(PlayerAnalyticsUnit.ma_ttp.toString(), "3")
                mediaEvent.set(PlayerAnalyticsUnit.ma_w.toString(), "1280")

                mediaEvent.set(PlayerAnalyticsUnit.ma_h.toString(), "720")
                mediaEvent.set(PlayerAnalyticsUnit.ma_fs.toString(), "0")
                mediaEvent.set(PlayerAnalyticsUnit.ma_se.toString(), "")

                PlayerUtils.getTracker(this@MainActivity)!!.track(mediaEvent)
                PlayerUtils.getTracker(this@MainActivity)!!.dispatch()
            }
        }
    }
}