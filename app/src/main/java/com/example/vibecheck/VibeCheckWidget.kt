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
import androidx.glance.LocalContext
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
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
import kotlinx.coroutines.flow.first

/**
 * Glance Widget Implementation
 */
class VibeCheckWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val language = AppSettings.getLanguage(context).first()
        val strings = Localization.getStrings(language)

        provideContent {
            val prefs = currentState<Preferences>()
            val word = prefs[SlangPrefsKeys.WORD] ?: strings.loading
            val translation = prefs[SlangPrefsKeys.TRANSLATION] ?: ""
            val contextText = prefs[SlangPrefsKeys.CONTEXT] ?: strings.fetching
            val trendReason = prefs[SlangPrefsKeys.TREND_REASON] ?: ""
            val trendScore = prefs[SlangPrefsKeys.TREND_SCORE] ?: 0

            GlanceTheme {
                VibeWidgetContent(word, translation, contextText, trendReason, trendScore, strings)
            }
        }
    }

    @Composable
    private fun VibeWidgetContent(
        word: String,
        translation: String,
        contextText: String,
        trendReason: String,
        trendScore: Int,
        strings: Localization.UiStrings
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .background(GlanceTheme.colors.surface),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            // Scrollable area for the text content
            LazyColumn(
                modifier = GlanceModifier.defaultWeight().fillMaxWidth()
            ) {
                item {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            text = word,
                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = GlanceTheme.colors.onSurface
                            )
                        )
                        if (translation.isNotEmpty()) {
                            Text(
                                text = translation,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlanceTheme.colors.secondary
                                )
                            )
                        }
                        Spacer(GlanceModifier.height(8.dp))
                        
                        Text(
                            text = contextText,
                            style = TextStyle(
                                fontSize = 13.sp,
                                color = GlanceTheme.colors.onSurfaceVariant
                            )
                        )
                        
                        if (trendReason.isNotEmpty()) {
                            Spacer(GlanceModifier.height(6.dp))
                            Text(
                                text = "${strings.trendReasonLabel} $trendReason",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GlanceTheme.colors.tertiary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(GlanceModifier.height(4.dp))
            
            // Bottom Row: Rating on the left, Icons on the right
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                // Trend Rating (Left Aligned)
                if (trendScore > 0) {
                     Text(
                         text = "${strings.trendRatingLabel} $trendScore/10",
                         style = TextStyle(
                             fontSize = 12.sp,
                             fontWeight = FontWeight.Bold,
                             color = GlanceTheme.colors.error
                         )
                     )
                }

                // Pushes icons to the far right
                Spacer(GlanceModifier.defaultWeight())

                // Icons (Right Aligned)
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh_stylish),
                    contentDescription = strings.refreshButton,
                    modifier = GlanceModifier
                        .clickable(actionRunCallback<RefreshActionCallback>())
                        .padding(6.dp)
                )

                Image(
                    provider = ImageProvider(R.drawable.ic_share_stylish),
                    contentDescription = strings.shareChooserTitle,
                    modifier = GlanceModifier
                        .clickable(
                            actionRunCallback<ShareActionCallback>(
                                actionParametersOf(
                                    ActionParameters.Key<String>("text") to strings.shareText(word, translation),
                                    ActionParameters.Key<String>("chooserTitle") to strings.shareChooserTitle
                                )
                            )
                        )
                        .padding(6.dp)
                )
            }
        }
    }
}

class ShareActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val shareText = parameters[ActionParameters.Key<String>("text")] ?: ""
        val chooserTitle = parameters[ActionParameters.Key<String>("chooserTitle")] ?: "Share"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
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
