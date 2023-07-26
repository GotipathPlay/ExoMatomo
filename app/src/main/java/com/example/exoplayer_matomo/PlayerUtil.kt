package com.example.exoplayer_matomo

import android.content.Context
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder

class PlayerUtils {
    companion object{
        const val SAMPLE_VIDEO_URL = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        const val VMAP_ADS_TAG = "https://pubads.g.doubleclick.net/gampad/ads?iu=/21775744923/external/vmap_ad_samples&sz=640x480&cust_params=sample_ar%3Dpremidpost&ciu_szs=300x250&gdfp_req=1&ad_rule=1&output=vmap&unviewed_position_start=1&env=vp&impl=s&cmsid=496&vid=short_onecue&correlator="

        private var tracker: Tracker? = null

        @Synchronized
        fun getTracker(context: Context): Tracker? {
            if (tracker == null) {
                tracker = TrackerBuilder.createDefault("https://analytics.deepto.tv/matomo.php", 5)
                    .build(Matomo.getInstance(context))
            }
            return tracker
        }
    }

    /** LOG TAGS **/
/*
    Log.d("MATOMO_ANALYTICS","from pa: ${mediaEvent.toMap().toString()}")
    Log.d("MATOMO_ANALYTICS", "CURRENT: ${lastKnownPlaybackPercent}% (${player.currentPosition.toInt()/1000})")
    Log.d("MATOMO_ANALYTICS","after data post:"+ MainActivity.mediaEvent.toMap().toString())
*/

}