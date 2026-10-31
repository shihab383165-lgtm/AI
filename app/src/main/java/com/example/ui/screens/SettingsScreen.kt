package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AssistantViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val assistantName by viewModel.assistantName.collectAsState()
    val userNickname by viewModel.userNickname.collectAsState()
    val personalityMode by viewModel.personalityMode.collectAsState()
    val greetingStyle by viewModel.greetingStyle.collectAsState()
    val customInstructions by viewModel.customInstructions.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()

    val geminiKey by viewModel.geminiApiKey.collectAsState()
    val geminiModel by viewModel.geminiModel.collectAsState()
    val voiceProvider by viewModel.voiceProvider.collectAsState()
    val elevenKey by viewModel.elevenLabsApiKey.collectAsState()
    val elevenVoiceId by viewModel.elevenLabsVoiceId.collectAsState()
    val pitch by viewModel.ttsPitch.collectAsState()
    val speed by viewModel.ttsSpeed.collectAsState()
    val autoSpeak by viewModel.autoSpeak.collectAsState()

    // Local form states
    var nameInput by remember(assistantName) { mutableStateOf(assistantName) }
    var nicknameInput by remember(userNickname) { mutableStateOf(userNickname) }
    var modeSelected by remember(personalityMode) { mutableStateOf(personalityMode) }
    var greetingSelected by remember(greetingStyle) { mutableStateOf(greetingStyle) }
    var instructionsInput by remember(customInstructions) { mutableStateOf(customInstructions) }

    var geminiKeyInput by remember(geminiKey) { mutableStateOf(geminiKey) }
    var modelSelected by remember(geminiModel) { mutableStateOf(geminiModel) }
    var voiceProviderSelected by remember(voiceProvider) { mutableStateOf(voiceProvider) }
    var elevenKeyInput by remember(elevenKey) { mutableStateOf(elevenKey) }
    var elevenVoiceIdInput by remember(elevenVoiceId) { mutableStateOf(elevenVoiceId) }
    var currentPitch by remember(pitch) { mutableStateOf(pitch) }
    var currentSpeed by remember(speed) { mutableStateOf(speed) }
    var currentAutoSpeak by remember(autoSpeak) { mutableStateOf(autoSpeak) }

    var selectedSection by remember { mutableStateOf(0) } // 0: Profile & Name, 1: AI Providers, 2: Voice & ElevenLabs, 3: Appearance & Reset
    var testResultText by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Settings & Customization", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Configure $assistantName & Services", fontSize = 11.sp, color = NeonViolet)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicSurface)
            )
        },
        containerColor = CosmicBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                containerColor = CosmicSurface,
                contentColor = NeonViolet,
                edgePadding = 12.dp
            ) {
                Tab(
                    selected = selectedSection == 0,
                    onClick = { selectedSection = 0 },
                    text = { Text("Profile & Name", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 1,
                    onClick = { selectedSection = 1 },
                    text = { Text("AI Providers", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 2,
                    onClick = { selectedSection = 2 },
                    text = { Text("Voice & ElevenLabs", fontSize = 12.sp) }
                )
                Tab(
                    selected = selectedSection == 3,
                    onClick = { selectedSection = 3 },
                    text = { Text("Theme & Data", fontSize = 12.sp) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                when (selectedSection) {
                    0 -> {
                        // Section 1: Profile & Custom Name
                        item {
                            FuturisticGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Live Assistant Preview",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CyberMagenta
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "\"Hi, ${nicknameInput.ifBlank { "Jaan" }}! I'm ${nameInput.ifBlank { "Yesmin" }}. How can I help you today?\"",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }

                        item {
                            Text("Assistant Identity", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = { Text("Assistant Name") },
                                placeholder = { Text("e.g. Muskan, Yesmin, Jarvis, Sophia") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("assistant_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonViolet,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                label = { Text("User Nickname (How she addresses you)") },
                                placeholder = { Text("e.g. Jaan, Master, Alex, Boss") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("user_nickname_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonViolet,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            Text("Personality Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            val modes = listOf("Girlfriend / Companion", "Friendly Assistant", "Professional Assistant", "Coding Assistant", "Study Assistant")
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                modes.forEach { mode ->
                                    val isSelected = modeSelected == mode
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { modeSelected = mode },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) NeonViolet.copy(alpha = 0.2f) else CosmicSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonViolet else GlassBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { modeSelected = mode },
                                                colors = RadioButtonDefaults.colors(selectedColor = NeonViolet)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(mode, color = TextPrimary, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Custom Personality Instructions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = instructionsInput,
                                onValueChange = { instructionsInput = it },
                                placeholder = { Text("Additional guidance (e.g. 'Always reply in sweet Hinglish', 'Be very technical when coding')...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonViolet,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.saveAssistantProfile(
                                            name = nameInput,
                                            nickname = nicknameInput,
                                            mode = modeSelected,
                                            style = greetingSelected,
                                            instructions = instructionsInput
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("save_profile_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Save Name & Profile")
                                }

                                OutlinedButton(
                                    onClick = {
                                        viewModel.resetAssistantProfileToDefault()
                                        nameInput = "Yesmin"
                                        nicknameInput = "Jaan"
                                        modeSelected = "Girlfriend / Companion"
                                        instructionsInput = ""
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reset Default")
                                }
                            }
                        }
                    }

                    1 -> {
                        // Section 2: AI Providers (Gemini, OpenAI, Anthropic)
                        item {
                            FuturisticGlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Google Gemini Intelligence (Official)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Powers high-speed conversational empathy, multimodal vision, coding, and live speech reasoning.",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = geminiKeyInput,
                                onValueChange = { geminiKeyInput = it },
                                label = { Text("Gemini API Key") },
                                placeholder = { Text("AI Studio automatically injects this from Secrets") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("gemini_key_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberCyan,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            Text("Gemini Model Version", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            val models = listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-flash")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                models.forEach { mod ->
                                    val isSel = modelSelected == mod
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { modelSelected = mod },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CyberCyan.copy(alpha = 0.2f) else CosmicSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyberCyan else GlassBorder)
                                    ) {
                                        Text(
                                            text = mod,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CyberCyan else TextSecondary,
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    isTesting = true
                                    testResultText = "Testing connection..."
                                    viewModel.testGeminiConnection { success, message ->
                                        isTesting = false
                                        testResultText = message
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_gemini_connection_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isTesting) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Test Gemini Connection", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            if (testResultText != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = testResultText!!,
                                    color = if (testResultText!!.startsWith("Success")) EmeraldGreen else Color(0xFFEF4444),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        item {
                            Button(
                                onClick = {
                                    viewModel.saveApiSettings(
                                        geminiKey = geminiKeyInput,
                                        model = modelSelected,
                                        provider = voiceProviderSelected,
                                        elevenKey = elevenKeyInput,
                                        elevenVoiceId = elevenVoiceIdInput,
                                        pitch = currentPitch,
                                        speed = currentSpeed,
                                        autoSpk = currentAutoSpeak
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Save AI Provider Settings")
                            }
                        }
                    }

                    2 -> {
                        // Section 3: Voice & ElevenLabs
                        item {
                            Text("Voice Output Provider", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            val providers = listOf("Browser TTS", "ElevenLabs", "Gemini Voice")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                providers.forEach { prov ->
                                    val isSel = voiceProviderSelected == prov
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { voiceProviderSelected = prov },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSel) CyberMagenta.copy(alpha = 0.2f) else CosmicSurface,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) CyberMagenta else GlassBorder)
                                    ) {
                                        Text(
                                            text = prov,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSel) CyberMagenta else TextSecondary,
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = elevenKeyInput,
                                onValueChange = { elevenKeyInput = it },
                                label = { Text("ElevenLabs API Key") },
                                placeholder = { Text("xi-api-key...") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberMagenta,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = elevenVoiceIdInput,
                                onValueChange = { elevenVoiceIdInput = it },
                                label = { Text("ElevenLabs Voice ID") },
                                placeholder = { Text("e.g. 21m00Tcm4TlvDq8ikWAM (Rachel)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberMagenta,
                                    unfocusedBorderColor = GlassBorder,
                                    focusedContainerColor = CosmicSurface,
                                    unfocusedContainerColor = CosmicSurface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        item {
                            Text("Voice Pitch: ${String.format("%.2f", currentPitch)}x", fontSize = 13.sp, color = TextPrimary)
                            Slider(
                                value = currentPitch,
                                onValueChange = { currentPitch = it },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = CyberMagenta, activeTrackColor = CyberMagenta)
                            )
                        }

                        item {
                            Text("Speech Speed: ${String.format("%.2f", currentSpeed)}x", fontSize = 13.sp, color = TextPrimary)
                            Slider(
                                value = currentSpeed,
                                onValueChange = { currentSpeed = it },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(thumbColor = CyberMagenta, activeTrackColor = CyberMagenta)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Auto-Speak AI Responses", fontSize = 13.sp, color = TextPrimary)
                                Switch(
                                    checked = currentAutoSpeak,
                                    onCheckedChange = { currentAutoSpeak = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CyberMagenta, checkedTrackColor = CyberMagenta.copy(alpha = 0.4f))
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (voiceProviderSelected == "ElevenLabs") {
                                            viewModel.testElevenLabsVoice(context) { success, msg ->
                                                viewModel.showFeedback(msg)
                                            }
                                        } else {
                                            viewModel.speakText("Hi $userNickname, I am $assistantName. Voice playback is operational!")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Test Voice")
                                }

                                Button(
                                    onClick = {
                                        viewModel.saveApiSettings(
                                            geminiKey = geminiKeyInput,
                                            model = modelSelected,
                                            provider = voiceProviderSelected,
                                            elevenKey = elevenKeyInput,
                                            elevenVoiceId = elevenVoiceIdInput,
                                            pitch = currentPitch,
                                            speed = currentSpeed,
                                            autoSpk = currentAutoSpeak
                                        )
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Save Voice")
                                }
                            }
                        }
                    }

                    3 -> {
                        // Section 4: Theme, Accent & Storage Reset
                        item {
                            Text("Cyber Glow Accent Color", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(10.dp))
                            val accentColors = listOf(
                                "Neon Violet" to NeonViolet,
                                "Cyber Magenta" to CyberMagenta,
                                "Cyber Cyan" to CyberCyan,
                                "Emerald Glow" to EmeraldGreen,
                                "Solar Gold" to SolarGold
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                accentColors.forEachIndexed { idx, (label, color) ->
                                    val isSelected = accentIndex == idx
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(
                                                width = if (isSelected) 3.dp else 1.dp,
                                                color = if (isSelected) Color.White else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable { viewModel.setAccentColorIndex(idx) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Storage & Data Reset", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.clearAllMemory() },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear All User Memories", color = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }
}
