package com.example.exoplayer_matomo

import android.content.Context
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
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.Util
import org.matomo.sdk.QueryParams
import org.matomo.sdk.TrackMe
import org.matomo.sdk.extra.TrackHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var player: ExoPlayer
    private lateinit var simpleExoPlayer: ExoPlayer
    private lateinit var adsLoader: ImaAdsLoader
    private lateinit var analyticsListener: PlayerAnalyticsListener
    private lateinit var trackSelector: DefaultTrackSelector

    var userInfoMap = mutableMapOf<String, String>()
    private var userID: String? = null
    private var subscriptionStatus: Boolean? = null


    companion object{ var mediaEvent = TrackMe()}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userInfoMap = checkSharedPrefData()
        checkException(userInfoMap)

        PlayerUtils.getTracker(this@MainActivity)!!.defaultTrackMe.set(QueryParams.USER_ID,userInfoMap["userId"])
        subscriptionStatus = userInfoMap["cd_SubscriptionStatus"].toBoolean()

        simpleExoPlayer = ExoPlayer.Builder(this).build()
        adsLoader = ImaAdsLoader.Builder(this).build()

        binding.tvSubsStat.text = if (subscriptionStatus == true) "paid user" else "free user"
    }




    private fun setupExoWithAdd() {
        trackSelector = DefaultTrackSelector(/* context= */this, AdaptiveTrackSelection.Factory())
        val params = DefaultTrackSelector.ParametersBuilder().apply {
            if (subscriptionStatus == false) setMaxVideoSize(1280, 720)
        }.build()
        trackSelector.parameters = params


        // Set up the factory for media sources, passing the ads loader and ad view providers.
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(this)
        val mediaSourceFactory: MediaSource.Factory =
            DefaultMediaSourceFactory(dataSourceFactory)
                .setLocalAdInsertionComponents(
                    { adsLoader }, binding.playerView
                )

        // Create an ExoPlayer and set it as the player for content and ads.
        player = ExoPlayer
            .Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setTrackSelector(trackSelector)
            .build()
        binding.playerView.player = player
        adsLoader.setPlayer(player)

        // Create the MediaItem to play, specifying the content URI and ad tag URI.
        val contentUri = Uri.parse(PlayerUtils.SAMPLE_VIDEO_URL_M3U8)
        val adTagUri = Uri.parse("")
        val mediaItem = MediaItem.Builder()
            .setUri(contentUri)
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .setAdsConfiguration(MediaItem.AdsConfiguration.Builder(adTagUri).build())
            .build()

//         Prepare the content and ad to be played with the SimpleExoPlayer.
//        player.setMediaItem(mediaItem)
        player.setMediaItems(PlayerUtils.mediaItemList)
        player.addListener(playBackStateListener())
        analyticsListener = PlayerAnalyticsListener(this,player)
        player.addAnalyticsListener(analyticsListener)
        player.prepare()
        // Set PlayWhenReady. If true, content and ads will autoplay.
        player.playWhenReady = true
    }

    private fun playBackStateListener() = object : Player.Listener{
        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == ExoPlayer.STATE_BUFFERING) {

            } else if (playbackState == ExoPlayer.STATE_READY || playbackState == 9 || playbackState == 8){

            }
        }
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

    fun getRandomString(length: Int) : String {
        val charset = "ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    private fun getRandomHex(length: Int) : String {
        val charset = "abcdef0123456789"
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }
    private fun checkSharedPrefData(): MutableMap<String, String> {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString("userId", null)
        var cdUserId = sharedPreferences.getString("cd_userId", null)
        var cdSubscriptionStatus = sharedPreferences.getString("cd_SubscriptionStatus", null)
        val resultMap = mutableMapOf<String, String>()

        if (userId == null) {
            val newUserId = getRandomString(10) // Function to generate a new user ID
            sharedPreferences.edit().putString("userId", newUserId).apply()
            userId = newUserId
        }
        if (cdUserId == null){
            val newCDUserId = getRandomString(10)
            sharedPreferences.edit().putString("cd_userId", newCDUserId).apply()
            cdUserId = newCDUserId
        }
        if (cdSubscriptionStatus == null){
            val newCDSubscriptionStatus = "0"
            sharedPreferences.edit().putString("cd_SubscriptionStatus", newCDSubscriptionStatus).apply()
            cdSubscriptionStatus = newCDSubscriptionStatus
        }

        resultMap["userId"] = userId
        resultMap["cd_userId"] = cdUserId
        resultMap["cd_SubscriptionStatus"] = cdSubscriptionStatus

        return resultMap
    }

    private fun checkException(userInfoMap: MutableMap<String, String>) {
        if (userInfoMap.isEmpty()) reportException()
        if (userInfoMap["userId"].isNullOrEmpty()) reportException()
        if (userInfoMap["cd_userId"].isNullOrEmpty()) reportException()
        if (userInfoMap["cd_SubscriptionStatus"].isNullOrEmpty()) reportException()
    }
    private fun reportException(){
        TrackHelper.track()
            .exception(Exception("Platform Channel Exception"))
            .description("Exception regarding userId, subscription status, custom dimensions")
            .fatal(false)
            .with(PlayerUtils.getTracker(this@MainActivity))
    }
}