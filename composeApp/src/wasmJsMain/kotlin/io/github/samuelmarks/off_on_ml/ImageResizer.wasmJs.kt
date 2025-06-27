package io.github.samuelmarks.off_on_ml

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.Image
import org.w3c.dom.url.URL
import kotlin.coroutines.resume

val imageQualityPercentage: JsNumber = js("""0.9""")

actual suspend fun resizeImage(source: ImageSource, resolution: Resolution): ImageSource.Local? {
    return suspendCancellableCoroutine { continuation ->
        val imageUrl = when (source) {
            is ImageSource.Local -> source.path // This will be a blob URL from the picker
            is ImageSource.Remote -> source.url
        }

        val image = Image()
        image.src = imageUrl
        // Required for remote images to avoid canvas tainting when loading from other domains
        image.crossOrigin = "Anonymous"

        image.onload = {
            try {
                val canvas = document.createElement("canvas") as HTMLCanvasElement
                canvas.width = resolution.width
                canvas.height = resolution.height
                val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

                ctx.drawImage(image, 0.0, 0.0, resolution.width.toDouble(), resolution.height.toDouble())

                // Get a new blob URL from the canvas content
                canvas.toBlob({ blob ->
                    if (continuation.isActive) {
                        if (blob != null) {
                            val newUrl = URL.createObjectURL(blob)
                            continuation.resume(ImageSource.Local(newUrl))
                        } else {
                            continuation.resume(null)
                        }
                    }
                }, "image/jpeg", imageQualityPercentage)

            } catch (e: Exception) {
                println("Error resizing image on canvas: $e")
                if (continuation.isActive) {
                    continuation.resume(null)
                }
            }
            null // FIX 1: Explicitly return null to satisfy the event handler's return type (JsAny?)
        }

        image.onerror = { _, _, _, _, error ->
            println("Failed to load image for resizing from $imageUrl $error")
            if (continuation.isActive) {
                continuation.resume(null)
            }
            null
        }
    }
}