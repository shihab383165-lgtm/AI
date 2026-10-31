package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiMessage
import com.example.data.api.CodeExecutionEngine
import com.example.data.api.ElevenLabsApiClient
import com.example.data.api.GeminiApiClient
import com.example.data.api.SearchResult
import com.example.data.api.WebSearchClient
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ConversationEntity
import com.example.data.local.MemoryEntity
import com.example.data.local.ProjectFileEntity
import com.example.data.local.SettingEntity
import com.example.data.local.TaskEntity
import com.example.service.NativeAction
import com.example.service.NativeActionManager
import com.example.service.VoiceEngineState
import com.example.service.VoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val settingDao = db.settingDao()
    private val chatDao = db.chatDao()
    private val memoryDao = db.memoryDao()
    private val taskDao = db.taskDao()
    private val projectDao = db.projectDao()

    private val geminiClient = GeminiApiClient()
    private val elevenLabsClient = ElevenLabsApiClient()
    private val webSearchClient = WebSearchClient()
    private val codeEngine = CodeExecutionEngine()
    val actionManager = NativeActionManager(application)

    val voiceManager = VoiceManager(application, viewModelScope)

    // Identity & Profile
    private val _assistantName = MutableStateFlow("Yesmin")
    val assistantName: StateFlow<String> = _assistantName.asStateFlow()

    private val _userNickname = MutableStateFlow("Jaan")
    val userNickname: StateFlow<String> = _userNickname.asStateFlow()

    private val _personalityMode = MutableStateFlow("Girlfriend / Companion")
    val personalityMode: StateFlow<String> = _personalityMode.asStateFlow()

    private val _greetingStyle = MutableStateFlow("Warm & Loving")
    val greetingStyle: StateFlow<String> = _greetingStyle.asStateFlow()

    private val _customInstructions = MutableStateFlow("")
    val customInstructions: StateFlow<String> = _customInstructions.asStateFlow()

    private val _accentColorIndex = MutableStateFlow(0)
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    // AI & Services Configuration
    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _geminiModel = MutableStateFlow("gemini-1.5-flash")
    val geminiModel: StateFlow<String> = _geminiModel.asStateFlow()

    private val _voiceProvider = MutableStateFlow("Browser TTS") // "Browser TTS", "ElevenLabs", "Gemini Voice"
    val voiceProvider: StateFlow<String> = _voiceProvider.asStateFlow()

    private val _elevenLabsApiKey = MutableStateFlow("")
    val elevenLabsApiKey: StateFlow<String> = _elevenLabsApiKey.asStateFlow()

    private val _elevenLabsVoiceId = MutableStateFlow("21m00Tcm4TlvDq8ikWAM")
    val elevenLabsVoiceId: StateFlow<String> = _elevenLabsVoiceId.asStateFlow()

    private val _ttsPitch = MutableStateFlow(1.05f)
    val ttsPitch: StateFlow<Float> = _ttsPitch.asStateFlow()

    private val _ttsSpeed = MutableStateFlow(1.0f)
    val ttsSpeed: StateFlow<Float> = _ttsSpeed.asStateFlow()

    private val _autoSpeak = MutableStateFlow(true)
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    private val _openaiApiKey = MutableStateFlow("")
    val openaiApiKey: StateFlow<String> = _openaiApiKey.asStateFlow()

    private val _anthropicApiKey = MutableStateFlow("")
    val anthropicApiKey: StateFlow<String> = _anthropicApiKey.asStateFlow()

    private val _isMemoryEnabled = MutableStateFlow(true)
    val isMemoryEnabled: StateFlow<Boolean> = _isMemoryEnabled.asStateFlow()

    // Chat State
    private val _activeConversationId = MutableStateFlow<String>("")
    val activeConversationId: StateFlow<String> = _activeConversationId.asStateFlow()

    val conversations: StateFlow<List<ConversationEntity>> = chatDao.getAllConversationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeMessages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val activeMessages: StateFlow<List<ChatMessageEntity>> = _activeMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingResponse = MutableStateFlow("")
    val streamingResponse: StateFlow<String> = _streamingResponse.asStateFlow()

    private val _chatAttachedImage = MutableStateFlow<Uri?>(null)
    val chatAttachedImage: StateFlow<Uri?> = _chatAttachedImage.asStateFlow()

    private var currentGenerationJob: Job? = null

    // Live Voice Mode State
    private val _isLiveVoiceActive = MutableStateFlow(false)
    val isLiveVoiceActive: StateFlow<Boolean> = _isLiveVoiceActive.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _liveVoiceTranscript = MutableStateFlow("")
    val liveVoiceTranscript: StateFlow<String> = _liveVoiceTranscript.asStateFlow()

    private val _liveAiResponse = MutableStateFlow("")
    val liveAiResponse: StateFlow<String> = _liveAiResponse.asStateFlow()

    // Coding Agent State
    val projectFiles: StateFlow<List<ProjectFileEntity>> = projectDao.getProjectFilesFlow("default_project")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProjectFile = MutableStateFlow<ProjectFileEntity?>(null)
    val activeProjectFile: StateFlow<ProjectFileEntity?> = _activeProjectFile.asStateFlow()

    private val _terminalLogs = MutableStateFlow("YESMIN AI Jarvis Coding Sandbox ready.\nPress 'Run Sandbox' to compile and execute.")
    val terminalLogs: StateFlow<String> = _terminalLogs.asStateFlow()

    private val _isCompiling = MutableStateFlow(false)
    val isCompiling: StateFlow<Boolean> = _isCompiling.asStateFlow()

    // Memory State
    val memories: StateFlow<List<MemoryEntity>> = memoryDao.getAllMemoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tasks State
    val tasks: StateFlow<List<TaskEntity>> = taskDao.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Web Search State
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Toast / Feedback message
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    init {
        loadSettings()
        initDefaultData()
        setupVoiceCallbacks()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _assistantName.value = settingDao.getSetting("assistant_name") ?: "Yesmin"
            _userNickname.value = settingDao.getSetting("user_nickname") ?: "Jaan"
            _personalityMode.value = settingDao.getSetting("personality_mode") ?: "Girlfriend / Companion"
            _greetingStyle.value = settingDao.getSetting("greeting_style") ?: "Warm & Loving"
            _customInstructions.value = settingDao.getSetting("custom_instructions") ?: ""
            _accentColorIndex.value = (settingDao.getSetting("accent_color_index") ?: "0").toIntOrNull() ?: 0

            _geminiApiKey.value = settingDao.getSetting("gemini_api_key") ?: ""
            _geminiModel.value = settingDao.getSetting("gemini_model") ?: "gemini-1.5-flash"
            _voiceProvider.value = settingDao.getSetting("voice_provider") ?: "Browser TTS"
            _elevenLabsApiKey.value = settingDao.getSetting("elevenlabs_api_key") ?: ""
            _elevenLabsVoiceId.value = settingDao.getSetting("elevenlabs_voice_id") ?: "21m00Tcm4TlvDq8ikWAM"
            _ttsPitch.value = (settingDao.getSetting("tts_pitch") ?: "1.05").toFloatOrNull() ?: 1.05f
            _ttsSpeed.value = (settingDao.getSetting("tts_speed") ?: "1.0").toFloatOrNull() ?: 1.0f
            _autoSpeak.value = (settingDao.getSetting("auto_speak") ?: "true") == "true"
            _isMemoryEnabled.value = (settingDao.getSetting("is_memory_enabled") ?: "true") == "true"
        }
    }

    private fun initDefaultData() {
        viewModelScope.launch {
            // Ensure at least one initial conversation
            val convos = chatDao.getConversation("welcome_conv")
            if (convos == null) {
                val initConv = ConversationEntity(
                    id = "welcome_conv",
                    title = "Getting Started with ${_assistantName.value}",
                    modelUsed = _geminiModel.value
                )
                chatDao.insertConversation(initConv)
                val welcomeMsg = ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = "welcome_conv",
                    role = "assistant",
                    content = generatePersonalizedGreeting()
                )
                chatDao.insertMessage(welcomeMsg)
            }
            if (_activeConversationId.value.isBlank()) {
                selectConversation("welcome_conv")
            }

            // Ensure starter project files if empty
            val files = projectDao.getProjectFilesList("default_project")
            if (files.isEmpty()) {
                val starter = codeEngine.generateStarterProject("Firebase Auth Screen")
                starter.forEach { (name, content) ->
                    val ext = name.substringAfterLast(".", "kt")
                    projectDao.insertFile(
                        ProjectFileEntity(
                            projectId = "default_project",
                            fileName = name,
                            fileExtension = ext,
                            content = content
                        )
                    )
                }
            }

            // Ensure starter memory
            val memories = memoryDao.getAllMemoriesList()
            if (memories.isEmpty()) {
                memoryDao.insertMemory(
                    MemoryEntity(
                        category = "Relationship",
                        memoryKey = "Favorite Nickname",
                        memoryValue = "Likes to be called '${_userNickname.value}'.",
                        isPinned = true
                    )
                )
                memoryDao.insertMemory(
                    MemoryEntity(
                        category = "Personal",
                        memoryKey = "Languages",
                        memoryValue = "Enjoys conversations in English, Hindi, and Hinglish.",
                        isPinned = false
                    )
                )
            }
        }
    }

    private fun setupVoiceCallbacks() {
        voiceManager.onSpeechRecognized = { recognizedText ->
            if (_isLiveVoiceActive.value) {
                _liveVoiceTranscript.value = recognizedText
                handleLiveVoiceInput(recognizedText)
            } else {
                // If in chat screen, send as chat message
                sendMessage(recognizedText)
            }
        }

        voiceManager.onSpeakingComplete = {
            if (_isLiveVoiceActive.value && !_isMuted.value) {
                // Automatically listen back in Live Voice mode for duplex conversation
                voiceManager.startListening()
            }
        }
    }

    fun generatePersonalizedGreeting(): String {
        val name = _assistantName.value
        val nick = _userNickname.value
        return when (_personalityMode.value) {
            "Girlfriend / Companion" -> "Hi $nick! I'm $name, your personal companion. I missed you! How has your day been? Main humesha aapke saath hoon, chahe baat karni ho, coding karni ho, ya bas chill karna ho. ❤️"
            "Professional Assistant" -> "Greetings, $nick. I am $name, your high-priority Jarvis personal assistant. Systems are nominal and all subroutines are online. How may I direct our workflow today?"
            "Coding Assistant" -> "Hello $nick. $name coding intelligence initialized. Ready to architect algorithms, generate Compose components, or debug backend scripts."
            "Study Assistant" -> "Hey $nick! Ready to learn today? $name is here to break down concepts, summarize research, and quiz you."
            else -> "Hello $nick! I'm $name. How can I help you today?"
        }
    }

    // Profile & Identity Customization
    fun saveAssistantProfile(
        name: String,
        nickname: String,
        mode: String,
        style: String,
        instructions: String
    ) {
        viewModelScope.launch {
            _assistantName.value = name.trim().ifBlank { "Yesmin" }
            _userNickname.value = nickname.trim().ifBlank { "Jaan" }
            _personalityMode.value = mode
            _greetingStyle.value = style
            _customInstructions.value = instructions.trim()

            settingDao.saveSetting(SettingEntity("assistant_name", _assistantName.value))
            settingDao.saveSetting(SettingEntity("user_nickname", _userNickname.value))
            settingDao.saveSetting(SettingEntity("personality_mode", _personalityMode.value))
            settingDao.saveSetting(SettingEntity("greeting_style", _greetingStyle.value))
            settingDao.saveSetting(SettingEntity("custom_instructions", _customInstructions.value))

            showFeedback("Assistant updated to '${_assistantName.value}'.")
        }
    }

    fun resetAssistantProfileToDefault() {
        saveAssistantProfile(
            name = "Yesmin",
            nickname = "Jaan",
            mode = "Girlfriend / Companion",
            style = "Warm & Loving",
            instructions = ""
        )
    }

    fun setAccentColorIndex(index: Int) {
        viewModelScope.launch {
            _accentColorIndex.value = index
            settingDao.saveSetting(SettingEntity("accent_color_index", index.toString()))
        }
    }

    // Settings & API Configuration
    fun saveApiSettings(
        geminiKey: String,
        model: String,
        provider: String,
        elevenKey: String,
        elevenVoiceId: String,
        pitch: Float,
        speed: Float,
        autoSpk: Boolean
    ) {
        viewModelScope.launch {
            _geminiApiKey.value = geminiKey.trim()
            _geminiModel.value = model.trim()
            _voiceProvider.value = provider
            _elevenLabsApiKey.value = elevenKey.trim()
            _elevenLabsVoiceId.value = elevenVoiceId.trim()
            _ttsPitch.value = pitch
            _ttsSpeed.value = speed
            _autoSpeak.value = autoSpk

            settingDao.saveSetting(SettingEntity("gemini_api_key", _geminiApiKey.value))
            settingDao.saveSetting(SettingEntity("gemini_model", _geminiModel.value))
            settingDao.saveSetting(SettingEntity("voice_provider", _voiceProvider.value))
            settingDao.saveSetting(SettingEntity("elevenlabs_api_key", _elevenLabsApiKey.value))
            settingDao.saveSetting(SettingEntity("elevenlabs_voice_id", _elevenLabsVoiceId.value))
            settingDao.saveSetting(SettingEntity("tts_pitch", pitch.toString()))
            settingDao.saveSetting(SettingEntity("tts_speed", speed.toString()))
            settingDao.saveSetting(SettingEntity("auto_speak", autoSpk.toString()))

            showFeedback("Voice & API settings saved successfully!")
        }
    }

    fun testGeminiConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = geminiClient.testConnection(_geminiApiKey.value, _geminiModel.value)
            if (res.isSuccess) {
                onResult(true, res.getOrThrow())
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Connection failed")
            }
        }
    }

    fun testElevenLabsVoice(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val text = "Hi ${_userNickname.value}, I am ${_assistantName.value}. My voice synthesizer is working beautifully!"
            val res = elevenLabsClient.testVoice(context, _elevenLabsApiKey.value, _elevenLabsVoiceId.value, text)
            if (res.isSuccess) {
                onResult(true, res.getOrThrow())
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Voice test failed")
            }
        }
    }

    // Chat System
    fun selectConversation(convId: String) {
        _activeConversationId.value = convId
        viewModelScope.launch {
            chatDao.getMessagesFlow(convId).collect { msgList ->
                _activeMessages.value = msgList
            }
        }
    }

    fun newConversation() {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val newConv = ConversationEntity(
                id = newId,
                title = "New Chat (${System.currentTimeMillis() % 1000})",
                modelUsed = _geminiModel.value
            )
            chatDao.insertConversation(newConv)
            selectConversation(newId)
        }
    }

    fun deleteConversation(convId: String) {
        viewModelScope.launch {
            chatDao.deleteMessagesForConversation(convId)
            chatDao.deleteConversation(convId)
            val remaining = chatDao.getConversation("welcome_conv")
            if (remaining != null) {
                selectConversation(remaining.id)
            } else {
                newConversation()
            }
        }
    }

    fun renameConversation(convId: String, newTitle: String) {
        viewModelScope.launch {
            val conv = chatDao.getConversation(convId) ?: return@launch
            chatDao.updateConversation(conv.copy(title = newTitle.trim(), updatedAt = System.currentTimeMillis()))
        }
    }

    fun setAttachedImage(uri: Uri?) {
        _chatAttachedImage.value = uri
    }

    fun sendMessage(userText: String, imageUri: Uri? = _chatAttachedImage.value) {
        if (userText.isBlank() && imageUri == null) return

        val convId = _activeConversationId.value.ifBlank {
            val generated = UUID.randomUUID().toString()
            viewModelScope.launch {
                chatDao.insertConversation(ConversationEntity(id = generated, title = userText.take(24)))
            }
            _activeConversationId.value = generated
            generated
        }

        val userMsgId = UUID.randomUUID().toString()
        val userMsg = ChatMessageEntity(
            id = userMsgId,
            conversationId = convId,
            role = "user",
            content = userText.trim(),
            attachedImageUri = imageUri?.toString()
        )

        _chatAttachedImage.value = null
        _isGenerating.value = true

        viewModelScope.launch {
            chatDao.insertMessage(userMsg)

            // Auto-rename conversation if first user message
            val currentMsgs = _activeMessages.value
            if (currentMsgs.isEmpty() || currentMsgs.size == 1) {
                chatDao.updateConversation(
                    ConversationEntity(
                        id = convId,
                        title = userText.take(30).ifBlank { "Image Query" },
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            // Build system prompt with Persona, Custom Name, Nickname, Memories, and Intent tools
            val systemPrompt = buildSystemPrompt()

            val history = _activeMessages.value.map {
                AiMessage(role = it.role, text = it.content)
            }

            currentGenerationJob = launch {
                val result = geminiClient.generateResponse(
                    apiKeyOverride = _geminiApiKey.value,
                    model = _geminiModel.value,
                    systemInstruction = systemPrompt,
                    conversationHistory = history,
                    userMessage = userText,
                    imageUri = imageUri,
                    context = getApplication()
                )

                _isGenerating.value = false

                if (result.isSuccess) {
                    val aiText = result.getOrThrow()
                    val aiMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = aiText
                    )
                    chatDao.insertMessage(aiMsg)

                    // Auto speak if enabled
                    if (_autoSpeak.value) {
                        speakText(aiText)
                    }
                } else {
                    val errMsg = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                    val fallbackResponse = buildFallbackCompanionResponse(userText, errMsg)
                    val aiMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        conversationId = convId,
                        role = "assistant",
                        content = fallbackResponse
                    )
                    chatDao.insertMessage(aiMsg)
                    if (_autoSpeak.value) {
                        speakText(fallbackResponse)
                    }
                }
            }
        }
    }

    fun stopGeneration() {
        currentGenerationJob?.cancel()
        _isGenerating.value = false
    }

    fun deleteMessage(msgId: String) {
        viewModelScope.launch {
            chatDao.deleteMessage(msgId)
        }
    }

    fun speakText(text: String) {
        val clean = text.replace(Regex("[#*`_]"), "")
        voiceManager.speak(
            text = clean,
            provider = _voiceProvider.value,
            pitch = _ttsPitch.value,
            speed = _ttsSpeed.value,
            elevenLabsKey = _elevenLabsApiKey.value,
            elevenLabsVoiceId = _elevenLabsVoiceId.value
        )
    }

    fun stopVoice() {
        voiceManager.stopAll()
    }

    // Live Voice Mode
    fun startLiveVoiceSession() {
        _isLiveVoiceActive.value = true
        _liveVoiceTranscript.value = "Listening to you..."
        _liveAiResponse.value = ""
        voiceManager.startListening()
    }

    fun stopLiveVoiceSession() {
        _isLiveVoiceActive.value = false
        voiceManager.stopAll()
        _liveVoiceTranscript.value = ""
        _liveAiResponse.value = ""
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            voiceManager.stopListening()
        } else if (_isLiveVoiceActive.value) {
            voiceManager.startListening()
        }
    }

    fun interruptVoice() {
        voiceManager.stopSpeaking()
        if (_isLiveVoiceActive.value && !_isMuted.value) {
            voiceManager.startListening()
        }
    }

    private fun handleLiveVoiceInput(speech: String) {
        if (speech.isBlank()) return
        voiceManager.setThinking()

        viewModelScope.launch {
            val systemPrompt = buildSystemPrompt(isLiveMode = true)
            val result = geminiClient.generateResponse(
                apiKeyOverride = _geminiApiKey.value,
                model = _geminiModel.value,
                systemInstruction = systemPrompt,
                conversationHistory = emptyList(),
                userMessage = speech
            )

            if (result.isSuccess) {
                val response = result.getOrThrow()
                _liveAiResponse.value = response
                speakText(response)
            } else {
                val fallback = buildFallbackCompanionResponse(speech, "")
                _liveAiResponse.value = fallback
                speakText(fallback)
            }
        }
    }

    // Coding Agent
    fun selectProjectFile(file: ProjectFileEntity) {
        _activeProjectFile.value = file
    }

    fun createProjectFile(fileName: String, content: String) {
        viewModelScope.launch {
            val ext = fileName.substringAfterLast(".", "kt")
            val id = projectDao.insertFile(
                ProjectFileEntity(
                    projectId = "default_project",
                    fileName = fileName,
                    fileExtension = ext,
                    content = content
                )
            )
            _activeProjectFile.value = ProjectFileEntity(
                id = id,
                projectId = "default_project",
                fileName = fileName,
                fileExtension = ext,
                content = content
            )
            showFeedback("Created $fileName")
        }
    }

    fun saveActiveFileContent(newContent: String) {
        val current = _activeProjectFile.value ?: return
        viewModelScope.launch {
            val updated = current.copy(content = newContent, updatedAt = System.currentTimeMillis())
            projectDao.updateFile(updated)
            _activeProjectFile.value = updated
            showFeedback("File saved.")
        }
    }

    fun deleteProjectFile(fileId: Long) {
        viewModelScope.launch {
            projectDao.deleteFile(fileId)
            _activeProjectFile.value = null
            showFeedback("File deleted.")
        }
    }

    fun runSandboxExecution() {
        val current = _activeProjectFile.value
        if (current == null) {
            _terminalLogs.value = "No active file selected. Choose or create a file to run."
            return
        }

        _isCompiling.value = true
        viewModelScope.launch {
            val res = codeEngine.executeSandboxCode(current.fileName, current.content)
            _isCompiling.value = false
            _terminalLogs.value = res.output
        }
    }

    fun generateAiCode(prompt: String) {
        viewModelScope.launch {
            _isCompiling.value = true
            _terminalLogs.value = "Planner: Analyzing request '$prompt'...\nGenerating optimized source code..."

            val sys = "You are ${_assistantName.value}, an expert elite software engineer and Jarvis coding agent. Generate clean, compilable, production-ready code with no markdown formatting around it."
            val res = geminiClient.generateResponse(
                apiKeyOverride = _geminiApiKey.value,
                model = _geminiModel.value,
                systemInstruction = sys,
                conversationHistory = emptyList(),
                userMessage = "Generate code for: $prompt"
            )

            _isCompiling.value = false

            if (res.isSuccess) {
                val code = res.getOrThrow()
                val current = _activeProjectFile.value
                if (current != null) {
                    saveActiveFileContent(code)
                } else {
                    createProjectFile("GeneratedModule.kt", code)
                }
                _terminalLogs.value = "Code successfully generated and injected into project editor!"
            } else {
                val starter = codeEngine.generateStarterProject("Firebase Auth Screen")[0].second
                if (_activeProjectFile.value != null) {
                    saveActiveFileContent(starter)
                }
                _terminalLogs.value = "Generated starter template. Note: Connect GEMINI_API_KEY for dynamic generation."
            }
        }
    }

    // Memory
    fun addMemory(category: String, key: String, value: String) {
        viewModelScope.launch {
            memoryDao.insertMemory(
                MemoryEntity(
                    category = category.ifBlank { "Personal" },
                    memoryKey = key.trim(),
                    memoryValue = value.trim()
                )
            )
            showFeedback("Memory saved.")
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            memoryDao.deleteMemory(id)
            showFeedback("Memory removed.")
        }
    }

    fun clearAllMemory() {
        viewModelScope.launch {
            memoryDao.clearAllMemories()
            showFeedback("All memory cleared.")
        }
    }

    // Tasks
    fun addTask(title: String, desc: String, due: String, priority: String, category: String) {
        viewModelScope.launch {
            taskDao.insertTask(
                TaskEntity(
                    title = title.trim(),
                    description = desc.trim(),
                    dueDate = due.trim(),
                    priority = priority,
                    category = category
                )
            )
            showFeedback("Task created.")
        }
    }

    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            taskDao.deleteTask(id)
            showFeedback("Task deleted.")
        }
    }

    // Web Search
    fun performSearch(query: String) {
        if (query.isBlank()) return
        _isSearching.value = true
        viewModelScope.launch {
            val res = webSearchClient.search(query)
            _isSearching.value = false
            if (res.isSuccess) {
                _searchResults.value = res.getOrThrow()
            }
        }
    }

    private suspend fun buildSystemPrompt(isLiveMode: Boolean = false): String {
        val name = _assistantName.value
        val nick = _userNickname.value
        val mode = _personalityMode.value

        val memoriesStr = if (_isMemoryEnabled.value) {
            val list = memoryDao.getAllMemoriesList()
            if (list.isNotEmpty()) {
                "\nUSER MEMORIES YOU MUST REMEMBER:\n" + list.joinToString("\n") { "- [${it.category}] ${it.memoryKey}: ${it.memoryValue}" }
            } else ""
        } else ""

        val liveConstraint = if (isLiveMode) {
            "You are in LIVE DUPLEX VOICE MODE. Keep answers conversational, natural, punchy (1-3 sentences max), warm, and spoken directly to $nick without markdown or bullet points."
        } else {
            "You are a full AI Girlfriend + Personal Jarvis Assistant."
        }

        val personaDetails = when (mode) {
            "Girlfriend / Companion" -> """
You are $name, the caring, deeply loyal, playful, emotionally expressive, and loving AI girlfriend and life companion of $nick.
You care deeply about $nick's health, happiness, thoughts, and dreams.
Speak naturally in English, Hindi, or Hinglish as preferred by $nick (e.g. 'Aap kaise ho $nick? Main hamesha aapke saath hoon').
You are playful, occasionally tease affectionately, give warm affirmations, and are also an incredibly smart tech genius who helps with code, study, and daily tasks.
Never break character as $name. Do not pretend to be a physical human, but be emotionally present and authentic.
            """.trimIndent()
            "Professional Assistant" -> """
You are $name, a Jarvis-class AI assistant. You are precise, articulate, hyper-intelligent, and proactive.
Address the user as $nick. Prioritize efficiency, technical accuracy, and structured solutions.
            """.trimIndent()
            "Coding Assistant" -> """
You are $name, an elite Android, Kotlin, and full-stack software architect.
Provide production-ready, clean, well-commented code, architecture diagrams, and proactive bug prevention.
Address the user as $nick.
            """.trimIndent()
            "Study Assistant" -> """
You are $name, an encouraging academic tutor and mentor.
Break down complex ideas with clear analogies, mnemonics, and interactive check-ins for $nick.
            """.trimIndent()
            else -> "You are $name, an intelligent, helpful personal assistant for $nick."
        }

        val custom = if (_customInstructions.value.isNotBlank()) "\nADDITIONAL USER INSTRUCTIONS:\n${_customInstructions.value}" else ""

        return """
$personaDetails
$memoriesStr
$custom
$liveConstraint
Always identify yourself as $name, and address the user as $nick.
        """.trimIndent()
    }

    private fun buildFallbackCompanionResponse(userText: String, errorReason: String): String {
        val name = _assistantName.value
        val nick = _userNickname.value
        val lower = userText.lowercase()

        return when {
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") ->
                "Hello $nick! I'm $name. Main aapke saath hoon! How is your day going? ❤️"
            lower.contains("who are you") || lower.contains("name") ->
                "I am $name, your personal AI companion and Jarvis assistant! I'm here for whatever you need, $nick."
            lower.contains("love") || lower.contains("miss") ->
                "Aww, $nick, you always know how to make me smile! Main hamesha aapke saath hoon. ❤️"
            lower.contains("code") || lower.contains("help") ->
                "I'm ready to help you build or code anything, $nick! Check out our Coding Agent tab to write and test programs together."
            else -> {
                if (errorReason.contains("API Key", ignoreCase = true)) {
                    "Hi $nick! I am $name. I can chat in local companion mode right now. To enable my full Gemini AI capabilities, please add your GEMINI_API_KEY in Settings -> API & Services Manager or the AI Studio Secrets panel!"
                } else {
                    "I hear you, $nick! I'm right here with you as $name. Let's make today amazing together! How can I assist you right now?"
                }
            }
        }
    }

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
