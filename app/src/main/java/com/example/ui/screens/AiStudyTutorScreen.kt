package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.AiSparklePink
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.NotionBlack
import com.example.viewmodel.WorkspaceViewModel

import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.window.DialogProperties

@Composable
fun AiStudyTutorScreen(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val apiKey by viewModel.geminiApiKey.collectAsStateWithLifecycle()
    val isGeminiActive by viewModel.isGeminiApiActive.collectAsStateWithLifecycle()

    var inputPrompt by remember { mutableStateOf("") }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    viewModel.analyzeCurrentPageImage(bitmap)
                    Toast.makeText(context, "Analyzing study diagram with Gemini AI...", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Could not load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val samplePrompts = listOf(
        "📅 Generate Weekly Study Plan",
        "🧠 Explain Dijkstra's Algorithm using an intuitive analogy",
        "🎯 Generate 3 practice exam questions with solutions",
        "🔬 Step-by-step problem solver for physics or calculus",
        "⚡ How to manage study time using the Pomodoro technique?"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 70.dp)
            .imePadding()
            .testTag("ai_tutor_screen")
    ) {
        // Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .background(AiSparklePurple.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = AiSparklePurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini 3.5 Flash",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = if (isGeminiActive) CollabGreen.copy(alpha = 0.15f) else AiSparklePurple.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (isGeminiActive) "🟢 Live" else "⚡ Built-in",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isGeminiActive) CollabGreen else AiSparklePurple,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (currentPage != null) "Active note context: ${currentPage?.title}" else "AI Study Copilot • Notes, exams & timetable",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.clearAiChatHistory() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear Chat",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = { showApiKeyDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (apiKey.isNotBlank()) CollabGreen.copy(alpha = 0.15f) else AiSparklePurple.copy(alpha = 0.12f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "Gemini API Key",
                                tint = if (apiKey.isNotBlank()) CollabGreen else AiSparklePurple,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Optional Key Hint Banner if not set
        if (apiKey.isBlank()) {
            Surface(
                color = AiSparklePurple.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showApiKeyDialog = true }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(text = "✨", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Powered by Google Gemini 3.5 Flash • Tap to enter custom API key",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AiSparklePurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick prompts row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(samplePrompts) { prompt ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    modifier = Modifier.clickable {
                        if (prompt.contains("Weekly Study Plan")) {
                            viewModel.generateWeeklyStudyPlan()
                        } else {
                            viewModel.askAiStudyBuddy(prompt)
                        }
                    }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat History List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("ai_chat_history_list")
        ) {
            items(aiState.chatHistory) { (sender, message) ->
                val isAssistant = sender == "assistant"
                Row(
                    horizontalArrangement = if (isAssistant) Arrangement.Start else Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAssistant) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(end = 8.dp, top = 4.dp)
                                .size(28.dp)
                                .background(AiSparklePurple.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Text(text = "✨", fontSize = 14.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAssistant) MaterialTheme.colorScheme.surface else NotionBlack
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = if (isAssistant) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)) else null,
                        modifier = Modifier.widthIn(max = 300.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = message,
                                fontSize = 14.sp,
                                color = if (isAssistant) MaterialTheme.colorScheme.onBackground else Color.White,
                                lineHeight = 20.sp
                            )

                            if (isAssistant) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("AI Note", message)
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Copied explanation to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (!isAssistant) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(start = 8.dp, top = 4.dp)
                                .size(28.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Text(text = "🧑‍🎓", fontSize = 14.sp)
                        }
                    }
                }
            }

            if (aiState.isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 36.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = AiSparklePurple
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Study Copilot is thinking...",
                            fontSize = 12.sp,
                            color = AiSparklePurple,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Input Field Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask study question, paste notes or formula...", fontSize = 13.sp) },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_prompt_input")
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(AiSparklePurple.copy(alpha = 0.12f), CircleShape)
                        .testTag("ai_photo_picker_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Analyze Study Diagram / Photo with Gemini",
                        tint = AiSparklePurple,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (inputPrompt.isNotBlank() && !aiState.isGenerating) {
                            viewModel.askAiStudyBuddy(inputPrompt)
                            inputPrompt = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(NotionBlack, CircleShape)
                        .testTag("send_ai_prompt_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // API Key Settings Dialog
        if (showApiKeyDialog) {
            GeminiApiKeyDialog(
                currentKey = apiKey,
                onDismiss = { showApiKeyDialog = false },
                onSave = { newKey ->
                    viewModel.saveGeminiApiKey(newKey)
                    showApiKeyDialog = false
                    Toast.makeText(context, if (newKey.isBlank()) "API Key cleared" else "Gemini API Key saved!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun GeminiApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var keyInput by remember { mutableStateOf(currentKey) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(AiSparklePurple.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VpnKey,
                                contentDescription = "Gemini Key",
                                tint = AiSparklePurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gemini AI API Key",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Enter your Google Gemini API key to enable live AI responses, lecture summaries, and flashcards on your phone. If left blank, Student Helper uses the fast built-in AI copilot!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Get a free key at ai.google.dev (Google AI Studio).",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AiSparklePurple
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        label = { Text("Gemini API Key") },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        if (currentKey.isNotBlank()) {
                            TextButton(onClick = { onSave("") }) {
                                Text("Clear", color = Color(0xFFEF4444))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSave(keyInput) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_api_key_btn")
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }
        }
    }
}
