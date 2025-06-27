package io.github.samuelmarks.off_on_ml

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

import androidx.compose.runtime.Composable

@Serializable
sealed class ImageSource {
    abstract val key: String

    @Serializable
    @SerialName("local")
    data class Local(val path: String) : ImageSource() {
        override val key: String = path
    }

    @Serializable
    @SerialName("remote")
    data class Remote(val url: String) : ImageSource() {
        override val key: String = url
    }
}

expect class ImagePicker {
    @Composable
    fun registerPicker(onImagePicked: (ImageSource.Local?) -> Unit)

    fun pickImage()
}

@Composable
expect fun rememberImagePicker(onImagePicked: (ImageSource.Local?) -> Unit): ImagePicker
