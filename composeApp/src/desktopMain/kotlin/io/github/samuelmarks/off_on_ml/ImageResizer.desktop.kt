package io.github.samuelmarks.off_on_ml

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

actual suspend fun resizeImage(source: ImageSource, resolution: Resolution): ImageSource.Local? = withContext(Dispatchers.IO) {
    try {
        val originalImage: BufferedImage = when (source) {
            is ImageSource.Local -> ImageIO.read(File(source.path))
            is ImageSource.Remote -> ImageIO.read(URL(source.url))
        }

        val scaledImage = originalImage.getScaledInstance(resolution.width, resolution.height, Image.SCALE_SMOOTH)
        val outputImage = BufferedImage(resolution.width, resolution.height, BufferedImage.TYPE_INT_RGB)

        outputImage.createGraphics().apply {
            drawImage(scaledImage, 0, 0, null)
            dispose()
        }

        val tempFile = File.createTempFile("resized_", ".jpg")
        ImageIO.write(outputImage, "jpg", tempFile)

        ImageSource.Local(tempFile.path)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
