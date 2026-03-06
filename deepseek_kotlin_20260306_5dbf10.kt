package com.youtubeproxy.player

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class YouTubeProxyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidContext(this@YouTubeProxyApplication)
            modules(appModule)
        }
    }
}