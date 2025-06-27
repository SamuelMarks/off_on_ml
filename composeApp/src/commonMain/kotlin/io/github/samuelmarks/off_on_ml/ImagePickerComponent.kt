package io.github.samuelmarks.off_on_ml

import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImagePickerComponent(
    // A scope is needed to launch the resizing coroutine
    private val scope: CoroutineScope,
    private val targetResolution: Resolution? = null
) {
    // ... (selectedImage and recentImages StateFlows are the same)
    private val _selectedImage = MutableStateFlow<ImageSource?>(null)
    val selectedImage: StateFlow<ImageSource?> = _selectedImage.asStateFlow()

    private val _recentImages = MutableStateFlow<List<ImageSource>>(emptyList())
    val recentImages: StateFlow<List<ImageSource>> = _recentImages.asStateFlow()

    private val _isResizing = MutableStateFlow(false)
    val isResizing: StateFlow<Boolean> = _isResizing.asStateFlow()

    // This function now triggers the full (optional) resizing flow
    fun onImageSelectedFromPicker(source: ImageSource) {
        if (targetResolution != null) {
            scope.launch {
                _isResizing.value = true
                _selectedImage.value = null // Clear previous image
                val resizedImage = resizeImage(source, targetResolution)
                onImageProcessed(resizedImage)
                _isResizing.value = false
            }
        } else {
            onImageProcessed(source)
        }
    }

    // A unified function to update state after processing
    private fun onImageProcessed(image: ImageSource?) {
        _selectedImage.value = image
        if (image != null) {
            _recentImages.update { currentList ->
                listOf(image) + currentList.filterNot { it.key == image.key }
                    .take(5)
            }
        }
    }

    fun onUrlSelected(url: String) {
        if (url.isNotBlank()) {
            onImageSelectedFromPicker(ImageSource.Remote(url))
        }
    }

    fun onRecentSelected(image: ImageSource) {
        // Recents are already processed, so we can just select them
        _selectedImage.value = image
    }
}
