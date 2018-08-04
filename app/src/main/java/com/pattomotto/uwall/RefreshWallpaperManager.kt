package com.pattomotto.uwall

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.result.Result
import kotlin.concurrent.thread

class RefreshWallpaperManager {

    fun fetchImage(context: Context, queryText: String, width: Int, height: Int, callback: (String, UnsplashPhoto) -> Unit) {
        val persistentStorage = PersistentStorage(context)
        val encodedQuery = Uri.encode(queryText)
        persistentStorage.randomPhotoUrl = "https://api.unsplash.com/photos/random?count=1&orientation=portrait&query=$encodedQuery&w=$width&h=$height"
        fetchImage(context, callback)
    }

    fun fetchImage(context: Context, callback: (String, UnsplashPhoto) -> Unit) {
        val unsplashApiKey = BuildConfig.UnsplashApiKey
        val persistentStorage = PersistentStorage(context)
        val header = Pair("Authorization","Client-ID $unsplashApiKey")
        val url = persistentStorage.randomPhotoUrl
        Fuel.get(url)
                .header(header).responseObject { request: Request, response: Response, result: Result<List<UnsplashPhoto>, FuelError> ->
                    result.component1()?.map {
                        val randomPhoto = it
                        randomPhoto.urls.custom.let {
                            persistentStorage.photoUrl = it
                            Handler(Looper.getMainLooper()).post {
                                callback(it, randomPhoto)
                            }
                        }
                    }
                }
    }

    fun previewAndSetWallpaper(url: String, context: Context, imageView: ImageView, progressBar: ProgressBar) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions().centerCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(object : RequestListener<Drawable> {
                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        (resource as? BitmapDrawable)?.let {
                            setDeviceWallpaper(it.bitmap, context)
                        }
                        progressBar.visibility = View.GONE
                        return false
                    }

                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        progressBar.visibility = View.GONE
                        return false
                    }
                })
                .into(imageView)
    }

    fun setWallpaper(url: String, context: Context, callback: (() -> Unit)? = null) {
        Glide.with(context)
                .load(url)
                .apply(RequestOptions().centerCrop())
                .into(object : SimpleTarget<Drawable>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        (resource as? BitmapDrawable)?.let {
                            setDeviceWallpaper(it.bitmap, context)
                            callback?.invoke()
                        }
                    }
                })
    }

    fun setDeviceWallpaper(bitmap: Bitmap, context: Context) {
        thread {
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }
        }
    }
}