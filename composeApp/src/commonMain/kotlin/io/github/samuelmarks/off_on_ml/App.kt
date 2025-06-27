package io.github.samuelmarks.off_on_ml

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import offonml.composeapp.generated.resources.Res
import offonml.composeapp.generated.resources.contract_edit_24px
import offonml.composeapp.generated.resources.photo_camera_24px
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.ui.tooling.preview.Preview

// Updated model lists to reflect real Gemini model IDs
val offlineModels: List<String> =
    listOf("google/medgemma-4b-it", "google/medgemma-4b-pt", "google/medgemma-27b-text-it")
val onlineModels: List<String> =
    listOf("google/gemini-1.5-flash-latest", "google/gemini-1.5-pro-latest", "google/gemini-1.0-pro")

@Composable
@Preview
fun App() {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(systemInDarkTheme) }

    // State for API Key, Models, and AI interaction
    var apiKey by remember { mutableStateOf(settings.getString(SettingsKeys.GOOGLE_API_KEY, "")) }
    var selectedOnlineModel by remember { mutableStateOf(settings.getString(SettingsKeys.SELECTED_ONLINE_MODEL, onlineModels.first())) }
    var selectedOfflineModel by remember { mutableStateOf(settings.getString(SettingsKeys.SELECTED_OFFLINE_MODEL, offlineModels.first())) }

    // State for prompts (hoisted from child screens)
    var textPrompt by remember { mutableStateOf("") }
    var imagePrompt by remember { mutableStateOf<ImageSource?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    // State for AI response
    var aiResponse by remember { mutableStateOf("Your AI response will appear here.") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    MaterialTheme(colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = colorScheme.background) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text("Off/On ML") },
                        actions = { Switch(checked = isDarkTheme.value, onCheckedChange = { isDarkTheme.value = it }) }
                    )
                },
                bottomBar = {
                    BottomAppBar {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDarkTheme.value) {
                                ModelChooser(isOffline = true, selectedOfflineModel) { newSelection ->
                                    selectedOfflineModel = newSelection
                                    settings.putString(SettingsKeys.SELECTED_OFFLINE_MODEL, newSelection)
                                }
                            } else {
                                ModelChooser(isOffline = false, selectedOnlineModel) { newSelection ->
                                    selectedOnlineModel = newSelection
                                    settings.putString(SettingsKeys.SELECTED_ONLINE_MODEL, newSelection)
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier.padding(innerPadding).fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ApiKeyInput(apiKey) { newKey ->
                        apiKey = newKey
                        settings.putString(SettingsKeys.GOOGLE_API_KEY, newKey)
                    }

                    PromptTabs(
                        selectedTabIndex = selectedTabIndex,
                        onTabSelected = { selectedTabIndex = it },
                        textPrompt = textPrompt,
                        onTextPromptChange = { textPrompt = it },
                        imagePrompt = imagePrompt,
                        onImagePromptChange = { imagePrompt = it }
                    )

                    // The AI processing button
                    Button(
                        onClick = {
                            scope.launch {
                                if (apiKey.isBlank()) {
                                    aiResponse = "Please enter your Google API key above."
                                    return@launch
                                }
                                isLoading = true
                                val service = AIModelService(apiKey)
                                val finalPrompt = if (selectedTabIndex == 0) "Describe this image." else textPrompt

                                // For this demo, we are only sending the text prompt.
                                // The SDK supports image inputs, which can be added later.
                                aiResponse = service.generateContent(selectedOnlineModel, finalPrompt)
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && (textPrompt.isNotBlank() || imagePrompt != null)
                    ) {
                        Text("Generate Content with Gemini")
                    }

                    // Response Area
                    Card(modifier = Modifier.fillMaxWidth().weight(1f)) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator()
                            } else {
                                Text(aiResponse, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Composables ---

@Composable
fun ApiKeyInput(apiKey: String, onApiKeyChange: (String) -> Unit) {
    OutlinedTextField(
        value = apiKey,
        onValueChange = onApiKeyChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Google API Key") },
        placeholder = { Text("Enter your key here") },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Info, contentDescription = "API Key") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelChooser(isOffline: Boolean, selectedOption: String, onOptionSelected: (String) -> Unit) {
    val options = if (isOffline) offlineModels else onlineModels
    val prefix = if (isOffline) "\uD83C\uDF1A OFFLINE: " else "\uD83C\uDF1E ONLINE: "
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !it }) {
        TextField(
            prefix = { Text(prefix, style = MaterialTheme.typography.bodySmall) },
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            textStyle = MaterialTheme.typography.bodySmall
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = colorScheme.onSurface) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun PromptTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    textPrompt: String,
    onTextPromptChange: (String) -> Unit,
    imagePrompt: ImageSource?,
    onImagePromptChange: (ImageSource?) -> Unit
) {
    val tabs = listOf(
        TabItem(Res.drawable.photo_camera_24px, "Image Prompt"),
        TabItem(Res.drawable.contract_edit_24px, "Text Prompt")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    icon = { Icon(painterResource(tabItem.icon), contentDescription = tabItem.contentDescription) },
                    text = { Text(tabItem.title) }
                )
            }
        }

        AnimatedVisibility(visible = selectedTabIndex == 0) {
            ImagePromptScreen(imagePrompt, onImagePromptChange)
        }
        AnimatedVisibility(visible = selectedTabIndex == 1) {
            TextPromptScreen(textPrompt, onTextPromptChange)
        }
    }
}

@Composable
fun ImagePromptScreen(selectedImage: ImageSource?, onImageChange: (ImageSource?) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val targetResolution = Resolution(896, 896)

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(onClick = { showPicker = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Select an Image")
        }
        Box(
            modifier = Modifier.size(150.dp).border(1.dp, colorScheme.outline),
            contentAlignment = Alignment.Center
        ) {
            if (selectedImage != null) {
                KamelImage(
                    resource = asyncPainterResource(data = selectedImage.key),
                    contentDescription = "Selected image for prompt"
                )
            } else {
                Text("No Image Selected", textAlign = TextAlign.Center)
            }
        }
    }

    ImagePickerLauncher(
        show = showPicker,
        targetResolution = targetResolution,
        onDismissRequest = { showPicker = false },
        onImageSelected = { onImageChange(it) }
    )
}

@Composable
fun TextPromptScreen(text: String, onTextChange: (String) -> Unit) {
    TextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Text prompt goes here…") },
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(vertical = 16.dp)
    )
}

data class TabItem(val icon: DrawableResource, val title: String)
