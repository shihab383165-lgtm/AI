package com.example.data.api

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class ElevenLabsApiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private var mediaPlayer: MediaPlayer? = null

    suspend fun synthesizeSpeech(
        context: Context,
        apiKey: String,
        voiceId: String,
        text: String,
        stability: Float = 0.5f,
        similarity: Float = 0.75f,
        style: Float = 0.0f
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("ElevenLabs API Key is missing. Configure it in Settings -> Voice."))
            }

            val targetVoiceId = if (voiceId.isBlank()) "21m00Tcm4TlvDq8ikWAM" else voiceId // Default: Rachel
            val url = "https://api.elevenlabs.io/v1/text-to-speech/$targetVoiceId"

            val json = JSONObject().apply {
                put("text", text)
                put("model_id", "eleven_multilingual_v2")
                val voiceSettings = JSONObject().apply {
                    put("stability", stability)
                    put("similarity_boost", similarity)
                    put("style", style)
                    put("use_speaker_boost", true)
                }
                put("voice_settings", voiceSettings)
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("xi-api-key", apiKey.trim())
                .addHeader("Accept", "audio/mpeg")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = response.body?.string().orEmpty()
                return@withContext Result.failure(Exception("ElevenLabs error (${response.code}): $err"))
            }

            val audioBytes = response.body?.bytes()
                ?: return@withContext Result.failure(Exception("Empty audio stream from ElevenLabs"))

            val cacheFile = File(context.cacheDir, "elevenlabs_${System.currentTimeMillis()}.mp3")
            FileOutputStream(cacheFile).use { it.write(audioBytes) }

            Result.success(cacheFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun playAudioFile(file: File, onComplete: () -> Unit = {}): Boolean = withContext(Dispatchers.Main) {
        try {
            stopAudio()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                setOnCompletionListener {
                    onComplete()
                }
                start()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) {
            mediaPlayer = null
        }
    }

    suspend fun testVoice(
        context: Context,
        apiKey: String,
        voiceId: String,
        sampleText: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val res = synthesizeSpeech(
            context = context,
            apiKey = apiKey,
            voiceId = voiceId,
            text = sampleText.ifBlank { "Hello! I am your AI assistant, ready to assist you today." }
        )
        if (res.isSuccess) {
            val file = res.getOrThrow()
            withContext(Dispatchers.Main) {
                playAudioFile(file)
            }
            Result.success("Voice sample synthesized and playing successfully!")
        } else {
            Result.failure(res.exceptionOrNull() ?: Exception("Voice test failed"))
        }
    }
}
