package io.github.samuelmarks.off_on_ml

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import offonml.composeapp.generated.resources.Res
import offonml.composeapp.generated.resources.camera_roll_24px
import offonml.composeapp.generated.resources.compose_multiplatform
import offonml.composeapp.generated.resources.contract_edit_24px
import org.jetbrains.compose.resources.painterResource

/**
 * A launcher that presents the ImagePickerUI in a dialog.
 * This is the primary entry point for using the image picker component.
 *
 * @param show A boolean state to control the visibility of the picker.
 * @param targetResolution The target resolution for resizing images. If null, no resizing is done.
 * @param onDismissRequest Called when the user dismisses the dialog (e.g., by clicking outside).
 * @param onImageSelected Called with the final, processed ImageSource when an image is successfully picked.
 */
@Composable
fun ImagePickerLauncher(
    show: Boolean,
    targetResolution: Resolution? = null,
    onDismissRequest: () -> Unit,
    onImageSelected: (ImageSource) -> Unit,
) {
    if (show) {
        val scope = rememberCoroutineScope()
        // Remember the component; it will be recreated if targetResolution changes
        val component = remember(targetResolution) { ImagePickerComponent(scope, targetResolution) }

        // Effect to listen for the final selected image from the component
        LaunchedEffect(component.selectedImage.collectAsState().value) {
            val image = component.selectedImage.value
            // Ensure we only proceed when an image is selected and resizing is complete
            if (image != null && !component.isResizing.value) {
                onImageSelected(image)
                onDismissRequest() // Automatically dismiss after selection
            }
        }

        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Allows custom width
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                    .clickable(
                        // Allow dismissing by clicking the scrim
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null, // No ripple effect
                        onClick = onDismissRequest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.8f)
                        // Prevent the card click from propagating to the background scrim
                        .clickable(enabled = false, onClick = {}),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    ImagePickerView(component)
                }
            }
        }
    }
}

/**
 * The internal UI for the image picker.
 * It's displayed inside the dialog managed by ImagePickerLauncher.
 */
@Composable
private fun ImagePickerView(component: ImagePickerComponent) {
    val selectedImage by component.selectedImage.collectAsState()
    val recentImages by component.recentImages.collectAsState()
    val isResizing by component.isResizing.collectAsState()

    // Remember the platform-specific gallery picker
    val galleryPicker = rememberImagePicker { imageSource ->
        if (imageSource != null) {
            // This triggers the full selection and resizing flow
            component.onImageSelectedFromPicker(imageSource)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { galleryPicker.pickImage() }, modifier = Modifier.weight(1f)) {
                Icon(painterResource(Res.drawable.contract_edit_24px), contentDescription = "Gallery")
                Spacer(Modifier.width(4.dp))
                Text("From Gallery")
            }
        }

        UrlPicker(onUrlSelected = { component.onUrlSelected(it) })

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // 2. Main Image Display Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Take up available space
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            when {
                isResizing -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(8.dp))
                        Text("Resizing Image...")
                    }
                }
                selectedImage != null -> {
                    val painterResource = asyncPainterResource(data = selectedImage!!.key)
                    KamelImage(
                        resource = painterResource,
                        contentDescription = "Selected image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onLoading = { CircularProgressIndicator() },
                        onFailure = { Text("Failed to load image") }
                    )
                }
                else -> {
                    Text("Select an Image", textAlign = TextAlign.Center)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 3. Recent Images Section
        if (recentImages.isNotEmpty()) {
            Text("Recently Selected", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recentImages, key = { it.key }) { image ->
                    RecentImageItem(image) {
                        component.onRecentSelected(image)
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlPicker(onUrlSelected: (String) -> Unit) {
    var url by remember { mutableStateOf("") }
    OutlinedTextField(
        value = url,
        onValueChange = { url = it },
        label = { Text("Or paste image URL") },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        singleLine = true,
        trailingIcon = {
            IconButton(
                onClick = {
                    onUrlSelected(url)
                    url = "" // Clear after submitting
                },
                enabled = url.isNotBlank()
            ) {
                Icon(painterResource(Res.drawable.camera_roll_24px), contentDescription = "Add URL")
            }
        }
    )
}

@Composable
private fun RecentImageItem(image: ImageSource, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
    ) {
        val painterResource = asyncPainterResource(data = image.key)
        KamelImage(
            resource = painterResource,
            contentDescription = "Recent image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            onLoading = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            },
            onFailure = { /* Silent failure for thumbnails is acceptable */ }
        )
    }
}
