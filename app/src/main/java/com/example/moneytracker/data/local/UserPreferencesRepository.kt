package com.example.moneytracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Currency
import java.util.Locale

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val LANGUAGE_CODE = stringPreferencesKey("language_code")
        val IS_AI_ENABLED = booleanPreferencesKey("is_ai_enabled")
        val MODEL_DOWNLOAD_ID = longPreferencesKey("model_download_id")
        val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")
        val IS_SAMPLE_DATA_ACTIVE = booleanPreferencesKey("is_sample_data_active")
    }

    val currencySymbol: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] ?: getDefaultCurrencySymbol()
        }

    val languageCode: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] ?: Locale.getDefault().language
        }

    val isAiEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_AI_ENABLED] ?: false
        }

    val modelDownloadId: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.MODEL_DOWNLOAD_ID] ?: -1L
        }

    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] ?: false
        }

    val isSampleDataActive: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_SAMPLE_DATA_ACTIVE] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_COMPLETED_ONBOARDING] = completed
        }
    }

    suspend fun setSampleDataActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SAMPLE_DATA_ACTIVE] = active
        }
    }

    suspend fun updateCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun updateLanguageCode(code: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE_CODE] = code
        }
    }

    suspend fun updateAiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_AI_ENABLED] = enabled
        }
    }

    suspend fun updateModelDownloadId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MODEL_DOWNLOAD_ID] = id
        }
    }

    private fun getDefaultCurrencySymbol(): String {
        return try {
            Currency.getInstance(Locale.getDefault()).symbol
        } catch (e: Exception) {
            "$"
        }
    }
}
