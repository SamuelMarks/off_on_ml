package io.github.samuelmarks.off_on_ml

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher

import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import offonml.composeapp.generated.resources.Res
import offonml.composeapp.generated.resources.compose_multiplatform
import offonml.composeapp.generated.resources.contract_edit_24px
import offonml.composeapp.generated.resources.photo_camera_24px
import org.jetbrains.compose.resources.DrawableResource

val offlineModels: List<String> =
    listOf("google/medgemma-4b-it", "google/medgemma-4b-pt", "google/medgemma-27b-text-it")
val onlineModels: List<String> =
    listOf("gemini-2.5-flash", "gemini-2.5-pro", "gemini-2.5-pro-preview-06-05")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun modelChooser(
    offline: Boolean,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val (options, prefix) = if (offline)
        Pair(offlineModels, "\uD83C\uDF1A OFFLINE: ")
    else
        Pair(onlineModels, "\uD83C\uDF1E ONLINE: ")

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            prefix = { Text(prefix) },
            value = selectedOption,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            color = colorScheme.onSurface
                        )
                    },
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

// Data class to represent each tab
data class TabItem(
    val icon: DrawableResource,
    val contentDescription: String,
    val screen: @Composable () -> Unit // Your screen composable for each tab
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptOfImageOrText() {
    // State to track the selected tab index
    var selectedTabIndex by remember { mutableStateOf(0) }

    // List of tab items
    val tabs = listOf(
        TabItem(Res.drawable.photo_camera_24px, "Camera", { CameraScreen() }),
        TabItem(Res.drawable.contract_edit_24px, "Text", { TextScreen() })
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    icon = {
                        Icon(
                            painter = painterResource(tabItem.icon),
                            contentDescription = tabItem.contentDescription
                        )
                    }
                )
            }
        }

        // Display content based on the selected tab
        when (selectedTabIndex) {
            0 -> CameraScreen()
            1 -> TextScreen()
        }
    }
}

@Composable
fun CameraScreen() {
    val scope = rememberCoroutineScope()

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                // Process the selected images' ByteArrays.
                println(it)
            }
        }
    )

    Button(
        onClick = {
            singleImagePicker.launch()
        }
    ) {
        Text(
            "Image from gallery",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
    Button(
        onClick = {
            /* TODO: See CameraHandler.kt for WiP */
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.tertiary
        )
    ) {
        Text(
            "Take a new photo",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TextScreen() {
    var text by remember { mutableStateOf("") }
    TextField(
        value = text,
        onValueChange = { text = it },
        label = { Text("Text prompt goes here…") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 5
    )
}

@Composable
fun AiProcessButton() {
    var showPopup by remember { mutableStateOf(false) } // State for popup visibility

    Image(
        painterResource(Res.drawable.compose_multiplatform),
        "Click to start AI processing",
        modifier = Modifier.clickable {
            showPopup = true
        }
    )

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            confirmButton = {
                TextButton(onClick = { showPopup = false }) {
                    Text("TODO: AI")
                }
            },
            title = { Text("TODO: AI") },
            text = { Text("TODO: AI") }
        )
    }
}

@Composable
@Preview
fun App() {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkTheme = remember { mutableStateOf(systemInDarkTheme) }

    var selectedOfflineModel by remember {
        mutableStateOf(
            settings.getString(
                SettingsKeys.SELECTED_OFFLINE_MODEL,
                offlineModels.first()
            )
        )
    }
    var selectedOnlineModel by remember {
        mutableStateOf(
            settings.getString(
                SettingsKeys.SELECTED_ONLINE_MODEL,
                onlineModels.first()
            )
        )
    }

    MaterialTheme(colorScheme = if (isDarkTheme.value) darkColorScheme() else lightColorScheme()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {},
                bottomBar = {
                    BottomAppBar {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isDarkTheme.value) {
                                modelChooser(
                                    offline = true,
                                    selectedOption = selectedOfflineModel,
                                    onOptionSelected = { newSelection ->
                                        selectedOfflineModel = newSelection
                                        settings.putString(
                                            SettingsKeys.SELECTED_OFFLINE_MODEL,
                                            newSelection
                                        )
                                    }
                                )
                            } else {
                                modelChooser(
                                    offline = false,
                                    selectedOption = selectedOnlineModel,
                                    onOptionSelected = { newSelection ->
                                        selectedOnlineModel = newSelection
                                        settings.putString(
                                            SettingsKeys.SELECTED_ONLINE_MODEL,
                                            newSelection
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .safeContentPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Switch(
                        checked = isDarkTheme.value,
                        onCheckedChange = { isDarkTheme.value = it }
                    )

                    val platform = remember { getPlatform() }
                    AiProcessButton()
                    PromptOfImageOrText()
                    Text(
                        text = "Running on ${platform.name}.\n" +
                                "Toggle betwixt offline/online.\n" +
                                "ML response will replace this section.",
                        color = colorScheme.inverseSurface
                    )
                }
            }
        }
    }
}
