package io.github.samuelmarks.off_on_ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.graphics.scale

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlin.random.Random

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URL

private fun getContext(): Context = MainApplication.INSTANCE

actual suspend fun resizeImage(source: ImageSource, resolution: Resolution): ImageSource.Local? = withContext(Dispatchers.IO) {
    try {
        val inputStream = getInputStream(source)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeStream(getInputStream(source), null, options) // First pass to get dimensions

        options.inSampleSize = calculateInSampleSize(options, resolution.width, resolution.height)
        options.inJustDecodeBounds = false

        val bitmap = BitmapFactory.decodeStream(getInputStream(source), null, options) ?: return@withContext null
        val scaledBitmap = bitmap.scale(resolution.width, resolution.height, filter = true)
        bitmap.recycle()

        val tempFile = File(getContext().cacheDir, "resized_${Random.nextLong()}.jpg")
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

private fun getInputStream(source: ImageSource): InputStream {
    return when (source) {
        is ImageSource.Local -> getContext().contentResolver.openInputStream(Uri.parse(source.path))!!
        is ImageSource.Remote -> URL(source.url).openStream()
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

// You might need to expose your Application context via a static field for this file.
// In your `MainApplication.kt` (or custom Application class):
// companion object {
//    lateinit var INSTANCE: MainApplication
// }
// override fun onCreate() {
//    super.onCreate()
//    INSTANCE = this
// }
