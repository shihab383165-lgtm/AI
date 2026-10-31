package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: AssistantViewModel = viewModel()
            val accentIndex by viewModel.accentColorIndex.collectAsState()
            val userFeedback by viewModel.userFeedback.collectAsState()
            val assistantName by viewModel.assistantName.collectAsState()

            val accentColors = listOf(
                NeonViolet,
                CyberMagenta,
                CyberCyan,
                EmeraldGreen,
                SolarGold
            )
            val currentAccent = accentColors.getOrElse(accentIndex) { NeonViolet }

            var currentScreen by remember { mutableStateOf("home") }

            // Handle system back navigation
            BackHandler(enabled = currentScreen != "home") {
                currentScreen = "home"
            }

            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(userFeedback) {
                userFeedback?.let {
                    snackbarHostState.showSnackbar(it)
                    viewModel.clearFeedback()
                }
            }

            MyApplicationTheme(accentColor = currentAccent) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = CosmicBackground,
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            Snackbar(
                                snackbarData = data,
                                containerColor = CosmicSurfaceVariant,
                                contentColor = TextPrimary,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    },
                    bottomBar = {
                        // Show bottom nav on top-level screens
                        val showBottomNav = currentScreen in listOf("home", "chat", "code", "settings")
                        AnimatedVisibility(
                            visible = showBottomNav,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            CyberBottomNav(
                                currentScreen = currentScreen,
                                onScreenSelect = { currentScreen = it },
                                accentColor = currentAccent
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            "home" -> HomeScreen(
                                viewModel = viewModel,
                                onNavigate = { currentScreen = it }
                            )
                            "chat" -> ChatScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                            "live" -> LiveVoiceScreen(
                                viewModel = viewModel,
                                onEndCall = { currentScreen = "home" }
                            )
                            "code" -> CodingAgentScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                            "apps" -> AppsActionsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                            "memory" -> MemoryScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                            "tasks" -> TasksScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                            "search" -> WebSearchScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" },
                                onAskInChat = { prompt ->
                                    viewModel.sendMessage(prompt)
                                    currentScreen = "chat"
                                }
                            )
                            "settings" -> SettingsScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "home" }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberBottomNav(
    currentScreen: String,
    onScreenSelect: (String) -> Unit,
    accentColor: Color
) {
    Surface(
        color = CosmicSurface.copy(alpha = 0.95f),
        tonalElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavTabItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == "home",
                accentColor = accentColor,
                onClick = { onScreenSelect("home") }
            )
            NavTabItem(
                icon = Icons.Default.Chat,
                label = "Chat",
                isSelected = currentScreen == "chat",
                accentColor = accentColor,
                onClick = { onScreenSelect("chat") }
            )
            // Live Voice Center Highlight Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CyberMagenta)
                    .clickable { onScreenSelect("live") }
                    .testTag("nav_live_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Live Voice",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            NavTabItem(
                icon = Icons.Default.Code,
                label = "Code",
                isSelected = currentScreen == "code",
                accentColor = accentColor,
                onClick = { onScreenSelect("code") }
            )
            NavTabItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentScreen == "settings",
                accentColor = accentColor,
                onClick = { onScreenSelect("settings") }
            )
        }
    }
}

@Composable
fun NavTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (isSelected) accentColor else TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) accentColor else TextSecondary,
            fontSize = 10.sp
        )
    }
}
