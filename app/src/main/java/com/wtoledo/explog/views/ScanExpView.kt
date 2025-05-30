package com.wtoledo.explog.views

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.wtoledo.explog.viewModels.ExpenseViewModel
import com.wtoledo.explog.viewModels.ScanExpViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

@Composable
fun ScanExpView(
    expenseViewModel: ExpenseViewModel,
    navController: NavController,
    scanExpViewModel: ScanExpViewModel = viewModel()) {

    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    val isProcessing by scanExpViewModel.isProcessing.observeAsState(initial = false)
    val scannedAmount by scanExpViewModel.scannedAmount.observeAsState(initial = 0.0)
    val scannedDate by scanExpViewModel.scannedDate.observeAsState(initial = "")
    val scannedName by scanExpViewModel.scannedName.observeAsState(initial = "")

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
    val preview = remember(previewView) {
        Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
    }
    val cameraSelector = remember { CameraSelector.DEFAULT_BACK_CAMERA }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasCameraPermission = isGranted
    }
    LaunchedEffect(key1 = true) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }
    LaunchedEffect(hasCameraPermission, cameraProvider, lifecycleOwner, cameraSelector, preview, imageCapture) {
        if (hasCameraPermission) {
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                FirebaseCrashlytics.getInstance().log("Camera bind to lifecycle")
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                Log.e("CameraX", "Error inicializando la cámara", e)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            FirebaseCrashlytics.getInstance().log("cameraExecutor shutdown")
        }
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing == false) {
            FirebaseCrashlytics.getInstance().log("Processing finished. Checking for scanned data.")
            if (scannedName.isNotEmpty() || scannedDate.isNotEmpty() || scannedAmount > 0.0) {
                FirebaseCrashlytics.getInstance().log("Data processed, navigating back.")
                expenseViewModel.updateAmount(scannedAmount)
                expenseViewModel.updateDate(scannedDate)
                expenseViewModel.updateEstablishmentName(scannedName)
                navController.popBackStack()
            } else {
                FirebaseCrashlytics.getInstance().log("Processing finished but no data found.")
            }
        }
    }

    fun takePicture(
        hasCameraPermission: Boolean,
        context: android.content.Context,
        imageCapture: ImageCapture,
        cameraExecutor: ExecutorService,
        onPictureTaken: (Bitmap) -> Unit
    ) {
        FirebaseCrashlytics.getInstance().log("takePicture() started")
        if (hasCameraPermission) {
            FirebaseCrashlytics.getInstance().log("hasCameraPermission is true")
            val outputFile = File(context.filesDir, "captured_image.jpg")
            FirebaseCrashlytics.getInstance().log("outputFile created: ${outputFile.absolutePath}")
            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
            FirebaseCrashlytics.getInstance().log("outputOptions created")

            imageCapture.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        FirebaseCrashlytics.getInstance().log("onImageSaved() started")
                        CoroutineScope(Dispatchers.IO).launch {
                            val bitmap = android.graphics.BitmapFactory.decodeFile(outputFile.absolutePath)
                            FirebaseCrashlytics.getInstance().log("Bitmap decoded")
                            CoroutineScope(Dispatchers.Main).launch {
                                onPictureTaken(bitmap)
                                FirebaseCrashlytics.getInstance().log("onPictureTaken() called")
                            }
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        FirebaseCrashlytics.getInstance().recordException(exception)
                        Log.e("CameraX", "Error tomando foto", exception)
                    }
                })
        } else {
            FirebaseCrashlytics.getInstance().log("hasCameraPermission is false")
        }
        FirebaseCrashlytics.getInstance().log("takePicture() finished")
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (capturedBitmap != null) {
            Image(
                bitmap = capturedBitmap!!.asImageBitmap(),
                contentDescription = "Imagen Capturada",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isProcessing == true) {
            Text(text = "Procesando...")
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            FirebaseCrashlytics.getInstance().log("Button Take Picture clicked")
            takePicture(hasCameraPermission, context, imageCapture, cameraExecutor) { bitmap ->
                FirebaseCrashlytics.getInstance().log("onPictureTaken lambda started")
                capturedBitmap = bitmap
                scanExpViewModel.processImage(bitmap)

                FirebaseCrashlytics.getInstance().log("processImage() called")
            }
            FirebaseCrashlytics.getInstance().log("Button Take Picture finished")
        }) {
            Text("Tomar Foto")
        }

    }
}