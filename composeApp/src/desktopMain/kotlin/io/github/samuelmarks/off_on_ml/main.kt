package io.github.samuelmarks.off_on_ml

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OffOnMl",
    ) {
        App()
    }
}