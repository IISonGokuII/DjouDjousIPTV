package com.djoudjou.iptv.ui.onboarding.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*

/**
 * LoginScreen - Stufe 2a des Onboarding (Xtream Codes).
 *
 * Eingabe von Server-URL, Benutzername und Passwort.
 * TV-optimiert mit D-Pad Navigation.
 */
@Composable
fun LoginScreen(
    url: String = "",
    username: String = "",
    password: String = "",
    isLoading: Boolean = false,
    error: String? = null,
    onUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Xtream Codes Login",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Geben Sie Ihre Zugangsdaten ein",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Server URL Field
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Server URL") },
            placeholder = { Text("http://example.com") },
            modifier = Modifier.width(400.dp),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Benutzername") },
            placeholder = { Text("Ihr Benutzername") },
            modifier = Modifier.width(400.dp),
            enabled = !isLoading,
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Passwort") },
            placeholder = { Text("Ihr Passwort") },
            modifier = Modifier.width(400.dp),
            enabled = !isLoading,
            singleLine = true,
            password = true
        )

        // Error Message
        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onBack,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Zurück")
            }

            Button(
                onClick = onSubmit,
                enabled = !isLoading && url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Verbinde..." : "Verbinden")
            }
        }
    }
}
