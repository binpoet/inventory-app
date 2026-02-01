package com.rsa.app

import android.app.Application

/**
 * Application class for global app initialization
 */
class App : Application() {
    
    companion object {
        lateinit var instance: App
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
