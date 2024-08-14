package de.berlindroid.zeapp

import android.app.Application
import timber.log.Timber

class ZeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        zeStartKoin()
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }
}
