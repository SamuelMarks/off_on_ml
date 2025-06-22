package io.github.samuelmarks.off_on_ml

/*
fun CameraHandler() {
    val cameraPermissionState = remember { mutableStateOf(permissions.hasCameraPermission()) }
    val storagePermissionState = remember { mutableStateOf(permissions.hasStoragePermission()) }

// Request permissions if needed
    if (!cameraPermissionState.value) {
        permissions.RequestCameraPermission(
            onGranted = { cameraPermissionState.value = true },
            onDenied = { println("Camera Permission Denied") }
        )
    }

    if (!storagePermissionState.value) {
        permissions.RequestStoragePermission(
            onGranted = { storagePermissionState.value = true },
            onDenied = { println("Storage Permission Denied") }
        )
    }

    val cameraController = remember { mutableStateOf<CameraController?>(null) }

// Configure plugins if needed
    val imageSaverPlugin = rememberImageSaverPlugin(
        config = ImageSaverConfig(
            isAutoSave = false,
            prefix = "MyApp",
            directory = Directory.PICTURES,
            customFolderName = "MyAppPhotos"  // Android only
        )
    )

    val qrScannerPlugin = rememberQRScannerPlugin(coroutineScope = coroutineScope)

// Set up QR code detection
    LaunchedEffect(Unit) {
        qrScannerPlugin.getQrCodeFlow().distinctUntilChanged()
            .collectLatest { qrCode ->
                println("QR Code Detected: $qrCode")
                qrScannerPlugin.pauseScanning()
            }
    }

    CameraPreview(
        modifier = Modifier.fillMaxSize(),
        cameraConfiguration = {
            setCameraLens(CameraLens.BACK)
            setFlashMode(FlashMode.OFF)
            setImageFormat(ImageFormat.JPEG)
            setDirectory(Directory.PICTURES)
            addPlugin(imageSaverPlugin)
            addPlugin(qrScannerPlugin)
        },
        onCameraControllerReady = {
            cameraController.value = it
            qrScannerPlugin.startScanning()
        }
    )

// Display your custom camera UI once controller is ready
    cameraController.value?.let { controller ->
        CameraScreen(cameraController = controller, imageSaverPlugin)
    }

    Button(
        onClick = {
            scope.launch {
                when (val result = cameraController.takePicture()) {
                    is ImageCaptureResult.Success -> {
                        // Handle the captured image
                        val bitmap = result.byteArray.decodeToImageBitmap()

                        // Manually save the image if auto-save is disabled
                        if (!imageSaverPlugin.config.isAutoSave) {
                            imageSaverPlugin.saveImage(
                                byteArray = result.byteArray,
                                imageName = "Photo_${System.currentTimeMillis()}"
                            )
                        }
                    }
                    is ImageCaptureResult.Error -> {
                        println("Image Capture Error: ${result.exception.message}")
                    }
                }
            }
        }
    ) {
        Text("Capture")
    }
}
 */