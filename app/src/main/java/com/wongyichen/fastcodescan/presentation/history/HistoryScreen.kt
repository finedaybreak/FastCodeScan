package com.wongyichen.fastcodescan.presentation.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wongyichen.fastcodescan.domain.model.CodeRecord
import com.wongyichen.fastcodescan.domain.model.CodeType
import com.wongyichen.fastcodescan.ui.components.AppButton
import com.wongyichen.fastcodescan.ui.components.AppCard
import com.wongyichen.fastcodescan.ui.components.ButtonVariant
import com.wongyichen.fastcodescan.ui.components.ConfirmDialog
import com.wongyichen.fastcodescan.ui.components.EmptyState
import com.wongyichen.fastcodescan.ui.components.HistoryItem
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.headlineSmall
            )

            val currentHistory = when (uiState.selectedTab) {
                HistoryTab.SCAN -> uiState.scanHistory
                HistoryTab.GENERATE -> uiState.generateHistory
            }

            Box(modifier = Modifier.size(48.dp)) {
                if (currentHistory.isNotEmpty()) {
                    IconButton(onClick = { viewModel.showClearConfirmDialog() }) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteSweep,
                            contentDescription = "Clear all",
                            tint = colors.error
                        )
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            containerColor = colors.background,
            contentColor = colors.onBackground,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[uiState.selectedTab.ordinal])
                        .height(3.dp)
                        .padding(horizontal = 48.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(colors.primary)
                )
            },
            divider = {}
        ) {
            HistoryTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.onTabChange(tab) },
                    text = {
                        Text(
                            text = when (tab) {
                                HistoryTab.SCAN -> "Scanned"
                                HistoryTab.GENERATE -> "Generated"
                            },
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    selectedContentColor = colors.primary,
                    unselectedContentColor = colors.onSurfaceVariant
                )
            }
        }

        // Content
        val currentHistory = when (uiState.selectedTab) {
            HistoryTab.SCAN -> uiState.scanHistory
            HistoryTab.GENERATE -> uiState.generateHistory
        }

        if (currentHistory.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = "No history yet",
                description = when (uiState.selectedTab) {
                    HistoryTab.SCAN -> "Your scanned codes will appear here"
                    HistoryTab.GENERATE -> "Your generated codes will appear here"
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = currentHistory,
                    key = { it.id }
                ) { record ->
                    HistoryItem(
                        record = record,
                        onDelete = { viewModel.onDeleteRecord(record) },
                        onClick = { viewModel.onRecordClick(record) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Clear confirmation dialog
    if (uiState.showClearConfirmDialog) {
        ConfirmDialog(
            title = "Clear History",
            message = when (uiState.selectedTab) {
                HistoryTab.SCAN -> "Are you sure you want to clear all scan history?"
                HistoryTab.GENERATE -> "Are you sure you want to clear all generation history?"
            },
            confirmText = "Clear",
            dismissText = "Cancel",
            onConfirm = viewModel::clearHistory,
            onDismiss = viewModel::dismissClearConfirmDialog
        )
    }

    // Record detail dialog
    if (uiState.showRecordDetail && uiState.selectedRecord != null) {
        RecordDetailDialog(
            record = uiState.selectedRecord!!,
            bitmap = uiState.generatedBitmap,
            isGeneratingBitmap = uiState.isGeneratingBitmap,
            onDismiss = viewModel::dismissRecordDetail,
            context = context
        )
    }
}

@Composable
private fun RecordDetailDialog(
    record: CodeRecord,
    bitmap: Bitmap?,
    isGeneratingBitmap: Boolean,
    onDismiss: () -> Unit,
    context: Context
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    Dialog(onDismissRequest = onDismiss) {
        AppCard {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Code image display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (record.type == CodeType.QR_CODE) 1f else 2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGeneratingBitmap) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Code image",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Type", value = record.type.name.replace("_", " "))
                DetailRow(label = "Format", value = record.format.name.replace("_", " "))
                DetailRow(label = "Created", value = record.createdAt.format(formatter))

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Content",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = record.content,
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
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(
                        text = "Copy",
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Content", record.content))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        variant = ButtonVariant.Outline
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AppButton(
                        text = "Close",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
