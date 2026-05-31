package com.corbstech.sumo

import android.app.Application
import org.koin.android.ext.koin.androidContext
import platform.di.initKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@App)
        }
    }
}
