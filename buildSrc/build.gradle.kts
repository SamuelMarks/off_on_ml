plugins {
    `kotlin-dsl`
}

repositories {
    maven {
        url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
    gradlePluginPortal()
}
