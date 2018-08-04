package com.pattomotto.uwall

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson


data class PersistentStorage(val context: Context){

    private val keywordKey= "keyword"
    private val randomPhotoUrlKey = "randomPhotoUrl"
    private val photoUrlKey = "photoUrl"
    private val timeIntervalHrKey = "timeIntervalHr"
    private val photoInfoKey = "photoInfo"

    private val prefKey = "uWall"
    private val defaultKeyword = "landscape"

    private val sharePref: SharedPreferences by lazy { context.getSharedPreferences(prefKey, Context.MODE_PRIVATE) }
    private val gson: Gson by lazy { Gson() }

    var keyword: String
        get() = sharePref.getString(keywordKey, defaultKeyword)
        set(value) =  with(sharePref.edit()) {
            putString(keywordKey, value)
            commit()
        }


    var randomPhotoUrl: String
        get() = sharePref.getString(randomPhotoUrlKey, "")
        set(value) =  with(sharePref.edit()) {
            putString(randomPhotoUrlKey, value)
            commit()
        }

    var photoUrl: String
        get() = sharePref.getString(photoUrlKey, "")
        set(value) =  with(sharePref.edit()) {
            putString(photoUrlKey, value)
            commit()
        }

    var timeIntervalHr: Int
        get() = sharePref.getInt(timeIntervalHrKey, 24)
        set(value) =  with(sharePref.edit()) {
            putInt(timeIntervalHrKey, value)
            commit()
        }

    var photoInfo: PhotoInfo?
        get() = sharePref.getString(photoInfoKey,"").let {
            if (it.isEmpty()) { null } else {
                gson.fromJson(it, PhotoInfo::class.java)
            }
        }
        set(value) = with(sharePref.edit()) {
            putString(photoInfoKey, gson.toJson(value))
            commit()
        }
}