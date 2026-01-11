package com.example.vibecheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.vibecheck.ui.theme.VibeCheckTheme
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Main Activity
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setupPeriodicWork()
        setContent {
            VibeCheckTheme {
                 Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val scope = rememberCoroutineScope()
                    
                    val targetLanguage by AppSettings.getLanguage(context).collectAsState(initial = "Russian")

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VibeCheck", style = MaterialTheme.typography.headlineLarge)
                        Text("Твой ежедневный гид по сленгу", style = MaterialTheme.typography.bodyMedium)
                        
                        Spacer(Modifier.height(32.dp))
                        
                        Text("Язык перевода:", style = MaterialTheme.typography.titleMedium)
                        
                        val languages = listOf("Russian", "English", "Spanish", "German", "French")
                        languages.forEach { language ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = (language == targetLanguage),
                                        onClick = {
                                            scope.launch {
                                                AppSettings.setLanguage(context, language)
                                            }
                                        }
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (language == targetLanguage),
                                    onClick = null
                                )
                                Text(
                                    text = language,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))
                        
                        Button(onClick = { forceUpdateSlang() }) {
                            Text("Обновить слово дня сейчас")
                        }
                    }
                }
            }
        }
    }

    private fun setupPeriodicWork() {
        val workRequest = PeriodicWorkRequestBuilder<SlangWorker>(24, TimeUnit.HOURS)
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SlangUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun forceUpdateSlang() {
        val workRequest = OneTimeWorkRequestBuilder<SlangWorker>()
            .build()
        WorkManager.getInstance(this).enqueue(workRequest)
    }
}
