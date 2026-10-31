package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.ActionType
import com.example.service.NativeAction
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsActionsScreen(
    viewModel: AssistantViewModel,
    onNavigateBack: () -> Unit
) {
    val assistantName by viewModel.assistantName.collectAsState()
    val actionManager = viewModel.actionManager
    val actions = actionManager.defaultSupportedActions

    var selectedActionForConfirm by remember { mutableStateOf<NativeAction?>(null) }
    var customUrlInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Apps & Native Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Android System Bridge", fontSize = 11.sp, color = CyberMagenta)
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation card
            FuturisticGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Native Android Bridge Active",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$assistantName can directly command native Android applications using device Intents and URL routing. Tap any action or launch custom links.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Custom URL / Deep Link launcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customUrlInput,
                    onValueChange = { customUrlInput = it },
                    placeholder = { Text("https://example.com or intent URL...", fontSize = 12.sp, color = TextMuted) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CosmicSurface,
                        unfocusedContainerColor = CosmicSurface,
                        focusedBorderColor = NeonViolet,
                        unfocusedBorderColor = GlassBorder
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            actionManager.openUrl(customUrlInput)
                            customUrlInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Launch")
                }
            }

            Text(
                text = "Supported Native Apps & Tools",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            // Grid of native actions
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(actions) { action ->
                    ActionGridItem(
                        action = action,
                        onClick = {
                            if (action.isSensitive) {
                                selectedActionForConfirm = action
                            } else {
                                val res = actionManager.executeAction(action)
                                if (res.isSuccess) {
                                    viewModel.showFeedback("Launched ${action.name}")
                                } else {
                                    viewModel.showFeedback("Error launching ${action.name}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    // Confirmation dialog for sensitive actions
    if (selectedActionForConfirm != null) {
        val action = selectedActionForConfirm!!
        AlertDialog(
            onDismissRequest = { selectedActionForConfirm = null },
            title = { Text("Confirm Action: ${action.name}", color = TextPrimary) },
            text = {
                Text(
                    text = "Are you sure you want $assistantName to execute '${action.description}'?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        actionManager.executeAction(action)
                        selectedActionForConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta)
                ) {
                    Text("Execute")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedActionForConfirm = null }) {
                    Text("Cancel")
                }
            },
            containerColor = CosmicSurface
        )
    }
}

@Composable
fun ActionGridItem(
    action: NativeAction,
    onClick: () -> Unit
) {
    val (icon, color) = when (action.id) {
        "youtube" -> Icons.Default.PlayCircle to Color(0xFFEF4444)
        "whatsapp" -> Icons.Default.Chat to Color(0xFF22C55E)
        "gmail" -> Icons.Default.Mail to Color(0xFFEA4335)
        "maps" -> Icons.Default.Place to Color(0xFF3B82F6)
        "telegram" -> Icons.Default.Send to Color(0xFF0EA5E9)
        "chrome" -> Icons.Default.Language to Color(0xFFF59E0B)
        "camera" -> Icons.Default.CameraAlt to Color(0xFFA855F7)
        "calculator" -> Icons.Default.Calculate to Color(0xFF10B981)
        "calendar" -> Icons.Default.CalendarMonth to Color(0xFFEC4899)
        else -> Icons.Default.Apps to NeonViolet
    }

    FuturisticGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("action_${action.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = action.name, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(action.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(action.description, color = TextSecondary, fontSize = 11.sp, maxLines = 2, lineHeight = 15.sp)
        }
    }
}
