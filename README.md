# Matomo

- Track Helper
    - It is very important to add user id using `PlayerUtils.getTracker` before sending any media event like below.
        
        ```kotlin
        PlayerUtils.getTracker(this@MainActivity)!!.defaultTrackMe.set(QueryParams.USER_ID,userInfoMap["userId"])
        ```
        
    
    - Instead of using
    
    `PlayerUtils.getTracker(this@MainActivity)!!.track(mediaEvent)` ,
    
    we’ll be using `TrackHelper.track()` to track media events like below, along side the media events, custom dimensions can be sent as well.
    
    ```kotlin
    TrackHelper.track(mediaEvent)
                        .dimension(1, userInfoMap["cd_userId"])
                        .dimension(2, if (subscriptionStatus == true)"PAID-USER" else "FREE-USER")
                        .screen("")
                        .with(PlayerUtils.getTracker(this@MainActivity))
    ```
    
    ```kotlin
    // Add media events like content seek 
    MainActivity.mediaEvent.set(PlayerAnalyticsUnit.ma_st.toString(),(player.currentPosition.toInt() / 1000).toString())
    
    // And send using Trackhelper.
                        TrackHelper.track(MainActivity.mediaEvent)
                            .screen("")
                            .with(PlayerUtils.getTracker(context))
    ```
    
- Track Selector
    
    `trackSelector` is a part of ExoPlayer's architecture that helps manage and optimize the playback experience for different devices and content. Using which we can set params like `setMaxVideoSize` or `setMaxVideoBitrate` for our free users (720p).
    
    ```kotlin
    trackSelector = DefaultTrackSelector(/* context= */this, AdaptiveTrackSelection.Factory())
            val params = DefaultTrackSelector.ParametersBuilder().apply {
                if (subscriptionStatus == false) setMaxVideoSize(1280, 720)
            }.build()
            trackSelector.parameters = params
    ```
    
    The `trackSelector` instance has to be added when instantiating the player.
    
    ```kotlin
    // Create an ExoPlayer and set it as the player for content and ads.
    // Along with TrackSelector
            player = ExoPlayer
                .Builder(this)
                .setMediaSourceFactory(mediaSourceFactory)
                .setTrackSelector(trackSelector)
                .build()
    ```
    
- Shared Preference
    - On app startup `User ID` along with `Custom Dimension 1`  `Custom Dimension 2` has to be saved in shared preference with their keys from Flutter End of the app. Which will be retrieved later on to be set and sent by the `PlayerUtils.getTracker` and `TrackHelper.track()` . If the `User ID` and `Custom Dimension 1`  `Custom Dimension 2` are not available, the they will be generated and stored for persistency. If not available the `Subscription Status` be set to **FREE-USER**
    - If the app is failed to set or retrieve the user data (`User ID` and `Custom Dimension 1`  `Custom Dimension 2`), then exceptions will be posted at MATOMO.
    
    ```jsx
    /**
     * Checks shared preferences for user data. Generates and saves missing data if needed.
     * @return A mutable map containing user data, including userId, cd_userId, and cd_SubscriptionStatus.
     */
    private fun checkSharedPrefData(): MutableMap<String, String> {
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var userId = sharedPreferences.getString("userId", null)
        var cdUserId = sharedPreferences.getString("cd_userId", null)
        var cdSubscriptionStatus = sharedPreferences.getString("cd_SubscriptionStatus", null)
        val resultMap = mutableMapOf<String, String>()
    
        // If userId is missing, generate a new one and save it to shared preferences.
        if (userId == null) {
            val newUserId = getRandomString(10) // Function to generate a new user ID
            sharedPreferences.edit().putString("userId", newUserId).apply()
            userId = newUserId
        }
        
        // If cd_userId is missing, generate a new one and save it to shared preferences.
        if (cdUserId == null) {
            val newCDUserId = getRandomString(10)
            sharedPreferences.edit().putString("cd_userId", newCDUserId).apply()
            cdUserId = newCDUserId
        }
    
        // If cd_SubscriptionStatus is missing, set it to a default value and save it to shared preferences.
        if (cdSubscriptionStatus == null) {
            val newCDSubscriptionStatus = "0"
            sharedPreferences.edit().putString("cd_SubscriptionStatus", newCDSubscriptionStatus).apply()
            cdSubscriptionStatus = newCDSubscriptionStatus
        }
    
        // Populate the resultMap with the retrieved or newly generated values.
        resultMap["userId"] = userId
        resultMap["cd_userId"] = cdUserId
        resultMap["cd_SubscriptionStatus"] = cdSubscriptionStatus
    
        return resultMap
    }
    
    /**
     * Checks user data map for exceptions and reports them if necessary.
     * @param userInfoMap A map containing user data.
     */
    private fun checkException(userInfoMap: MutableMap<String, String>) {
        // Check if any user data is missing or empty and report an exception if found.
        if (userInfoMap.isEmpty() ||
            userInfoMap["userId"].isNullOrEmpty() ||
            userInfoMap["cd_userId"].isNullOrEmpty() ||
            userInfoMap["cd_SubscriptionStatus"].isNullOrEmpty()
        ) {
            reportException()
        }
    }
    
    /**
     * Reports a custom exception using tracking mechanisms.
     */
    private fun reportException() {
        TrackHelper.track()
            .exception(Exception("Platform Channel Exception"))
            .description("Exception regarding userId, subscription status, custom dimensions")
            .fatal(false)
            .with(PlayerUtils.getTracker(this@MainActivity))
    }
    ```