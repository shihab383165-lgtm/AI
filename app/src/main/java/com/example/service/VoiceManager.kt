package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.api.ElevenLabsApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

sealed class VoiceEngineState {
    object Idle : VoiceEngineState()
    object Listening : VoiceEngineState()
    object Thinking : VoiceEngineState()
    data class Speaking(val text: String) : VoiceEngineState()
    data class Error(val message: String) : VoiceEngineState()
}

class VoiceManager(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val elevenLabsClient = ElevenLabsApiClient()

    private var speechRecognizer: SpeechRecognizer? = null

    private val _engineState = MutableStateFlow<VoiceEngineState>(VoiceEngineState.Idle)
    val engineState: StateFlow<VoiceEngineState> = _engineState.asStateFlow()

    private val _rmsAudioDb = MutableStateFlow(0f)
    val rmsAudioDb: StateFlow<Float> = _rmsAudioDb.asStateFlow()

    var onSpeechRecognized: ((String) -> Unit)? = null
    var onSpeakingComplete: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            tts?.language = Locale.getDefault()
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // Speaking started
                }

                override fun onDone(utteranceId: String?) {
                    _engineState.value = VoiceEngineState.Idle
                    onSpeakingComplete?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    _engineState.value = VoiceEngineState.Idle
                }
            })
        }
    }

    fun speak(
        text: String,
        provider: String = "Browser TTS", // "ElevenLabs", "Browser TTS", "Gemini Voice"
        pitch: Float = 1.0f,
        speed: Float = 1.0f,
        elevenLabsKey: String = "",
        elevenLabsVoiceId: String = ""
    ) {
        stopAll()

        _engineState.value = VoiceEngineState.Speaking(text)

        if (provider == "ElevenLabs" && elevenLabsKey.isNotBlank()) {
            scope.launch {
                val res = elevenLabsClient.synthesizeSpeech(
                    context = context,
                    apiKey = elevenLabsKey,
                    voiceId = elevenLabsVoiceId,
                    text = text
                )
                if (res.isSuccess) {
                    val file = res.getOrThrow()
                    elevenLabsClient.playAudioFile(file) {
                        _engineState.value = VoiceEngineState.Idle
                        onSpeakingComplete?.invoke()
                    }
                } else {
                    // Fallback to native Android TTS if ElevenLabs fails
                    speakWithNativeTts(text, pitch, speed)
                }
            }
        } else {
            speakWithNativeTts(text, pitch, speed)
        }
    }

    private fun speakWithNativeTts(text: String, pitch: Float, speed: Float) {
        if (!isTtsInitialized || tts == null) {
            _engineState.value = VoiceEngineState.Idle
            return
        }
        try {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)
            val utteranceId = "utterance_${System.currentTimeMillis()}"
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        } catch (e: Exception) {
            _engineState.value = VoiceEngineState.Idle
        }
    }

    fun startListening(languageCode: String = "en-US") {
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _engineState.value = VoiceEngineState.Error("Speech recognition is not available on this device.")
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _engineState.value = VoiceEngineState.Listening
                    }

                    override fun onBeginningOfSpeech() {
                        _engineState.value = VoiceEngineState.Listening
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        _rmsAudioDb.value = rmsdB
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _engineState.value = VoiceEngineState.Thinking
                    }

                    override fun onError(error: Int) {
                        val message = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected. Tap mic to try again."
                            SpeechRecognizer.ERROR_NETWORK -> "Network issue in speech recognition."
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
                            else -> "Mic listen ended."
                        }
                        _engineState.value = VoiceEngineState.Idle
                    }

                    override fun onResults(results: Bundle?) {
                        _engineState.value = VoiceEngineState.Idle
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognizedText = matches[0]
                            onSpeechRecognized?.invoke(recognizedText)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!partials.isNullOrEmpty()) {
                            // Can show live preview
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _engineState.value = VoiceEngineState.Error("Unable to start microphone: ${e.localizedMessage}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {}
        if (_engineState.value is VoiceEngineState.Listening) {
            _engineState.value = VoiceEngineState.Idle
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
            elevenLabsClient.stopAudio()
        } catch (e: Exception) {}
        if (_engineState.value is VoiceEngineState.Speaking) {
            _engineState.value = VoiceEngineState.Idle
        }
    }

    fun stopAll() {
        stopListening()
        stopSpeaking()
        _engineState.value = VoiceEngineState.Idle
        _rmsAudioDb.value = 0f
    }

    fun setThinking() {
        _engineState.value = VoiceEngineState.Thinking
    }

    fun destroy() {
        stopAll()
        tts?.shutdown()
        tts = null
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
