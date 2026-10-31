package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ProjectFileEntity
import com.example.ui.components.FuturisticGlassCard
import com.example.ui.theme.*
import com.example.viewmodel.AssistantViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingAgentScreen(
    viewModel: AssistantViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val assistantName by viewModel.assistantName.collectAsState()
    val projectFiles by viewModel.projectFiles.collectAsState()
    val activeFile by viewModel.activeProjectFile.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val isCompiling by viewModel.isCompiling.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Editor, 1: AI Planner & Generator, 2: Terminal Logs
    var editorCodeText by remember { mutableStateOf("") }
    var aiPromptText by remember { mutableStateOf("") }
    var showNewFileDialog by remember { mutableStateOf(false) }
    var newFileNameInput by remember { mutableStateOf("") }

    // Update editor text when active file changes
    LaunchedEffect(activeFile?.id, activeFile?.content) {
        if (activeFile != null) {
            editorCodeText = activeFile!!.content
        } else if (projectFiles.isNotEmpty()) {
            viewModel.selectProjectFile(projectFiles.first())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Jarvis Coding Agent",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "AI Architect • Isolated Sandbox",
                            fontSize = 11.sp,
                            color = CyberCyan
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showNewFileDialog = true }) {
                        Icon(Icons.Default.NoteAdd, contentDescription = "New File", tint = CyberCyan)
                    }
                    IconButton(
                        onClick = {
                            viewModel.runSandboxExecution()
                            activeTab = 2 // Switch to logs
                        },
                        modifier = Modifier.testTag("run_sandbox_button")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run", tint = EmeraldGreen)
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
            // File tabs bar
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CosmicSurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(projectFiles) { file ->
                    val isSelected = activeFile?.id == file.id
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                if (activeFile != null && editorCodeText != activeFile!!.content) {
                                    viewModel.saveActiveFileContent(editorCodeText)
                                }
                                viewModel.selectProjectFile(file)
                            },
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) CosmicBackground else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, NeonViolet) else null
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Description,
                                contentDescription = null,
                                tint = if (isSelected) CyberCyan else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = file.fileName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // View Mode Tab Row: Editor | AI Architect | Terminal
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CosmicSurface,
                contentColor = NeonViolet
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Code Editor", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("AI Generator", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("Console Logs", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Tab Content
            when (activeTab) {
                0 -> {
                    // Code Editor
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeFile?.fileName ?: "No file selected",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                TextButton(
                                    onClick = {
                                        viewModel.saveActiveFileContent(editorCodeText)
                                    }
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonViolet)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save", color = NeonViolet, fontSize = 12.sp)
                                }
                                TextButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Source Code", editorCodeText))
                                        viewModel.showFeedback("Code copied to clipboard")
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = TextSecondary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", color = TextSecondary, fontSize = 12.sp)
                                }
                                if (activeFile != null) {
                                    IconButton(
                                        onClick = { viewModel.deleteProjectFile(activeFile!!.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Text Area for Code
                        OutlinedTextField(
                            value = editorCodeText,
                            onValueChange = { editorCodeText = it },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag("code_editor_field"),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFFE2E8F0)
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CodeBlockBg,
                                unfocusedContainerColor = CodeBlockBg,
                                focusedBorderColor = GlassBorderBright,
                                unfocusedBorderColor = GlassBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                1 -> {
                    // AI Generator & Planner
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "AI Coding Architect ($assistantName)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Text(
                            text = "Describe what module, Compose screen, or backend logic you need. $assistantName will architect, compile, and inject it into your active file.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        OutlinedTextField(
                            value = aiPromptText,
                            onValueChange = { aiPromptText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .testTag("ai_code_prompt_field"),
                            placeholder = {
                                Text("e.g. 'Create a Firebase login page with email validation' or 'Build a Room DAO for notes'...", color = TextMuted, fontSize = 13.sp)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = CosmicSurface,
                                unfocusedContainerColor = CosmicSurface,
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = GlassBorder
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val prompt = aiPromptText
                                    if (prompt.isNotBlank()) {
                                        viewModel.generateAiCode(prompt)
                                        activeTab = 2 // Switch to logs
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("generate_code_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isCompiling) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text("Generate & Inject", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Preset template buttons
                        Text(
                            text = "Quick Architect Presets",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.generateAiCode("Create a Firebase login and registration screen in Jetpack Compose")
                                    activeTab = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Firebase Auth", fontSize = 11.sp, color = TextPrimary)
                            }
                            Button(
                                onClick = {
                                    viewModel.generateAiCode("Create a Retrofit Weather API client with Moshi models")
                                    activeTab = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CosmicSurfaceVariant),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("REST API Client", fontSize = 11.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                2 -> {
                    // Terminal / Console Logs
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Execution Sandbox Terminal", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { viewModel.runSandboxExecution() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Execute", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            color = Color(0xFF06040F),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            val scrollState = rememberScrollState()
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .verticalScroll(scrollState)
                            ) {
                                Text(
                                    text = terminalLogs,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = if (terminalLogs.contains("[ERROR]")) Color(0xFFF87171) else Color(0xFF34D399),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // New File Dialog
    if (showNewFileDialog) {
        AlertDialog(
            onDismissRequest = { showNewFileDialog = false },
            title = { Text("Create New Source File", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newFileNameInput,
                    onValueChange = { newFileNameInput = it },
                    label = { Text("Filename (e.g. ProfileScreen.kt)") },
                    placeholder = { Text("MyScreen.kt") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFileNameInput.isNotBlank()) {
                            viewModel.createProjectFile(
                                fileName = newFileNameInput.trim(),
                                content = "// Created by $assistantName Coding Agent\npackage com.example\n\n"
                            )
                            newFileNameInput = ""
                            showNewFileDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFileDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = CosmicSurface
        )
    }
}
