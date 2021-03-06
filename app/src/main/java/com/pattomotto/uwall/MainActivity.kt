package com.pattomotto.uwall

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.*

class MainActivity : AppCompatActivity() {


    lateinit var persistentStorage: PersistentStorage
    lateinit var progressBar: ProgressBar
    lateinit var photographerTextView: TextView
    lateinit var unsplashTextView: TextView

    val backgroundManager: RefreshWallpaperManager by lazy { RefreshWallpaperManager() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        disableAlarm()

        persistentStorage = PersistentStorage(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val w = window
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        var editText: EditText = findViewById(R.id.queryEditText)
        var imageView: ImageView = findViewById(R.id.imageView)
        var refreshButton: Button = findViewById(R.id.refreshButton)
        var seekBar: SeekBar = findViewById(R.id.seekBar)
        val timeIntervalTextView: TextView = findViewById(R.id.timeIntervalTextView)
        progressBar = findViewById(R.id.progressBar)
        photographerTextView = findViewById(R.id.photographerTextView)
        unsplashTextView = findViewById(R.id.unsplashTextView)

        val point = Point()
        windowManager.defaultDisplay.getRealSize(point)
        val width = point.x
        val height = point.y

        if (persistentStorage.photoUrl.isEmpty()) {
            fetchImage(persistentStorage.keyword, width, height, imageView)
            setAlarm(persistentStorage.timeIntervalHr)
        } else {
            backgroundManager.previewAndSetWallpaper(persistentStorage.photoUrl, applicationContext, imageView, progressBar)
        }

        // setup view
        setupPhotographerTextView()
        editText.setText(persistentStorage.keyword)

        // listener
        editText.setOnEditorActionListener { v, actionId, event ->
            val text = v.text.toString()
            persistentStorage.keyword = text

            if (actionId == EditorInfo.IME_ACTION_DONE &&
                    (event == null || !event.isShiftPressed)) {
                fetchImage(text, width, height, imageView)
                true
            }
            false
        }
        refreshButton.setOnClickListener {
            fetchImage(persistentStorage.keyword, width, height, imageView)
        }
        unsplashTextView.setOnClickListener {
            persistentStorage.photoInfo?.source?.let {
                unsplashTextView.text = it.sourceName
                openWebBrowser(it.sourceUrl + persistentStorage.keyword + it.refParam)
            }
        }
        persistentStorage.timeIntervalHr.let {
            seekBar.progress = it
            timeIntervalTextView.text = "$it h"
        }
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeIntervalTextView.text = "$progress h"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    persistentStorage.timeIntervalHr = it.progress
                    setAlarm(it.progress)
                }
            }
        })
    }

    private fun fetchImage(text: String, width: Int, height: Int, imageView: ImageView) {
        progressBar.visibility = View.VISIBLE
        backgroundManager.fetchImage(applicationContext, text, width, height) { url, randomPhoto ->
            backgroundManager.previewAndSetWallpaper(url, applicationContext, imageView, progressBar)
            persistentStorage.photoInfo = photoInfoFromUnsplashPhoto(randomPhoto)
            setupPhotographerTextView()
        }
    }

    private fun setAlarm(timeHr: Int) {
        disableAlarm()
        val alarmManager:AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, TimerBootReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR * timeHr,
                AlarmManager.INTERVAL_HOUR * timeHr,
                alarmIntent)
        enableAlarm()
    }

    private fun enableAlarm() {
        val receiver = ComponentName(applicationContext, TimerBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    private fun disableAlarm() {
        val receiver = ComponentName(applicationContext, TimerBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    private fun setupPhotographerTextView() {
        persistentStorage.photoInfo?.let { photoInfo ->
            photographerTextView.text = photoInfo.photographerName
            photographerTextView.setOnClickListener {
                openWebBrowser(photoInfo.photoUrl + photoInfo.source.refParam)
            }
        }
    }

    private fun openWebBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

