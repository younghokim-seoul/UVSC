package com.cm.uvsc

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.cm.uvsc.BuildConfig
import timber.log.Timber

@HiltAndroidApp
class UvscApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

    }
}