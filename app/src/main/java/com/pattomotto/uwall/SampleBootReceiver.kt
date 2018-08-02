package com.pattomotto.uwall

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log

class SampleBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("SampleBootReceiver", "onReceive")
        val pendingResult = goAsync()
        val refreshBackgroundManager = RefreshWallpaperManager()
        refreshBackgroundManager.fetchImage(context) { url, _ ->
            Log.d("SampleBootReceiver", url)
            refreshBackgroundManager.setWallpaper(url, context) {
                Log.d("SampleBootReceiver", "completed")
                pendingResult.finish()
            }
        }
    }

}