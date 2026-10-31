package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.service.VoiceEngineState
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@Composable
fun LiveVoiceScreen(
    viewModel: AssistantViewModel,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val assistantName by viewModel.assistantName.collectAsState()
    val userNickname by viewModel.userNickname.collectAsState()
    val isLiveActive by viewModel.isLiveVoiceActive.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val voiceState by viewModel.voiceManager.engineState.collectAsState()
    val rmsDb by viewModel.voiceManager.rmsAudioDb.collectAsState()
    val liveTranscript by viewModel.liveVoiceTranscript.collectAsState()
    val liveAiResponse by viewModel.liveAiResponse.collectAsState()
    val voiceProvider by viewModel.voiceProvider.collectAsState()

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
        if (isGranted) {
            viewModel.startLiveVoiceSession()
        }
    }

    // Auto-start on entering screen if permission granted
    LaunchedEffect(Unit) {
        if (hasMicPermission) {
            viewModel.startLiveVoiceSession()
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Waveform bar animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveAnim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveAnim2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveAnim3 by infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "w3"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CosmicBackground,
                        Color(0xFF0F0A24),
                        CosmicBackground
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top status row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$assistantName LIVE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Engine: $voiceProvider • English / Hindi / Hinglish",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            // Connection indicator
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CosmicSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLiveActive) EmeraldGreen else Color(0xFFEF4444))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLiveActive) "DUPLEX LIVE" else "DISCONNECTED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Center Animated Orb & Waveforms
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            AnimatedAiOrb(
                state = voiceState,
                rmsDb = rmsDb,
                size = 220.dp,
                assistantName = assistantName,
                onClick = {
                    if (voiceState is VoiceEngineState.Speaking) {
                        viewModel.interruptVoice()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // State label
            Text(
                text = when (voiceState) {
                    is VoiceEngineState.Listening -> "I'm listening to you, $userNickname..."
                    is VoiceEngineState.Thinking -> "$assistantName is thinking..."
                    is VoiceEngineState.Speaking -> "$assistantName is speaking (Tap orb to interrupt)"
                    else -> "Ready. Speak freely or ask anything."
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = when (voiceState) {
                    is VoiceEngineState.Listening -> CyberCyan
                    is VoiceEngineState.Thinking -> SolarGold
                    is VoiceEngineState.Speaking -> CyberMagenta
                    else -> TextSecondary
                },
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Audio Waveform
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .width(160.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val heights = listOf(waveAnim1, waveAnim2, waveAnim3, waveAnim2, waveAnim1, waveAnim3, waveAnim2)
                heights.forEach { factor ->
                    val isActive = voiceState is VoiceEngineState.Listening || voiceState is VoiceEngineState.Speaking
                    val barH = if (isActive) (factor * 32).coerceAtLeast(4f) else 4f
                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(barH.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (voiceState is VoiceEngineState.Listening) CyberCyan else CyberMagenta
                            )
                    )
                }
            }
        }

        // Bottom Transcript & Controls Dock
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live speech transcript preview card
            FuturisticGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 70.dp, max = 120.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (voiceState is VoiceEngineState.Speaking && liveAiResponse.isNotBlank()) {
                            "$assistantName: \"$liveAiResponse\""
                        } else if (liveTranscript.isNotBlank()) {
                            "$userNickname: \"$liveTranscript\""
                        } else {
                            "Say 'Hello $assistantName', ask a question, or talk about anything..."
                        },
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 18.sp,
                        maxLines = 4
                    )
                }
            }

            // Controls Dock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute button
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color(0xFFEF4444) else CosmicSurfaceVariant)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("live_mute_button")
                ) {
                    Icon(
                        if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = Color.White
                    )
                }

                // Interrupt button
                IconButton(
                    onClick = { viewModel.interruptVoice() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CosmicSurfaceVariant)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("live_interrupt_button")
                ) {
                    Icon(
                        Icons.Default.Pause,
                        contentDescription = "Interrupt",
                        tint = CyberCyan
                    )
                }

                // End Call button
                IconButton(
                    onClick = {
                        viewModel.stopLiveVoiceSession()
                        onEndCall()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626))
                        .testTag("live_end_call_button")
                ) {
                    Icon(
                        Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
