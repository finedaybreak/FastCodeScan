package com.wongyichen.fastcodescan.domain.model

import java.time.LocalDateTime

data class CodeRecord(
    val id: Long = 0,
    val content: String,
    val type: CodeType,
    val format: CodeFormat,
    val recordType: RecordType,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class CodeType {
    QR_CODE,
    BARCODE
}

enum class CodeFormat {
    QR_CODE,
    CODE_128,
    CODE_39,
    CODE_93,
    CODABAR,
    EAN_13,
    EAN_8,
    ITF,
    UPC_A,
    UPC_E,
    DATA_MATRIX,
    PDF_417,
    AZTEC,
    UNKNOWN;

    companion object {
        fun fromMlKitFormat(format: Int): CodeFormat {
            return when (format) {
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE -> QR_CODE
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_128 -> CODE_128
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_39 -> CODE_39
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODE_93 -> CODE_93
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_CODABAR -> CODABAR
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_13 -> EAN_13
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_EAN_8 -> EAN_8
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_ITF -> ITF
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_A -> UPC_A
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_UPC_E -> UPC_E
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_DATA_MATRIX -> DATA_MATRIX
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_PDF417 -> PDF_417
                com.google.mlkit.vision.barcode.common.Barcode.FORMAT_AZTEC -> AZTEC
                else -> UNKNOWN
            }
        }
    }

    fun isBarcode(): Boolean = this != QR_CODE && this != DATA_MATRIX && this != AZTEC && this != PDF_417 && this != UNKNOWN

    fun toZxingFormat(): com.google.zxing.BarcodeFormat {
        return when (this) {
            QR_CODE -> com.google.zxing.BarcodeFormat.QR_CODE
            CODE_128 -> com.google.zxing.BarcodeFormat.CODE_128
            CODE_39 -> com.google.zxing.BarcodeFormat.CODE_39
            CODE_93 -> com.google.zxing.BarcodeFormat.CODE_93
            CODABAR -> com.google.zxing.BarcodeFormat.CODABAR
            EAN_13 -> com.google.zxing.BarcodeFormat.EAN_13
            EAN_8 -> com.google.zxing.BarcodeFormat.EAN_8
            ITF -> com.google.zxing.BarcodeFormat.ITF
            UPC_A -> com.google.zxing.BarcodeFormat.UPC_A
            UPC_E -> com.google.zxing.BarcodeFormat.UPC_E
            DATA_MATRIX -> com.google.zxing.BarcodeFormat.DATA_MATRIX
            PDF_417 -> com.google.zxing.BarcodeFormat.PDF_417
            AZTEC -> com.google.zxing.BarcodeFormat.AZTEC
            UNKNOWN -> com.google.zxing.BarcodeFormat.QR_CODE
        }
    }
}

enum class RecordType {
    SCAN,
    GENERATE
}
