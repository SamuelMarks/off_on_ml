package io.github.samuelmarks.off_on_ml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class ImagePicker(
    private val onImagePicked: (ImageSource.Local?) -> Unit
) {
    // AWT FileDialog is simpler and more native-looking on some systems
    private val fileDialog = FileDialog(null as Frame?, "Select an image", FileDialog.LOAD).apply {
        isMultipleMode = false
        // Filter for common image types
        setFilenameFilter { _, name ->
            name.endsWith(".png", true) || name.endsWith(".jpg", true) || name.endsWith(".jpeg", true)
        }
    }

    actual fun pickImage() {
        fileDialog.isVisible = true
        val directory = fileDialog.directory
        val file = fileDialog.file

        if (directory != null && file != null) {
            val imagePath = File(directory, file).path
            onImagePicked(ImageSource.Local(imagePath))
        } else {
            onImagePicked(null)
        }
    }

    // No-op for Desktop
    @Composable
    actual fun registerPicker(onImagePicked: (ImageSource.Local?) -> Unit) {}
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageSource.Local?) -> Unit): ImagePicker {
    return remember { ImagePicker(onImagePicked) }
}
