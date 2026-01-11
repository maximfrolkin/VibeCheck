package com.example.vibecheck

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SlangPrefsKeys {
    val WORD = stringPreferencesKey("word")
    val TRANSLATION = stringPreferencesKey("translation")
    val CONTEXT = stringPreferencesKey("context")
    val TREND_REASON = stringPreferencesKey("trend_reason")
    val TREND_SCORE = intPreferencesKey("trend_score")
    val TARGET_LANGUAGE = stringPreferencesKey("target_language")
}
