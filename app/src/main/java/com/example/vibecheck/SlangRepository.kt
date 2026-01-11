package com.example.vibecheck

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Data model for the Slang of the Day.
 */
data class SlangData(
    val word: String,
    val translation: String,
    val context: String,
    val trendReason: String,
    val trendScore: Int
)

/**
 * Repository to fetch slang from Gemini API.
 */
object SlangRepository {
    private val API_KEY = BuildConfig.GEMINI_API_KEY
    private const val TAG = "SlangRepository"

    private fun getPrompt(language: String) = """
        Act as a real-time trend analyst and linguist. Your task is to scan popular digital platforms—specifically X (Twitter), TikTok, YouTube, Instagram—as well as global news services to identify the most trending English "Word of the Day" for today. 

        The word should reflect current social media discourse, viral events, or major news headlines from the last 24 hours.

        Provide the result in JSON format with the following fields:
        - 'word': [The English word]
        - 'translation': [Translation into $language]
        - 'trendReason': [A brief description in $language explaining why it's popular: the context and which platforms or events made this word trend]
        - 'context': [A short explanation of the word's meaning in $language]
        - 'trendScore': [Popularity Rating: A score from 1 to 10]
        
        Do not include any markdown formatting, just the raw JSON.
    """.trimIndent()

    suspend fun fetchTrendingSlang(language: String = "Russian"): SlangData? {
        var result: SlangData? = null
        var delayMs = 1000L

        // Exponential backoff retry logic (up to 5 times)
        for (i in 0..4) {
            try {
                Log.d(TAG, "Attempt $i to fetch slang using model ${Constants.GEMINI_MODEL}...")
                val response = makeApiCall(getPrompt(language))
                if (response != null) {
                    Log.d(TAG, "Response received: $response")
                    val json = JSONObject(response)
                    
                    // Check for error response from API
                    if (json.has("error")) {
                        Log.e(TAG, "API Error: ${json.getJSONObject("error")}")
                        return null
                    }

                    if (!json.has("candidates")) {
                         Log.e(TAG, "No candidates in response")
                         return null
                    }

                    val candidate = json.getJSONArray("candidates").getJSONObject(0)
                    val textResponse = candidate.getJSONObject("content")
                        .getJSONArray("parts").getJSONObject(0).getString("text")
                    
                    Log.d(TAG, "Raw text response: $textResponse")

                    // Robust JSON extraction
                    val firstBrace = textResponse.indexOf('{')
                    val lastBrace = textResponse.lastIndexOf('}')
                    
                    if (firstBrace != -1 && lastBrace != -1 && firstBrace < lastBrace) {
                        val cleanJson = textResponse.substring(firstBrace, lastBrace + 1)
                        val data = JSONObject(cleanJson)
                        
                        result = SlangData(
                            word = data.getString("word"),
                            translation = data.getString("translation"),
                            context = data.optString("context", ""),
                            trendReason = data.getString("trendReason"),
                            trendScore = data.getInt("trendScore")
                        )
                        break
                    } else {
                        Log.e(TAG, "Could not find JSON braces in response")
                    }
                } else {
                    Log.e(TAG, "Response was null")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception during fetch: ${e.message}", e)
                if (i == 4) return null // Final failure
                delay(delayMs)
                delayMs *= 2
            }
        }
        return result
    }

    private suspend fun makeApiCall(query: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("${Constants.BASE_URL}/models/${Constants.GEMINI_MODEL}:generateContent?key=$API_KEY")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true

            val payload = JSONObject().apply {
                put("contents", org.json.JSONArray().put(JSONObject().apply {
                    put("parts", org.json.JSONArray().put(JSONObject().apply {
                        put("text", query)
                    }))
                }))
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 1.0) // High temperature for randomness
                })
            }

            connection.outputStream.use { it.write(payload.toString().toByteArray()) }

            val responseCode = connection.responseCode
            if (responseCode == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                val errorStream = connection.errorStream?.bufferedReader()?.readText()
                Log.e(TAG, "HTTP Error $responseCode: $errorStream")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network error: ${e.message}", e)
            null
        }
    }
}
