package com.sonza.music

import android.app.Application
import com.sonza.music.core.logging.SonzaLogger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SonzaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SonzaLogger.i("SonzaApplication", "SONZA Audiophile Player initialized successfully")
    }
}
