package io.github.samuelmarks.off_on_ml

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.*
import platform.UIKit.*
import kotlin.random.Random

@OptIn(ExperimentalForeignApi::class)
actual suspend fun resizeImage(source: ImageSource, resolution: Resolution): ImageSource.Local? = withContext(Dispatchers.Default) {
    try {
        val data = when (source) {
            is ImageSource.Local -> NSData.dataWithContentsOfFile(source.path)
            is ImageSource.Remote -> NSData.dataWithContentsOfURL(NSURL.URLWithString(source.url)!!)
        } ?: return@withContext null

        val image = UIImage.imageWithData(data) ?: return@withContext null

        val newSize = CGSizeMake(resolution.width.toDouble(), resolution.height.toDouble())
        val renderer = UIGraphicsImageRenderer(newSize)
        val newImage = renderer.imageWithActions {
            image.drawInRect(CGRectMake(0.0, 0.0, resolution.width.toDouble(), resolution.height.toDouble()))
        }

        val resizedData = UIImageJPEGRepresentation(newImage, 0.9) ?: return@withContext null

        val tempPath = NSTemporaryDirectory() + "resized_${Random.nextLong()}.jpg"
        resizedData.writeToFile(tempPath, atomically = true)

        ImageSource.Local(tempPath)
    } catch (e: Exception) {
        println("Image resizing failed: ${e.message}")
        null
    }
}
