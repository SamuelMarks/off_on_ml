package io.github.samuelmarks.off_on_ml

import android.os.Build
import android.content.Context

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

internal object PlatformDependencies {
    lateinit var appContext: Context
}

fun initOnApplicationCreate(context: Context) {
    PlatformDependencies.appContext = context.applicationContext
}
