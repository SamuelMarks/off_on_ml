package io.github.samuelmarks.off_on_ml

data class Resolution(val width: Int, val height: Int)

/**
 * Resizes an image from a given source to a target resolution.
 * This is a suspending function and should be called from a coroutine.
 *
 * @param source The original image source (local or remote).
 * @param resolution The target width and height.
 * @return A new [ImageSource.Local] pointing to the resized, temporary image file, or null on failure.
 */
expect suspend fun resizeImage(
    source: ImageSource,
    resolution: Resolution
): ImageSource.Local?
