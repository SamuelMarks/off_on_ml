package io.github.samuelmarks.off_on_ml

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

import platform.PhotosUI.*
import platform.UIKit.*
import platform.darwin.NSObject

actual class ImagePicker(
    private val onImagePicked: (ImageSource.Local?) -> Unit
) {
    private val rootViewController: UIViewController?
        get() = UIApplication.sharedApplication.keyWindow?.rootViewController

    actual fun pickImage() {
        if (rootViewController != null) {
            val picker = PHPickerViewController(PHPickerConfiguration().apply {
                selectionLimit = 1
                filter = PHPickerFilter.imagesFilter()
            })
            picker.delegate = createPickerDelegate()
            rootViewController!!.presentViewController(picker, animated = true, completion = null)
        }
    }

    private fun createPickerDelegate() = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)

            val result = didFinishPicking.firstOrNull() as? PHPickerResult
            result?.itemProvider?.loadFileRepresentationForTypeIdentifier(
                "public.image"
            ) { url, error ->
                if (error != null) {
                    println("Error loading image: $error")
                    onImagePicked(null)
                } else if (url != null) {
                    onImagePicked(ImageSource.Local(url.path!!))
                }
            }
        }
    }

    // No-op on iOS for the Composable register
    @Composable actual fun registerPicker(onImagePicked: (ImageSource.Local?) -> Unit) {}
}

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageSource.Local?) -> Unit): ImagePicker {
    return remember { ImagePicker(onImagePicked) }
}
