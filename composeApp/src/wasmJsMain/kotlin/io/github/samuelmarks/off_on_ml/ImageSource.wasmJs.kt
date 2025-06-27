package io.github.samuelmarks.off_on_ml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import kotlinx.browser.document

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.get

actual class ImagePicker(
    private val onImagePicked: (ImageSource.Local?) -> Unit
) {
    private lateinit var input: HTMLInputElement

    @Composable
    actual fun registerPicker(onImagePicked: (ImageSource.Local?) -> Unit) {
        remember {
            input = (document.createElement("input") as HTMLInputElement).apply {
                type = "file"
                accept = "image/*"
                onchange = { event ->
                    val file = (event.target as? HTMLInputElement)?.files?.get(0)
                    if (file != null) {
                        val url = URL.createObjectURL(file)
                        onImagePicked(ImageSource.Local(url))
                    } else {
                        onImagePicked(null)
                    }
                }
            }
        }
    }

    actual fun pickImage() {
        input.click()
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageSource.Local?) -> Unit): ImagePicker {
    val picker = remember { ImagePicker(onImagePicked) }
    picker.registerPicker(onImagePicked)
    return picker
}
