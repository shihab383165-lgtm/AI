# YESMIN AI — Futuristic AI Girlfriend & Jarvis Personal Assistant

YESMIN AI is a production-grade, highly customizable Android application combining an empathetic AI Girlfriend / Companion with a Jarvis-class technical assistant, Gemini Live Duplex Voice, an interactive sandboxed Coding Agent, and native Android application bridges.

---

## 🌟 Core Highlights

1. **Custom Assistant Identity & Name**
   - Default: **YESMIN AI** (Assistant name: *Yesmin*, Title: *YESMIN AI*).
   - Fully customizable in **Settings → Profile & Assistant → Assistant Name**. Change to *Muskan*, *Jarvis*, *Sophia*, etc.
   - Name dynamically updates everywhere: Home greetings ("Hi, Jaan - I'm Muskan"), Chat headers, AI system prompts, Live Voice mode, status badges, and notifications.
   - User nickname customizable (e.g. *Jaan*, *Boss*, *Alex*).
   - Companion personality modes:
     - **Girlfriend / Companion**: Caring, friendly, playful, emotionally supportive, English, Hindi & Hinglish fluent.
     - **Friendly Assistant**: Conversational partner and daily guide.
     - **Professional Assistant**: Jarvis-class tactical advisor.
     - **Coding Assistant**: Software architect and debugger.
     - **Study Assistant**: Tutor, mentor, and quiz master.

2. **Gemini Live Duplex Voice Mode**
   - High-fidelity real-time voice loop using Android `SpeechRecognizer` and conversational synthesis.
   - Animated futuristic holographic 3D Orb reacting to voice volume (`rmsAudioDb`) and conversational states (*Idle*, *Listening*, *Thinking*, *Speaking*).
   - Dynamic audio waveform visualization.
   - Tap-to-interrupt assistant speech instantly and resume listening.

3. **Multi-Engine Voice & ElevenLabs TTS**
   - Voice Providers:
     - **Android Native TTS**: High-speed, offline-capable, adjustable pitch & speed.
     - **ElevenLabs AI TTS**: Ultra-realistic human voice generation via `https://api.elevenlabs.io/v1/text-to-speech/{voice_id}`.
     - **Gemini Audio**: Direct AI speech integration.
   - Voice testing playground in Settings with real audio playback.

4. **Jarvis Coding Agent & Execution Sandbox**
   - Multi-file workspace explorer (`.kt`, `.json`, `.xml`, `.md`).
   - Integrated syntax-highlighted code editor with line counts.
   - AI Coding Architect: generate code from prompts (e.g. *"Create a Firebase login page with Compose"*).
   - Safe execution sandbox with live terminal compilation logs and syntax verification.

5. **Native Android Apps & Actions Bridge**
   - Real Android Intent dispatchers for:
     - YouTube (`vnd.youtube:`)
     - WhatsApp (`com.whatsapp`)
     - Gmail (`com.google.android.gm`)
     - Google Maps (`com.google.android.apps.maps`)
     - Telegram (`org.telegram.messenger`)
     - Chrome Browser (`com.android.chrome`)
     - Camera (`MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA`)
     - Calculator (`Intent.CATEGORY_APP_CALCULATOR`)
     - Calendar (`Intent.CATEGORY_APP_CALENDAR`)
     - Instagram (`com.instagram.android`)
     - Custom URL & Deep Link executor.

6. **Persistent Memory System**
   - SQLite/Room database storing user facts, favorites, relationship notes, and personal preferences.
   - Injected directly into the AI system instructions so the assistant naturally recalls your life details across conversations.

7. **Multimodal Vision & Documents**
   - Zero-permission Android Photo Picker for analyzing images, screenshots, code bugs, and visual diagrams.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Local Persistence:** Room Database (`AppDatabase`, `ChatMessageEntity`, `ConversationEntity`, `MemoryEntity`, `TaskEntity`, `ProjectFileEntity`, `SettingEntity`)
- **Networking:** OkHttp 4, Retrofit 2
- **Image Loading:** Coil Compose
- **Speech Engine:** Android `TextToSpeech`, Android `SpeechRecognizer`, ElevenLabs REST API
- **AI Core:** Google Gemini REST API (`gemini-1.5-flash`, `gemini-1.5-pro`, `gemini-2.0-flash`) with dynamic key injection and secret resolution.

---

## 🔑 Environment Configuration (`.env.example`)

Configured via the AI Studio **Secrets Panel** or inside the in-app **Settings → API & Services Manager**:

```env
# Gemini API Key (Injected automatically at runtime by AI Studio Secrets panel)
GEMINI_API_KEY=YOUR_GEMINI_KEY

# ElevenLabs Voice API Key (Optional, for ultra-realistic human voices)
ELEVENLABS_API_KEY=YOUR_ELEVENLABS_KEY
ELEVENLABS_VOICE_ID=21m00Tcm4TlvDq8ikWAM

# OpenAI / Anthropic Keys (Optional fallback providers)
OPENAI_API_KEY=
ANTHROPIC_API_KEY=
```

---

## 🚀 Building & Testing

Run compilation check:
```bash
gradle assembleDebug
```

Unit & Robolectric tests:
```bash
gradle :app:testDebugUnitTest
```
