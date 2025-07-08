package io.github.samuelmarks.off_on_ml

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

// This is your expect fun from commonMain
/*
expect suspend fun resizeImage(
    source: ImageSource,
    resolution: Resolution
): ImageSource.Local?
*/

actual suspend fun resizeImage(source: ImageSource, resolution: Resolution): ImageSource.Local? = withContext(Dispatchers.IO) {
    // Get the context from our initialized dependency holder
    val context = PlatformDependencies.appContext

    try {
        // Read the full image into a byte array once. This avoids issues with
        // trying to read from the same InputStream multiple times.
        val imageBytes = getInputStream(source, context)?.use { it.readBytes() }
            ?: return@withContext null

        // First pass: decode bounds to get original dimensions and calculate sample size
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

        options.inSampleSize = calculateInSampleSize(options, resolution.width, resolution.height)
        options.inJustDecodeBounds = false

        // Second pass: decode the bitmap with the calculated sample size
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            ?: return@withContext null

        // Scale the downsampled bitmap to the final exact resolution
        val scaledBitmap = bitmap.scale(resolution.width, resolution.height, filter = true)
        if (scaledBitmap != bitmap) {
            bitmap.recycle() // Recycle the intermediate bitmap if a new one was created
        }

        // Save the final bitmap to a temporary file in the cache directory
        val tempFile = File(context.cacheDir, "resized_${System.currentTimeMillis()}.jpg")
        FileOutputStream(tempFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        scaledBitmap.recycle()

        ImageSource.Local(Uri.fromFile(tempFile).toString())
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getInputStream(source: ImageSource, context: android.content.Context): InputStream? {
    return try {
        when (source) {
            is ImageSource.Local -> context.contentResolver.openInputStream(Uri.parse(source.path))
            is ImageSource.Remote -> URL(source.url).openStream()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.run { outHeight to outWidth }
    var inSampleSize = 1
    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
