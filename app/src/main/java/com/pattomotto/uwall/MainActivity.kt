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
    val referral: String by lazy { "?utm_source=" + BuildConfig.APPLICATION_ID + "&utm_medium=referral" }
    val unsplashSearchUrl: String by lazy { "https://unsplash.com/search/photos/" }

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

        val lastKeyword = persistentStorage.keyword
        editText.setText(lastKeyword)
        if (persistentStorage.photoUrl.isEmpty()) {
            fetchImage(lastKeyword, width, height, imageView)
        } else {
            backgroundManager.previewAndSetWallpaper(persistentStorage.photoUrl, applicationContext, imageView, progressBar)
        }

        editText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE &&
                    (event == null || !event.isShiftPressed)) {
                val text = v.text.toString()
                persistentStorage.keyword = text
                fetchImage(text, width, height, imageView)
                true
            }
            false
        }

        refreshButton.setOnClickListener {
            fetchImage(lastKeyword, width, height, imageView)
        }
        unsplashTextView.setOnClickListener {
            openWebBrowser(unsplashSearchUrl + persistentStorage.keyword + referral)
        }
        setupPhotographerTextView()

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

        setAlarm(persistentStorage.timeIntervalHr)
    }

    fun fetchImage(text: String, width: Int, height: Int, imageView: ImageView) {
        progressBar.visibility = View.VISIBLE
        backgroundManager.fetchImage(applicationContext, text, width, height) { url, randomPhoto ->
            backgroundManager.previewAndSetWallpaper(url, applicationContext, imageView, progressBar)
            persistentStorage.randomPhoto = randomPhoto
            setupPhotographerTextView()
        }
    }

    fun setAlarm(timeHr: Int) {
        disableAlarm()
        val alarmManager:AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, SampleBootReceiver::class.java)
        val alarmIntent = PendingIntent.getBroadcast(applicationContext, 0, intent, 0);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR * timeHr,
                AlarmManager.INTERVAL_HOUR * timeHr,
                alarmIntent)
        enableAlarm()
    }

    fun enableAlarm() {
        val receiver = ComponentName(applicationContext, SampleBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    fun disableAlarm() {
        val receiver = ComponentName(applicationContext, SampleBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP)
    }

    fun setupPhotographerTextView() {
        persistentStorage.randomPhoto?.let {randomPhoto ->
            photographerTextView.text = randomPhoto.user.name
            photographerTextView.setOnClickListener {
                openWebBrowser(randomPhoto.links.html + referral)
            }
        }
    }

    fun openWebBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
}

