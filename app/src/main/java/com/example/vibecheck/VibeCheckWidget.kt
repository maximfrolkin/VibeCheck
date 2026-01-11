package com.example.vibecheck

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Glance Widget Implementation
 */
class VibeCheckWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val word = prefs[SlangPrefsKeys.WORD] ?: "Loading..."
            val translation = prefs[SlangPrefsKeys.TRANSLATION] ?: ""
            val contextText = prefs[SlangPrefsKeys.CONTEXT] ?: "Fetching slang..."
            val trendReason = prefs[SlangPrefsKeys.TREND_REASON] ?: ""
            val trendScore = prefs[SlangPrefsKeys.TREND_SCORE] ?: 0

            GlanceTheme {
                VibeWidgetContent(word, translation, contextText, trendReason, trendScore)
            }
        }
    }

    @Composable
    private fun VibeWidgetContent(word: String, translation: String, contextText: String, trendReason: String, trendScore: Int) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Text(
                text = word,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            if (translation.isNotEmpty()) {
                Text(
                    text = translation,
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = GlanceTheme.colors.secondary
                    ),
                    maxLines = 1
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            
            Text(
                text = contextText,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                ),
                maxLines = 3
            )
            
            if (trendReason.isNotEmpty()) {
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "Why: $trendReason",
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.tertiary
                    ),
                    maxLines = 3
                )
            }

            Spacer(GlanceModifier.defaultWeight())
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.End
            ) {
                if (trendScore > 0) {
                     Text(
                         text = "🔥".repeat(trendScore.coerceIn(1, 10)),
                         style = TextStyle(fontSize = 12.sp)
                     )
                     Spacer(GlanceModifier.width(12.dp)) 
                }

                Image(
                    provider = ImageProvider(android.R.drawable.ic_popup_sync),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .clickable(actionRunCallback<RefreshActionCallback>())
                        .padding(8.dp)
                )

                Image(
                    provider = ImageProvider(android.R.drawable.ic_menu_share),
                    contentDescription = "Share",
                    modifier = GlanceModifier
                        .clickable(
                            actionRunCallback<ShareActionCallback>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("text") to "Word of the Day: $word — $translation"
                                )
                            )
                        )
                        .padding(8.dp)
                )
            }
        }
    }
}

class ShareActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val shareText = parameters[ActionParameters.Key<String>("text")] ?: ""
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val workRequest = OneTimeWorkRequestBuilder<SlangWorker>()
            .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

class VibeCheckReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = VibeCheckWidget()
}
