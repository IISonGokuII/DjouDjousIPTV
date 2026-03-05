package com.djoudjou.iptv.ui.onboarding

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.MaterialTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * OnboardingActivity - Wizard für die Ersteinrichtung.
 *
 * Wird in Phase 3 vollständig implementiert mit:
 * - Stufe 1: Provider-Auswahl (Xtream Codes vs M3U)
 * - Stufe 2a: Xtream Login (URL, Username, Password)
 * - Stufe 2b: M3U Import (Remote-URL oder lokale Datei)
 * - Stufe 3: Kategorie-Filter mit Checkbox-Liste
 * - Stufe 4: Synchronisation in Room Database
 */
@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OnboardingScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    androidx.tv.material3.Surface(modifier = modifier) {
        Text(
            text = "Onboarding - Phase 3 (Coming Soon)",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen()
    }
}
