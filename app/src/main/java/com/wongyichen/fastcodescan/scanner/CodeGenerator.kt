package com.wongyichen.fastcodescan.scanner

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.wongyichen.fastcodescan.domain.model.CodeFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeGenerator @Inject constructor() {

    fun generateQRCode(
        content: String,
        width: Int = 512,
        height: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Result<Bitmap> = runCatching {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )

        val bitMatrix = MultiFormatWriter().encode(
            content,
            BarcodeFormat.QR_CODE,
            width,
            height,
            hints
        )

        createBitmap(bitMatrix, foregroundColor, backgroundColor)
    }

    fun generateBarcode(
        content: String,
        format: CodeFormat = CodeFormat.CODE_128,
        width: Int = 600,
        height: Int = 200,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Result<Bitmap> = runCatching {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )

        val bitMatrix = MultiFormatWriter().encode(
            content,
            format.toZxingFormat(),
            width,
            height,
            hints
        )

        createBitmap(bitMatrix, foregroundColor, backgroundColor)
    }

    private fun createBitmap(
        bitMatrix: BitMatrix,
        foregroundColor: Int,
        backgroundColor: Int
    ): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
            }
        }

        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, width, 0, 0, width, height)
        }
    }
}
