package com.example.vibecheck

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object AppSettings {
    private val TARGET_LANGUAGE = stringPreferencesKey("target_language")

    fun getLanguage(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[TARGET_LANGUAGE] ?: "Russian"
        }
    }

    suspend fun setLanguage(context: Context, language: String) {
        context.dataStore.edit { preferences ->
            preferences[TARGET_LANGUAGE] = language
        }
    }
}
