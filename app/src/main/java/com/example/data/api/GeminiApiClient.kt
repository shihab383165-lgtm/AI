package com.example.data.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class AiMessage(
    val role: String, // "user", "model", "system"
    val text: String,
    val imageBase64: String? = null
)

class GeminiApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(
        apiKeyOverride: String?,
        model: String,
        systemInstruction: String,
        conversationHistory: List<AiMessage>,
        userMessage: String,
        imageUri: Uri? = null,
        context: Context? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val resolvedKey = when {
                !apiKeyOverride.isNullOrBlank() -> apiKeyOverride.trim()
                BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY.trim()
                else -> ""
            }

            if (resolvedKey.isBlank()) {
                // Return clear instruction with smart local fallback response
                return@withContext Result.failure(
                    Exception("Gemini API Key is not set. Please add your GEMINI_API_KEY in Settings -> API & Services Manager or the AI Studio Secrets panel.")
                )
            }

            val targetModel = if (model.isBlank()) "gemini-1.5-flash" else model
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=$resolvedKey"

            val rootJson = JSONObject()

            // System instruction
            if (systemInstruction.isNotBlank()) {
                val sysInstObj = JSONObject()
                val partsArray = JSONArray()
                val textPart = JSONObject().put("text", systemInstruction)
                partsArray.put(textPart)
                sysInstObj.put("parts", partsArray)
                rootJson.put("system_instruction", sysInstObj)
            }

            // Contents array
            val contentsArray = JSONArray()

            // History messages (keep last 12 for context window)
            val trimmedHistory = conversationHistory.takeLast(12)
            for (msg in trimmedHistory) {
                val msgObj = JSONObject()
                msgObj.put("role", if (msg.role == "user") "user" else "model")
                val parts = JSONArray()
                parts.put(JSONObject().put("text", msg.text))
                msgObj.put("parts", parts)
                contentsArray.put(msgObj)
            }

            // Current user message
            val currentMsgObj = JSONObject()
            currentMsgObj.put("role", "user")
            val currentParts = JSONArray()

            // Check if there is an image attached
            if (imageUri != null && context != null) {
                try {
                    val base64Image = encodeImageToBase64(context, imageUri)
                    if (base64Image != null) {
                        val inlineData = JSONObject()
                        inlineData.put("mime_type", "image/jpeg")
                        inlineData.put("data", base64Image)
                        currentParts.put(JSONObject().put("inline_data", inlineData))
                    }
                } catch (e: Exception) {
                    // Ignore image error, still send prompt
                }
            }

            currentParts.put(JSONObject().put("text", userMessage))
            currentMsgObj.put("parts", currentParts)
            contentsArray.put(currentMsgObj)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 2048)
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errorObj = JSONObject(responseBody).optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP Error ${response.code}"
                } catch (e: Exception) {
                    "HTTP Error ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("No response received from Gemini"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(apiKey: String, model: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val targetModel = if (model.isBlank()) "gemini-1.5-flash" else model
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$targetModel:generateContent?key=${apiKey.trim()}"
            val payload = JSONObject().apply {
                val contents = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", "Respond with 'Connected successfully' only."))
                        })
                    })
                }
                put("contents", contents)
            }

            val body = payload.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val respStr = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("Success: Gemini API is connected and operational!")
            } else {
                val err = try {
                    JSONObject(respStr).optJSONObject("error")?.optString("message") ?: "Code ${response.code}"
                } catch (e: Exception) {
                    respStr
                }
                Result.failure(Exception("Connection failed: $err"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun encodeImageToBase64(context: Context, uri: Uri): String? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        val outputStream = ByteArrayOutputStream()
        // Resize if too big
        val scaled = if (bitmap.width > 1024 || bitmap.height > 1024) {
            val scale = 1024f / Math.max(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
