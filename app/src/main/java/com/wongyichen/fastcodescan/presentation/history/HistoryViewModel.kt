package com.wongyichen.fastcodescan.presentation.history

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.CodeType
import com.wongyichen.fastcodescan.scanner.CodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.wongyichen.fastcodescan.domain.usecase.ClearGenerateHistoryUseCase
import com.wongyichen.fastcodescan.domain.usecase.ClearScanHistoryUseCase
import com.wongyichen.fastcodescan.domain.usecase.DeleteGenerateRecordUseCase
import com.wongyichen.fastcodescan.domain.usecase.DeleteScanRecordUseCase
import com.wongyichen.fastcodescan.domain.usecase.GetGenerateHistoryUseCase
import com.wongyichen.fastcodescan.domain.usecase.GetScanHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HistoryTab {
    SCAN,
    GENERATE
}

data class HistoryUiState(
    val selectedTab: HistoryTab = HistoryTab.SCAN,
    val scanHistory: List<CodeRecord> = emptyList(),
    val generateHistory: List<CodeRecord> = emptyList(),
    val showClearConfirmDialog: Boolean = false,
    val selectedRecord: CodeRecord? = null,
    val showRecordDetail: Boolean = false,
    val generatedBitmap: Bitmap? = null,
    val isGeneratingBitmap: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getScanHistoryUseCase: GetScanHistoryUseCase,
    private val getGenerateHistoryUseCase: GetGenerateHistoryUseCase,
    private val deleteScanRecordUseCase: DeleteScanRecordUseCase,
    private val deleteGenerateRecordUseCase: DeleteGenerateRecordUseCase,
    private val clearScanHistoryUseCase: ClearScanHistoryUseCase,
    private val clearGenerateHistoryUseCase: ClearGenerateHistoryUseCase,
    private val codeGenerator: CodeGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            getScanHistoryUseCase().collect { records ->
                _uiState.update { it.copy(scanHistory = records) }
            }
        }

        viewModelScope.launch {
            getGenerateHistoryUseCase().collect { records ->
                _uiState.update { it.copy(generateHistory = records) }
            }
        }
    }

    fun onTabChange(tab: HistoryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onDeleteRecord(record: CodeRecord) {
        viewModelScope.launch {
            when (_uiState.value.selectedTab) {
                HistoryTab.SCAN -> deleteScanRecordUseCase(record)
                HistoryTab.GENERATE -> deleteGenerateRecordUseCase(record)
            }
        }
    }

    fun showClearConfirmDialog() {
        _uiState.update { it.copy(showClearConfirmDialog = true) }
    }

    fun dismissClearConfirmDialog() {
        _uiState.update { it.copy(showClearConfirmDialog = false) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            when (_uiState.value.selectedTab) {
                HistoryTab.SCAN -> clearScanHistoryUseCase()
                HistoryTab.GENERATE -> clearGenerateHistoryUseCase()
            }
            _uiState.update { it.copy(showClearConfirmDialog = false) }
        }
    }

    fun onRecordClick(record: CodeRecord) {
        _uiState.update {
            it.copy(
                selectedRecord = record,
                showRecordDetail = true,
                generatedBitmap = null,
                isGeneratingBitmap = true
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                if (record.type == CodeType.QR_CODE) {
                    codeGenerator.generateQRCode(record.content)
                } else {
                    codeGenerator.generateBarcode(record.content, record.format)
                }
            }

            result.fold(
                onSuccess = { bitmap ->
                    _uiState.update {
                        it.copy(
                            generatedBitmap = bitmap,
                            isGeneratingBitmap = false
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(isGeneratingBitmap = false)
                    }
                }
            )
        }
    }

    fun dismissRecordDetail() {
        _uiState.update {
            it.copy(
                showRecordDetail = false,
                selectedRecord = null,
                generatedBitmap = null,
                isGeneratingBitmap = false
            )
        }
    }
}
