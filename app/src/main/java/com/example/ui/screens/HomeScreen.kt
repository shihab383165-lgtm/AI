package com.example.ui.screens

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.VoiceEngineState
import com.example.ui.components.AnimatedAiOrb
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@Composable
fun HomeScreen(
    viewModel: AssistantViewModel,
    onNavigate: (String) -> Unit
) {
    val assistantName by viewModel.assistantName.collectAsState()
    val userNickname by viewModel.userNickname.collectAsState()
    val personalityMode by viewModel.personalityMode.collectAsState()
    val voiceState by viewModel.voiceManager.engineState.collectAsState()
    val rmsDb by viewModel.voiceManager.rmsAudioDb.collectAsState()
    val conversations by viewModel.conversations.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hi, $userNickname ❤️",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "I'm $assistantName. How can I help you?",
                        fontSize = 14.sp,
                        color = NeonViolet,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Status Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CosmicSurfaceVariant)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (voiceState is VoiceEngineState.Idle) EmeraldGreen else CyberMagenta)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (voiceState) {
                            is VoiceEngineState.Listening -> "LISTENING"
                            is VoiceEngineState.Thinking -> "THINKING"
                            is VoiceEngineState.Speaking -> "SPEAKING"
                            else -> "ONLINE"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Hero AI Orb Card
        item {
            FuturisticGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderGlow = true
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedAiOrb(
                        state = voiceState,
                        rmsDb = rmsDb,
                        size = 160.dp,
                        assistantName = assistantName,
                        onClick = { onNavigate("live") }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "$assistantName AI Core",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "$personalityMode • Tap Orb for Live Voice",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate("chat") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_chat_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Chat")
                        }

                        Button(
                            onClick = { onNavigate("live") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_live_voice_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Live Voice")
                        }
                    }
                }
            }
        }

        // Quick Conversational Starters
        item {
            Text(
                text = "Quick Sparks with $assistantName",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PromptChip(
                    text = "Motivate me ❤️",
                    onClick = {
                        viewModel.sendMessage("Motivate me and give me some caring affirmations today!")
                        onNavigate("chat")
                    }
                )
                PromptChip(
                    text = "Code an App",
                    onClick = {
                        onNavigate("code")
                    }
                )
                PromptChip(
                    text = "Search Web",
                    onClick = {
                        onNavigate("search")
                    }
                )
            }
        }

        // Jarvis Hub Actions Grid
        item {
            Text(
                text = "Jarvis Intelligence Hub",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "Coding Agent",
                        subtitle = "Sandboxed AI developer",
                        icon = Icons.Default.Code,
                        color = CyberCyan,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("code") }
                    )
                    ActionCard(
                        title = "Apps & Actions",
                        subtitle = "Native Android control",
                        icon = Icons.Default.Apps,
                        color = CyberMagenta,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("apps") }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionCard(
                        title = "Memory Store",
                        subtitle = "Saved user preferences",
                        icon = Icons.Default.Psychology,
                        color = NeonViolet,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("memory") }
                    )
                    ActionCard(
                        title = "Tasks & Plan",
                        subtitle = "Jarvis reminders",
                        icon = Icons.Default.CheckCircle,
                        color = EmeraldGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("tasks") }
                    )
                }
            }
        }

        // Recent Chats
        if (conversations.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Conversations",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { onNavigate("chat") }) {
                        Text("View All", color = NeonViolet, fontSize = 13.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    conversations.take(3).forEach { conv ->
                        FuturisticGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectConversation(conv.id)
                                    onNavigate("chat")
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(CosmicSurfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.ChatBubbleOutline,
                                        contentDescription = null,
                                        tint = NeonViolet,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = conv.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "Model: ${conv.modelUsed}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PromptChip(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = CosmicSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FuturisticGlassCard(
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
        }
    }
}
