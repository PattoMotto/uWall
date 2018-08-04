package com.pattomotto.uwall

import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log

class TimerBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val persistentStorage = PersistentStorage(context)
        val refreshBackgroundManager = RefreshWallpaperManager()
        refreshBackgroundManager.fetchImage(context) { url, randomPhoto ->
            persistentStorage.photoInfo = photoInfoFromUnsplashPhoto(randomPhoto)
            refreshBackgroundManager.setWallpaper(url, context) {
                pendingResult.finish()
            }
        }
    }

}