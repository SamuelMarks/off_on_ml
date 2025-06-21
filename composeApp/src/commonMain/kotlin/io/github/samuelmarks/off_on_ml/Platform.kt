package io.github.samuelmarks.off_on_ml

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform