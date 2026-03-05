package com.djoudjou.iptv.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

/**
 * ParentalPinDialog - PIN-Eingabe für Kindersicherung.
 */
@Composable
fun ParentalPinDialog(
    title: String = "PIN eingeben",
    onPinEntered: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // PIN Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                color = if (index < pin.length) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Box(modifier = Modifier.fillMaxSize())
                            }

                            if (index < pin.length) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                // Error Message
                error?.let { err ->
                    Text(
                        text = err,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                // Numpad
                Numpad(
                    onDigitEntered = { digit ->
                        if (pin.length < 4) {
                            pin += digit
                            error = null

                            if (pin.length == 4) {
                                onPinEntered(pin)
                            }
                        }
                    },
                    onBackspace = {
                        if (pin.isNotEmpty()) {
                            pin = pin.dropLast(1)
                            error = null
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}

/**
 * Numpad für PIN-Eingabe.
 */
@Composable
private fun Numpad(
    onDigitEntered: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: 1 2 3
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { num ->
                NumpadButton(text = "${num + 1}", onClick = { onDigitEntered("${num + 1}") })
            }
        }

        // Row 2: 4 5 6
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { num ->
                NumpadButton(text = "${num + 4}", onClick = { onDigitEntered("${num + 4}") })
            }
        }

        // Row 3: 7 8 9
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { num ->
                NumpadButton(text = "${num + 7}", onClick = { onDigitEntered("${num + 7}") })
            }
        }

        // Row 4: Clear 0 ←
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NumpadButton(text = "C", onClick = { /* Clear handled externally */ })
            NumpadButton(text = "0", onClick = { onDigitEntered("0") })
            NumpadButton(text = "←", onClick = onBackspace)
        }
    }
}

/**
 * Numpad Button.
 */
@Composable
private fun NumpadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = modifier.size(64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
