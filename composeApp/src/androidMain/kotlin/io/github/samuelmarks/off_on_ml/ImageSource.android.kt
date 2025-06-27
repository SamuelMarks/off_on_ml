package io.github.samuelmarks.off_on_ml

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

actual class ImagePicker(
    private val onImagePicked: (ImageSource.Local?) -> Unit
) {
    private lateinit var galleryLauncher: androidx.activity.result.ActivityResultLauncher<String>

    @Composable
    actual fun registerPicker(onImagePicked: (ImageSource.Local?) -> Unit) {
        galleryLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            onImagePicked(uri?.let { ImageSource.Local(it.toString()) })
        }
    }

    actual fun pickImage() {
        galleryLauncher.launch("image/*")
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageSource.Local?) -> Unit): ImagePicker {
    val picker = remember { ImagePicker(onImagePicked) }
    picker.registerPicker(onImagePicked)
    return picker
}
