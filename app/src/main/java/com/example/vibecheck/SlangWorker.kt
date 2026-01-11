package com.example.vibecheck

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class SlangWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(VibeCheckWidget::class.java)

            // Retrieve the target language from the dedicated AppSettings singleton
            val language = AppSettings.getLanguage(context).first()

            val slangData = SlangRepository.fetchTrendingSlang(language)
            
            if (slangData != null) {
                glanceIds.forEach { glanceId ->
                    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            this[SlangPrefsKeys.WORD] = slangData.word
                            this[SlangPrefsKeys.TRANSLATION] = slangData.translation
                            this[SlangPrefsKeys.CONTEXT] = slangData.context
                            this[SlangPrefsKeys.TREND_REASON] = slangData.trendReason
                            this[SlangPrefsKeys.TREND_SCORE] = slangData.trendScore
                        }
                    }
                }
                VibeCheckWidget().updateAll(context)
                Result.success()
            } else {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
