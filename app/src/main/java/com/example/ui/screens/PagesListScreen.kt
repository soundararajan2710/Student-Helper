package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Page
import com.example.model.SubjectItem
import com.example.ui.components.ManageSubjectsDialog
import com.example.ui.components.SubjectBadge
import com.example.ui.components.getCoverGradient
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.NotionBlack
import com.example.viewmodel.WorkspaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagesListScreen(
    viewModel: WorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val pages by viewModel.pages.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubjectFilter.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var showManageSubjectsDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .testTag("pages_list_column")
        ) {
            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search student notes, tags, formulas...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_pages_input")
                )
            }

            // Subject Filter Chips with Manage action
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        val isAllSelected = selectedSubject == "All"
                        FilterChip(
                            selected = isAllSelected,
                            onClick = { viewModel.selectedSubjectFilter.value = "All" },
                            label = { Text("All Subjects", fontSize = 12.sp, fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("subject_filter_All")
                        )
                    }
                    items(subjects) { subj ->
                        val isSelected = selectedSubject.equals(subj.name, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedSubjectFilter.value = subj.name },
                            label = { Text("${subj.emoji} ${subj.name}", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("subject_filter_${subj.name}")
                        )
                    }
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clickable { showManageSubjectsDialog = true }
                                .padding(start = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = "Manage Subjects", modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit Subjects", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Quick Student Templates Launcher
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "⚡ QUICK STUDENT TEMPLATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            letterSpacing = 0.6.sp
                        )
                    }
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            TemplateCard(
                                emoji = "📝",
                                title = "Lecture Notes",
                                subtitle = "Derivations & Homework",
                                colorIndex = 0,
                                onClick = {
                                    val defSubject = if (subjects.isNotEmpty()) subjects[0].name else "General"
                                    viewModel.createNewPage("New Lecture Notes", "📝", defSubject, 0, "LECTURE")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                emoji = "🎯",
                                title = "Exam Prep",
                                subtitle = "Flashcards & Checklist",
                                colorIndex = 1,
                                onClick = {
                                    val defSubject = if (subjects.isNotEmpty()) subjects[0].name else "General"
                                    viewModel.createNewPage("Exam Study Guide", "🎯", defSubject, 1, "EXAM_PREP")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                emoji = "🧠",
                                title = "Brain Dump",
                                subtitle = "Ideas & Quick Links",
                                colorIndex = 2,
                                onClick = {
                                    val defSubject = if (subjects.isNotEmpty()) subjects[0].name else "General"
                                    viewModel.createNewPage("Quick Brain Dump", "🧠", defSubject, 2, "BRAIN_DUMP")
                                }
                            )
                        }
                        item {
                            TemplateCard(
                                emoji = "✨",
                                title = "AI Assisted",
                                subtitle = "Blank Study Workspace",
                                colorIndex = 3,
                                onClick = {
                                    showCreateDialog = true
                                }
                            )
                        }
                    }
                }
            }

            // Student Notes Section Header
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = "📚 WORKSPACE PAGES (${pages.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        letterSpacing = 0.6.sp
                    )
                }
            }

            // Notes List
            if (pages.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(text = "📑", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (selectedSubject != "All") "No notes in $selectedSubject" else "No student notes found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Create your first lecture note, study sheet, or choose a template above.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(pages, key = { it.id }) { page ->
                    PageRowItem(
                        page = page,
                        onClick = { viewModel.selectPage(page.id) },
                        onToggleFavorite = { viewModel.togglePageFavorite(page) }
                    )
                }
            }
        }

        // Floating Action Button to create Note
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("create_page_fab")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Page")
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Page", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        // Create Page Dialog
        if (showCreateDialog) {
            CreatePageDialog(
                subjects = subjects,
                onDismiss = { showCreateDialog = false },
                onCreate = { title, emoji, subject, coverIndex, template ->
                    viewModel.createNewPage(title, emoji, subject, coverIndex, template)
                    showCreateDialog = false
                }
            )
        }

        // Manage Subjects Dialog
        if (showManageSubjectsDialog) {
            ManageSubjectsDialog(
                subjects = subjects,
                onAddSubject = { name, emoji, colorIdx ->
                    viewModel.createSubject(name, emoji, colorIdx)
                },
                onDeleteSubject = { subject ->
                    viewModel.deleteSubject(subject)
                },
                onDismiss = { showManageSubjectsDialog = false }
            )
        }
    }
}

@Composable
fun TemplateCard(
    emoji: String,
    title: String,
    subtitle: String,
    colorIndex: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick() }
            .testTag("template_card_$title")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(getCoverGradient(colorIndex))
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun PageRowItem(
    page: Page,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(page.updatedAt))

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("page_row_${page.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(getCoverGradient(page.coverColorIndex))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        Text(text = page.emoji, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = page.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SubjectBadge(subject = page.subject)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = formattedDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            if (page.isCollaborative) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Groups,
                                        contentDescription = "Collaborative",
                                        tint = CollabGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Shared",
                                        fontSize = 11.sp,
                                        color = CollabGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (page.isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (page.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreatePageDialog(
    subjects: List<SubjectItem>,
    onDismiss: () -> Unit,
    onCreate: (title: String, emoji: String, subject: String, coverIndex: Int, template: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📝") }
    var selectedSubject by remember {
        mutableStateOf(if (subjects.isNotEmpty()) subjects[0].name else "General")
    }
    var selectedCoverIndex by remember { mutableIntStateOf(0) }
    var selectedTemplate by remember { mutableStateOf<String?>("LECTURE") }

    val emojis = listOf("📝", "💻", "🔬", "📐", "📚", "⚡", "🎯", "🧠", "💡")
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_page_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "Create Student Page",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title") },
                        placeholder = { Text("e.g. CS 101 Lecture 5, Physics Notes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_page_title_input"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Emoji Picker
                    Text(text = "Choose Icon:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedEmoji == emoji) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Text(text = emoji, fontSize = 18.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject Selector
                    Text(text = "Subject / Course:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (subjects.isEmpty()) {
                            item {
                                FilterChip(
                                    selected = true,
                                    onClick = { },
                                    label = { Text("General", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        } else {
                            items(subjects) { subj ->
                                FilterChip(
                                    selected = selectedSubject.equals(subj.name, ignoreCase = true),
                                    onClick = { selectedSubject = subj.name },
                                    label = { Text("${subj.emoji} ${subj.name}", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.textButtonColors(),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val finalTitle = title.ifBlank { "Untitled Note" }
                                onCreate(finalTitle, selectedEmoji, selectedSubject, selectedCoverIndex, selectedTemplate)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("submit_create_page_btn")
                        ) {
                            Text("Create Page", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
