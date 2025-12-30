package com.wongyichen.fastcodescan.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class ButtonVariant {
    Primary,
    Secondary,
    Outline,
    Ghost,
    Destructive
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme

    when (variant) {
        ButtonVariant.Primary -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        ButtonVariant.Secondary -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.secondary,
                    contentColor = colors.onSecondary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        ButtonVariant.Outline -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, colors.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        ButtonVariant.Ghost -> {
            TextButton(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }

        ButtonVariant.Destructive -> {
            Button(
                onClick = onClick,
                modifier = modifier.height(40.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error,
                    contentColor = colors.onError
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
