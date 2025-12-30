package com.wongyichen.fastcodescan.presentation.scan

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wongyichen.fastcodescan.scanner.BarcodeAnalyzer
import com.wongyichen.fastcodescan.ui.components.AppButton
import com.wongyichen.fastcodescan.ui.components.AppCard
import com.wongyichen.fastcodescan.ui.components.ButtonVariant
import java.util.concurrent.Executors
import androidx.core.net.toUri

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            val lifecycleOwner = LocalLifecycleOwner.current
            val analyzer = remember {
                BarcodeAnalyzer { result ->
                    viewModel.onBarcodeScanned(result)
                }
            }

            // Handle scanning state changes
            LaunchedEffect(uiState.isScanning) {
                if (uiState.isScanning) {
                    analyzer.resumeScanning()
                } else {
                    analyzer.pauseScanning()
                }
            }

            // Re-activate analyzer when app resumes from background
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME && uiState.isScanning) {
                        analyzer.resumeScanning()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            CameraPreview(
                analyzer = analyzer,
                modifier = Modifier.fillMaxSize()
            )

            // Scan frame overlay
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Transparent)
                ) {
                    // Corner decorations
                    ScanFrameCorners()
                }
            }

            // Instructions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Align QR code or barcode within the frame",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Permission not granted
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Please grant camera permission to scan QR codes and barcodes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Grant Permission",
                    onClick = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }

        // Result Dialog
        if (uiState.showResultDialog && uiState.scanResult != null) {
            ScanResultDialog(
                content = uiState.scanResult!!.content,
                format = uiState.scanResult!!.codeFormat.name.replace("_", " "),
                onDismiss = { viewModel.dismissResult() },
                onContinue = { viewModel.resumeScanning() },
                context = context
            )
        }
    }
}

@Composable
private fun CameraPreview(
    analyzer: BarcodeAnalyzer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
private fun ScanFrameCorners() {
    val cornerColor = Color.White
    val cornerSize = 40.dp
    val cornerWidth = 4.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = cornerWidth)
            ) {
                Box(
                    modifier = Modifier
                        .size(cornerWidth, cornerSize - cornerWidth)
                        .background(cornerColor)
                )
            }
        }

        // Top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = cornerWidth)
            ) {
                Box(
                    modifier = Modifier
                        .size(cornerWidth, cornerSize - cornerWidth)
                        .background(cornerColor)
                        .align(Alignment.TopEnd)
                )
            }
        }

        // Bottom-left
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .align(Alignment.BottomStart)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .size(cornerWidth, cornerSize - cornerWidth)
                    .background(cornerColor)
            )
        }

        // Bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(cornerSize)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cornerWidth)
                    .align(Alignment.BottomEnd)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .size(cornerWidth, cornerSize - cornerWidth)
                    .background(cornerColor)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
private fun ScanResultDialog(
    content: String,
    format: String,
    onDismiss: () -> Unit,
    onContinue: () -> Unit,
    context: Context
) {
    val isUrl = content.startsWith("http://") || content.startsWith("https://")

    Dialog(onDismissRequest = onDismiss) {
        AppCard {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "Scan Result",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = format,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Scanned Content", content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy"
                        )
                    }

                    if (isUrl) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, content.toUri())
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language,
                                contentDescription = "Open URL"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    AppButton(
                        text = "Continue",
                        onClick = onContinue,
                        variant = ButtonVariant.Primary
                    )
                }
            }
        }
    }
}
