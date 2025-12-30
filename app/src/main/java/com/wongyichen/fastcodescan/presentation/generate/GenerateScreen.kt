package com.wongyichen.fastcodescan.presentation.generate

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.wongyichen.fastcodescan.ui.components.AppButton
import com.wongyichen.fastcodescan.ui.components.AppCard
import com.wongyichen.fastcodescan.ui.components.AppTextField
import com.wongyichen.fastcodescan.ui.components.ButtonVariant
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateScreen(
    viewModel: GenerateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Generate Code",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mode selection
        AppCard {
            Column {
                Text(
                    text = "Code Type",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeButton(
                        icon = Icons.Outlined.QrCode,
                        label = "QR Code",
                        isSelected = uiState.mode == GenerateMode.QR_CODE,
                        onClick = { viewModel.onModeChange(GenerateMode.QR_CODE) },
                        modifier = Modifier.weight(1f)
                    )

                    ModeButton(
                        icon = Icons.Outlined.ViewWeek,
                        label = "Barcode",
                        isSelected = uiState.mode == GenerateMode.BARCODE,
                        onClick = { viewModel.onModeChange(GenerateMode.BARCODE) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content input
        AppCard {
            Column {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = uiState.content,
                    onValueChange = viewModel::onContentChange,
                    placeholder = if (uiState.mode == GenerateMode.QR_CODE) {
                        "Enter text, URL, or any content..."
                    } else {
                        "Enter numbers or text..."
                    },
                    singleLine = false,
                    maxLines = 4,
                    isError = uiState.error != null,
                    errorMessage = uiState.error
                )

                // Barcode format dropdown
                if (uiState.mode == GenerateMode.BARCODE) {
                    Spacer(modifier = Modifier.height(12.dp))

                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.barcodeFormat.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Format") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            viewModel.barcodeFormats.forEach { format ->
                                DropdownMenuItem(
                                    text = { Text(format.name.replace("_", " ")) },
                                    onClick = {
                                        viewModel.onBarcodeFormatChange(format)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AppButton(
                    text = "Generate",
                    onClick = viewModel::generate,
                    enabled = !uiState.isGenerating && uiState.content.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Loading indicator
        if (uiState.isGenerating) {
            Spacer(modifier = Modifier.height(16.dp))

            AppCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    // Result dialog
    if (uiState.showResultDialog && uiState.generatedBitmap != null) {
        GenerateResultDialog(
            bitmap = uiState.generatedBitmap!!,
            mode = uiState.mode,
            content = uiState.content,
            onDismiss = viewModel::dismissResultDialog,
            onSave = { saveBitmapToGallery(context, uiState.generatedBitmap!!) },
            onClearAndClose = viewModel::clearResult
        )
    }
}

@Composable
private fun GenerateResultDialog(
    bitmap: Bitmap,
    mode: GenerateMode,
    content: String,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onClearAndClose: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        AppCard {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Generated Code",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row {
                        IconButton(onClick = onSave) {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Save"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(if (mode == GenerateMode.QR_CODE) 1f else 2f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Generated code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    AppButton(
                        text = "Close",
                        onClick = onDismiss,
                        variant = ButtonVariant.Outline
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    AppButton(
                        text = "Done",
                        onClick = onClearAndClose
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primary else colors.surface)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.primary else colors.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected) colors.onPrimary else colors.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) colors.onPrimary else colors.onSurface
            )
        }
    }
}

private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
    val filename = "code_${System.currentTimeMillis()}.png"

    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }

    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        var outputStream: OutputStream? = null
        try {
            outputStream = resolver.openOutputStream(it)
            outputStream?.let { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Toast.makeText(context, "Saved to gallery", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
        } finally {
            outputStream?.close()
        }
    }
}
