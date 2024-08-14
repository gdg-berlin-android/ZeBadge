package de.berlindroid.zeapp

import de.berlindroid.zeapp.zedi.apiModule
import de.berlindroid.zeapp.zedi.servicesModule
import de.berlindroid.zeapp.zedi.viewModelModule
import de.berlindroid.zeapp.zeservices.github.zeGitHubModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

fun ZeApplication.zeStartKoin() {
    startKoin {
        androidContext(this@zeStartKoin)
        androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
        modules(apiModule, viewModelModule, servicesModule, zeGitHubModule)
    }
}
