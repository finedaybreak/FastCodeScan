package com.wongyichen.fastcodescan.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.wongyichen.fastcodescan.domain.model.CodeFormat
import com.wongyichen.fastcodescan.domain.model.CodeType

data class ScanResult(
    val content: String,
    val codeType: CodeType,
    val codeFormat: CodeFormat
)

class BarcodeAnalyzer(
    private val onBarcodeDetected: (ScanResult) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)
    private var isScanning = true

    fun resumeScanning() {
        isScanning = true
    }

    fun pauseScanning() {
        isScanning = false
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.let { barcode ->
                        barcode.rawValue?.let { value ->
                            isScanning = false
                            val format = CodeFormat.fromMlKitFormat(barcode.format)
                            val codeType = if (format == CodeFormat.QR_CODE ||
                                format == CodeFormat.DATA_MATRIX ||
                                format == CodeFormat.AZTEC ||
                                format == CodeFormat.PDF_417
                            ) {
                                CodeType.QR_CODE
                            } else {
                                CodeType.BARCODE
                            }
                            onBarcodeDetected(
                                ScanResult(
                                    content = value,
                                    codeType = codeType,
                                    codeFormat = format
                                )
                            )
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
