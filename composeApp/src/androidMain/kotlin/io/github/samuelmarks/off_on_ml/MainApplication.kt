package io.github.samuelmarks.off_on_ml

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initOnApplicationCreate(this)
    }
}
