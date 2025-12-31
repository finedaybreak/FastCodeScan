package com.wongyichen.fastcodescan.presentation.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wongyichen.fastcodescan.domain.model.CodeFormat
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.CodeType
import com.wongyichen.fastcodescan.domain.model.RecordType
import com.wongyichen.fastcodescan.domain.usecase.SaveScanRecordUseCase
import com.wongyichen.fastcodescan.scanner.ScanResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScanUiState(
    val scanResult: ScanResult? = null,
    val isScanning: Boolean = true,
    val showResultDialog: Boolean = false
)

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val saveScanRecordUseCase: SaveScanRecordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun onBarcodeScanned(result: ScanResult) {
        _uiState.update {
            it.copy(
                scanResult = result,
                isScanning = false,
                showResultDialog = true
            )
        }

        viewModelScope.launch {
            val record = CodeRecord(
                content = result.content,
                type = result.codeType,
                format = result.codeFormat,
                recordType = RecordType.SCAN
            )
            saveScanRecordUseCase(record)
        }
    }

    fun dismissResult() {
        _uiState.update {
            it.copy(
                scanResult = null,
                isScanning = true,
                showResultDialog = false
            )
        }
    }

    fun resumeScanning() {
        _uiState.update {
            it.copy(
                scanResult = null,
                isScanning = true,
                showResultDialog = false
            )
        }
    }
}
