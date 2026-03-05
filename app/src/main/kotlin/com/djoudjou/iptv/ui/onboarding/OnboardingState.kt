package com.djoudjou.iptv.ui.onboarding

import com.djoudjou.iptv.data.local.CategoryEntity
import com.djoudjou.iptv.data.preferences.ProviderType
import com.djoudjou.iptv.domain.model.Result

/**
 * OnboardingState - Sealed Class für alle Wizard-Zustände.
 *
 * State Machine für das Onboarding:
 *
 * PROVIDER_SELECT → LOGIN (Xtream) oder M3U_IMPORT (M3U)
 *       ↓
 * LOGIN (Xtream) → CATEGORIES_SELECT
 *       ↓
 * M3U_IMPORT (M3U) → CATEGORIES_SELECT
 *       ↓
 * CATEGORIES_SELECT → SYNCING
 *       ↓
 * SYNCING → COMPLETE oder ERROR
 *
 * Jeder State enthält alle relevanten Daten für die UI.
 */
sealed class OnboardingState {

    /**
     * Stufe 1: Provider-Auswahl.
     *
     * UI zeigt Auswahl zwischen Xtream Codes und M3U.
     */
    object ProviderSelect : OnboardingState()

    /**
     * Stufe 2a: Xtream Login.
     *
     * UI zeigt Formular für Server-URL, Benutzername, Passwort.
     *
     * @param url Eingegebene Server-URL
     * @param username Eingegebener Benutzername
     * @param password Eingegebenes Passwort
     * @param isLoading true während der Authentifizierung
     * @param error Fehlermeldung oder null
     */
    data class Login(
        val url: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    ) : OnboardingState()

    /**
     * Stufe 2b: M3U Import.
     *
     * UI zeigt URL-Eingabe oder Datei-Picker.
     *
     * @param importType Gewählter Import-Typ (URL oder Datei)
     * @param url Eingegebene M3U-URL
     * @param filePath Ausgewählter Datei-Pfad
     * @param isLoading true während des Parsens
     * @param error Fehlermeldung oder null
     */
    data class M3uImport(
        val importType: M3uImportType = M3uImportType.URL,
        val url: String = "",
        val filePath: String? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : OnboardingState()

    /**
     * Stufe 3: Kategorie-Auswahl.
     *
     * UI zeigt Checkbox-Liste aller Kategorien mit Select-All/None/Invert.
     *
     * @param categories Alle verfügbaren Kategorien
     * @param selectedCategoryIds Ausgewählte Kategorie-IDs
     * @param isLoading true während des Ladens
     * @param error Fehlermeldung oder null
     */
    data class CategoriesSelect(
        val categories: List<CategoryUiModel> = emptyList(),
        val selectedCategoryIds: Set<String> = emptySet(),
        val isLoading: Boolean = false,
        val error: String? = null
    ) : OnboardingState()

    /**
     * Stufe 4: Synchronisation.
     *
     * UI zeigt Fortschrittsanzeige während DB-Write.
     *
     * @param currentStep Aktueller Schritt (1-4)
     * @param totalSteps Gesamtanzahl Schritte (4)
     * @param statusText Beschreibung des aktuellen Schritts
     * @param isComplete true wenn Synchronisation abgeschlossen
     * @param error Fehlermeldung oder null
     */
    data class Syncing(
        val currentStep: Int = 1,
        val totalSteps: Int = 4,
        val statusText: String = "Synchronisiere...",
        val isComplete: Boolean = false,
        val error: String? = null
    ) : OnboardingState()

    /**
     * Onboarding abgeschlossen.
     *
     * UI zeigt Erfolgsmeldung und leitet zur MainActivity weiter.
     */
    object Complete : OnboardingState()

    /**
     * Allgemeiner Fehler.
     *
     * @param message Fehlermeldung
     * @param retryAction Optionale Aktion für Retry
     */
    data class Error(
        val message: String,
        val retryAction: (() -> Unit)? = null
    ) : OnboardingState()
}

/**
 * M3U Import Typ.
 */
enum class M3uImportType {
    URL,        // Remote-URL
    FILE        // Lokale Datei
}

/**
 * UI-Model für Kategorien.
 *
 * Vereinfachte Darstellung für die UI.
 */
data class CategoryUiModel(
    val id: String,
    val name: String,
    val streamType: com.djoudjou.iptv.data.local.StreamType,
    val isAdult: Boolean = false,
    val streamCount: Int = 0,
    val isSelected: Boolean = true
) {
    /**
     * Erstellt CategoryUiModel aus CategoryEntity.
     */
    companion object {
        fun fromEntity(entity: CategoryEntity): CategoryUiModel {
            return CategoryUiModel(
                id = entity.categoryId,
                name = entity.name,
                streamType = entity.streamType,
                isAdult = entity.isAdult,
                streamCount = entity.streamCount,
                isSelected = entity.isSelected
            )
        }
    }
}

/**
 * Onboarding Event - Aktionen die vom Benutzer ausgelöst werden.
 */
sealed class OnboardingEvent {

    /**
     * Provider wurde ausgewählt.
     */
    data class ProviderSelected(val type: ProviderType) : OnboardingEvent()

    /**
     * Login-Daten wurden eingegeben.
     */
    data class LoginDataChanged(
        val url: String,
        val username: String,
        val password: String
    ) : OnboardingEvent()

    /**
     * Login wurde ausgelöst.
     */
    object LoginSubmit : OnboardingEvent()

    /**
     * M3U Import Typ wurde geändert.
     */
    data class M3uImportTypeChanged(val type: M3uImportType) : OnboardingEvent()

    /**
     * M3U URL wurde eingegeben.
     */
    data class M3uUrlChanged(val url: String) : OnboardingEvent()

    /**
     * M3U Datei wurde ausgewählt.
     */
    data class M3uFileSelected(val filePath: String) : OnboardingEvent()

    /**
     * M3U Import wurde ausgelöst.
     */
    object M3uSubmit : OnboardingEvent()

    /**
     * Kategorie-Auswahl wurde geändert.
     */
    data class CategorySelectionChanged(
        val categoryId: String,
        val isSelected: Boolean
    ) : OnboardingEvent()

    /**
     * Alle Kategorien auswählen.
     */
    object SelectAllCategories : OnboardingEvent()

    /**
     * Alle Kategorien abwählen.
     */
    object DeselectAllCategories : OnboardingEvent()

    /**
     * Auswahl umkehren.
     */
    object InvertCategorySelection : OnboardingEvent()

    /**
     * Synchronisation wurde ausgelöst.
     */
    object StartSync : OnboardingEvent()

    /**
     * Zurück zur vorherigen Stufe.
     */
    object GoBack : OnboardingEvent()

    /**
     * Onboarding überspringen (für Demo-Zwecke).
     */
    object Skip : OnboardingEvent()
}
