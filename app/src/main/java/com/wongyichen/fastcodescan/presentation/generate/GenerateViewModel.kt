package com.wongyichen.fastcodescan.presentation.generate

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wongyichen.fastcodescan.domain.model.CodeFormat
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.CodeType
import com.wongyichen.fastcodescan.domain.model.RecordType
import com.wongyichen.fastcodescan.domain.usecase.SaveGenerateRecordUseCase
import com.wongyichen.fastcodescan.scanner.CodeGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class GenerateMode {
    QR_CODE,
    BARCODE
}

data class GenerateUiState(
    val content: String = "",
    val mode: GenerateMode = GenerateMode.QR_CODE,
    val barcodeFormat: CodeFormat = CodeFormat.CODE_128,
    val generatedBitmap: Bitmap? = null,
    val isGenerating: Boolean = false,
    val error: String? = null,
    val showResultDialog: Boolean = false
)

@HiltViewModel
class GenerateViewModel @Inject constructor(
    private val codeGenerator: CodeGenerator,
    private val saveGenerateRecordUseCase: SaveGenerateRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = _uiState.asStateFlow()

    val barcodeFormats = listOf(
        CodeFormat.CODE_128,
        CodeFormat.CODE_39,
        CodeFormat.EAN_13,
        CodeFormat.EAN_8,
        CodeFormat.UPC_A,
        CodeFormat.ITF
    )

    fun onContentChange(content: String) {
        _uiState.update { it.copy(content = content, error = null) }
    }

    fun onModeChange(mode: GenerateMode) {
        _uiState.update {
            it.copy(
                mode = mode,
                generatedBitmap = null,
                error = null
            )
        }
    }

    fun onBarcodeFormatChange(format: CodeFormat) {
        _uiState.update {
            it.copy(
                barcodeFormat = format,
                generatedBitmap = null,
                error = null
            )
        }
    }

    fun generate() {
        val state = _uiState.value
        if (state.content.isBlank()) {
            _uiState.update { it.copy(error = "Please enter content") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }

            val result = withContext(Dispatchers.Default) {
                when (state.mode) {
                    GenerateMode.QR_CODE -> codeGenerator.generateQRCode(state.content)
                    GenerateMode.BARCODE -> codeGenerator.generateBarcode(
                        content = state.content,
                        format = state.barcodeFormat
                    )
                }
            }

            result.fold(
                onSuccess = { bitmap ->
                    _uiState.update {
                        it.copy(
                            generatedBitmap = bitmap,
                            isGenerating = false,
                            showResultDialog = true
                        )
                    }

                    val record = CodeRecord(
                        content = state.content,
                        type = if (state.mode == GenerateMode.QR_CODE) CodeType.QR_CODE else CodeType.BARCODE,
                        format = if (state.mode == GenerateMode.QR_CODE) CodeFormat.QR_CODE else state.barcodeFormat,
                        recordType = RecordType.GENERATE
                    )
                    saveGenerateRecordUseCase(record)
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            error = e.message ?: "Failed to generate code",
                            isGenerating = false
                        )
                    }
                }
            )
        }
    }

    fun clearResult() {
        _uiState.update {
            it.copy(
                generatedBitmap = null,
                content = "",
                error = null,
                showResultDialog = false
            )
        }
    }

    fun dismissResultDialog() {
        _uiState.update {
            it.copy(showResultDialog = false)
        }
    }
}
