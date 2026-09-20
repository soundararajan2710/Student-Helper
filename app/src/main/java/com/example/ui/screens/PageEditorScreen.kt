package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.BlockType
import com.example.ui.components.BlockItemView
import com.example.ui.components.CollaboratorsAvatarRow
import com.example.ui.components.SubjectBadge
import com.example.ui.components.getCoverGradient
import com.example.ui.theme.AiSparklePink
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.NotionBlack
import com.example.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.layout.imePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageEditorScreen(
    viewModel: WorkspaceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val page by viewModel.currentPage.collectAsStateWithLifecycle()
    val blocks by viewModel.currentBlocks.collectAsStateWithLifecycle()
    val comments by viewModel.currentComments.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    var showCommentsSheet by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addMediaBlock(BlockType.IMAGE, it, "Image_${System.currentTimeMillis()}.jpg")
        }
    }

    // PDF Picker Launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addMediaBlock(BlockType.ATTACHMENT_PDF, it, "Document_${System.currentTimeMillis()}.pdf")
        }
    }

    val flashcardBlocks = blocks.filter { it.type == BlockType.FLASHCARD }

    if (page == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val currentPage = page!!

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentPage.title.ifBlank { "Untitled Note" },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("editor_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Flashcard Study Button
                    if (flashcardBlocks.isNotEmpty()) {
                        Surface(
                            color = AiSparklePurple.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .clickable {
                                    val cards = flashcardBlocks.map { Pair(it.content, it.extra) }
                                    viewModel.startFlashcardStudy(currentPage.title, cards)
                                }
                                .testTag("start_flashcards_study_btn")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Study Flashcards",
                                    tint = AiSparklePurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Study (${flashcardBlocks.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AiSparklePurple
                                )
                            }
                        }
                    }

                    // Comments button
                    IconButton(
                        onClick = { showCommentsSheet = true },
                        modifier = Modifier.testTag("page_comments_btn")
                    ) {
                        Box {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Comments")
                            if (comments.isNotEmpty()) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(14.dp)
                                        .background(CollabGreen, CircleShape)
                                ) {
                                    Text(
                                        text = "${comments.size}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Favorite Star
                    IconButton(
                        onClick = { viewModel.togglePageFavorite(currentPage) },
                        modifier = Modifier.testTag("page_favorite_btn")
                    ) {
                        Icon(
                            imageVector = if (currentPage.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                            contentDescription = "Favorite",
                            tint = if (currentPage.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    // Options Menu
                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Attach Image/Pic") },
                                onClick = {
                                    showMoreMenu = false
                                    imagePickerLauncher.launch("image/*")
                                },
                                leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Attach PDF Document") },
                                onClick = {
                                    showMoreMenu = false
                                    pdfPickerLauncher.launch("application/pdf")
                                },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Page", color = Color(0xFFEF4444)) },
                                onClick = {
                                    viewModel.deleteCurrentPage()
                                    showMoreMenu = false
                                    onBack()
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("page_editor_blocks_list")
            ) {
                // Cover Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .background(getCoverGradient(currentPage.coverColorIndex))
                    )
                }

                // Header metadata area
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 10.dp, bottom = 14.dp)
                    ) {
                        // Emoji icon & Collaborators row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .clickable { showEmojiPicker = !showEmojiPicker }
                                    .testTag("page_emoji_icon")
                            ) {
                                Text(text = currentPage.emoji, fontSize = 30.sp)
                            }

                            if (syncState.activeCollaborators.isNotEmpty()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Co-authoring:",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    CollaboratorsAvatarRow(collaborators = syncState.activeCollaborators)
                                }
                            }
                        }

                        // Emoji picker dropdown
                        AnimatedVisibility(visible = showEmojiPicker) {
                            val emojiList = listOf("📝", "💻", "🔬", "📐", "📚", "⚡", "🎯", "🧠", "💡", "🧪", "🚀", "🎨")
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                items(emojiList) { emoji ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            .clickable {
                                                viewModel.updatePageEmoji(emoji)
                                                showEmojiPicker = false
                                            }
                                    ) {
                                        Text(text = emoji, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Title Text Field
                        BasicTextField(
                            value = currentPage.title,
                            onValueChange = { viewModel.updatePageTitle(it) },
                            textStyle = TextStyle(
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 32.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("editor_title_input"),
                            decorationBox = { innerTextField ->
                                if (currentPage.title.isEmpty()) {
                                    Text("Untitled Page", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                                innerTextField()
                            }
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Subject dropdown picker & Sync badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box {
                                Surface(
                                    modifier = Modifier.clickable { showSubjectDropdown = true }
                                ) {
                                    SubjectBadge(subject = currentPage.subject)
                                }

                                DropdownMenu(
                                    expanded = showSubjectDropdown,
                                    onDismissRequest = { showSubjectDropdown = false }
                                ) {
                                    subjects.forEach { subj ->
                                        DropdownMenuItem(
                                            text = { Text("${subj.emoji} ${subj.name}") },
                                            onClick = {
                                                viewModel.updatePageSubject(subj.name)
                                                showSubjectDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Tap subject to change",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Magic Toolbar Card
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AiSparklePurple.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_magic_bar")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        tint = AiSparklePurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "GEMINI 3.5 FLASH COPILOT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AiSparklePurple,
                                        letterSpacing = 0.5.sp
                                    )
                                    if (aiState.isGenerating) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            strokeWidth = 2.dp,
                                            color = AiSparklePurple
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = aiState.activeAction ?: "Generating...",
                                            fontSize = 11.sp,
                                            color = AiSparklePurple
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        AiActionChip(
                                            icon = "✨",
                                            label = "Summarize Note",
                                            onClick = { viewModel.summarizeCurrentPage() },
                                            enabled = !aiState.isGenerating
                                        )
                                    }
                                    item {
                                        AiActionChip(
                                            icon = "📝",
                                            label = "Practice Quiz",
                                            onClick = { viewModel.generateQuizForCurrentPage() },
                                            enabled = !aiState.isGenerating
                                        )
                                    }
                                    item {
                                        AiActionChip(
                                            icon = "🗂️",
                                            label = "Generate 4 Flashcards",
                                            onClick = { viewModel.generateFlashcardsForCurrentPage() },
                                            enabled = !aiState.isGenerating
                                        )
                                    }
                                    item {
                                        AiActionChip(
                                            icon = "📋",
                                            label = "Extract Tasks & Deadlines",
                                            onClick = { viewModel.extractTasksFromCurrentPage() },
                                            enabled = !aiState.isGenerating
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Blocks List
                items(blocks, key = { it.id }) { block ->
                    BlockItemView(
                        block = block,
                        onContentChange = { newContent -> viewModel.updateBlockContent(block, newContent) },
                        onExtraChange = { newExtra -> viewModel.updateBlockExtra(block, newExtra) },
                        onToggleChecked = { viewModel.toggleBlockChecked(block) },
                        onDelete = { viewModel.deleteBlock(block.id) },
                        onTypeChange = { newType ->
                            viewModel.updateBlockContent(block.copy(type = newType), block.content)
                        },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }

                // Add Block prompt area at bottom of note
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                            .clickable { viewModel.addBlock(BlockType.PARAGRAPH) }
                            .testTag("add_paragraph_click_area")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add block",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to add text or pick a tool below...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Floating Bottom Block Palette Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .imePadding()
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                    .testTag("floating_block_palette")
            ) {
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    item {
                        BlockToolButton(
                            icon = Icons.Default.TextFields,
                            label = "Text",
                            onClick = { viewModel.addBlock(BlockType.PARAGRAPH) }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.Title,
                            label = "Heading",
                            onClick = { viewModel.addBlock(BlockType.HEADING_1) }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.CheckCircle,
                            label = "To-Do",
                            onClick = { viewModel.addBlock(BlockType.TODO) }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.FormatListBulleted,
                            label = "Bullet",
                            onClick = { viewModel.addBlock(BlockType.BULLET) }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.AddPhotoAlternate,
                            label = "+ Image",
                            onClick = { imagePickerLauncher.launch("image/*") }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.PictureAsPdf,
                            label = "+ PDF",
                            onClick = { pdfPickerLauncher.launch("application/pdf") }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.School,
                            label = "Flashcard",
                            onClick = { viewModel.addBlock(BlockType.FLASHCARD, extra = "Answer") }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.Info,
                            label = "Callout",
                            onClick = { viewModel.addBlock(BlockType.CALLOUT, extra = "💡") }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.Code,
                            label = "Code",
                            onClick = { viewModel.addBlock(BlockType.CODE_SNIPPET) }
                        )
                    }
                    item {
                        BlockToolButton(
                            icon = Icons.Default.FormatQuote,
                            label = "Quote",
                            onClick = { viewModel.addBlock(BlockType.QUOTE) }
                        )
                    }
                }
            }
        }

        // Collaborative Comments Bottom Sheet
        if (showCommentsSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showCommentsSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "💬 Peer Collaboration & Comments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Discuss notes and study together in real-time.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (comments.isEmpty()) {
                        Text(
                            text = "No comments yet. Start a discussion with your study group!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            items(comments, key = { it.id }) { comment ->
                                val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                val timeStr = dateFormat.format(Date(comment.timestamp))

                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = comment.authorAvatar, fontSize = 16.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = comment.authorName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onBackground
                                                )
                                            }
                                            Text(
                                                text = timeStr,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = comment.text,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Write a comment or study note...", fontSize = 13.sp) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input_field")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    viewModel.addCommentToCurrentPage(newCommentText)
                                    newCommentText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .testTag("send_comment_btn")
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
            }
        }
    }
}

@Composable
fun AiActionChip(
    icon: String,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Surface(
        color = AiSparklePurple.copy(alpha = 0.08f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AiSparklePurple.copy(alpha = 0.25f)),
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .testTag("ai_chip_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) AiSparklePurple else Color.Gray
            )
        }
    }
}

@Composable
fun BlockToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .testTag("tool_btn_$label")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
        }
    }
}
