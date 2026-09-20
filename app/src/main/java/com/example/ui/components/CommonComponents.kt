package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.Collaborator
import com.example.model.SubjectItem
import com.example.model.SyncStatus
import com.example.model.TaskPriority
import com.example.ui.theme.AiSparklePink
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.SubjectBio
import com.example.ui.theme.SubjectBioBg
import com.example.ui.theme.SubjectCS
import com.example.ui.theme.SubjectCSBg
import com.example.ui.theme.SubjectGeneral
import com.example.ui.theme.SubjectGeneralBg
import com.example.ui.theme.SubjectLit
import com.example.ui.theme.SubjectLitBg
import com.example.ui.theme.SubjectMath
import com.example.ui.theme.SubjectMathBg
import com.example.ui.theme.SubjectPhysics
import com.example.ui.theme.SubjectPhysicsBg
import com.example.ui.theme.SyncBlue
import com.example.ui.theme.UrgentRed

fun getCoverGradient(index: Int): Brush {
    return when (index % 5) {
        0 -> Brush.horizontalGradient(listOf(Color(0xFF4355B9), Color(0xFF8E96FF), Color(0xFFC1C5FF)))
        1 -> Brush.horizontalGradient(listOf(Color(0xFF006E2C), Color(0xFF10B981), Color(0xFF6EE7B7)))
        2 -> Brush.horizontalGradient(listOf(Color(0xFFC05621), Color(0xFFF59E0B), Color(0xFFFDE68A)))
        3 -> Brush.horizontalGradient(listOf(Color(0xFF7C3AED), Color(0xFFA855F7), Color(0xFFF472B6)))
        else -> Brush.horizontalGradient(listOf(Color(0xFF32408F), Color(0xFF4355B9), Color(0xFF6366F1)))
    }
}

fun getSubjectColorByIndex(index: Int): Pair<Color, Color> {
    return when (index % 8) {
        0 -> Pair(Color(0xFF4355B9), Color(0xFFE0E1F9)) // Indigo
        1 -> Pair(Color(0xFF006E2C), Color(0xFFE0F9E8)) // Emerald
        2 -> Pair(Color(0xFFD97706), Color(0xFFFEF3C7)) // Amber
        3 -> Pair(Color(0xFFBA1A1A), Color(0xFFFFE0E0)) // Crimson/Red
        4 -> Pair(Color(0xFF7C3AED), Color(0xFFEDE9FE)) // Purple
        5 -> Pair(Color(0xFF0284C7), Color(0xFFE0F2FE)) // Ocean Sky
        6 -> Pair(Color(0xFF0D9488), Color(0xFFCCFBF1)) // Teal
        else -> Pair(Color(0xFFC05621), Color(0xFFFFF0DF)) // Terracotta
    }
}

fun getSubjectColors(subject: String): Pair<Color, Color> {
    return when {
        subject.contains("CS", ignoreCase = true) || subject.contains("Comp", ignoreCase = true) || subject.contains("Code", ignoreCase = true) -> getSubjectColorByIndex(0)
        subject.contains("Bio", ignoreCase = true) || subject.contains("Chem", ignoreCase = true) -> getSubjectColorByIndex(1)
        subject.contains("Phys", ignoreCase = true) || subject.contains("Elect", ignoreCase = true) -> getSubjectColorByIndex(2)
        subject.contains("Math", ignoreCase = true) || subject.contains("Calc", ignoreCase = true) || subject.contains("Stats", ignoreCase = true) -> getSubjectColorByIndex(3)
        subject.contains("Lit", ignoreCase = true) || subject.contains("Eng", ignoreCase = true) || subject.contains("Hist", ignoreCase = true) -> getSubjectColorByIndex(4)
        subject.contains("Econ", ignoreCase = true) || subject.contains("Bus", ignoreCase = true) -> getSubjectColorByIndex(5)
        subject.contains("Psych", ignoreCase = true) || subject.contains("Art", ignoreCase = true) -> getSubjectColorByIndex(6)
        else -> {
            val hash = Math.abs(subject.hashCode())
            getSubjectColorByIndex(hash)
        }
    }
}

@Composable
fun SubjectBadge(
    subject: String,
    modifier: Modifier = Modifier
) {
    val (textColor, bgColor) = getSubjectColors(subject)
    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = subject,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PriorityBadge(
    priority: TaskPriority,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (priority) {
        TaskPriority.URGENT -> Pair(UrgentRed, "Urgent")
        TaskPriority.HIGH -> Pair(Color(0xFFEA580C), "High")
        TaskPriority.MEDIUM -> Pair(SyncBlue, "Medium")
        TaskPriority.LOW -> Pair(Color(0xFF64748B), "Low")
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SyncStatusPill(
    isOnline: Boolean,
    isSyncing: Boolean,
    pendingChangesCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sync_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Surface(
        color = if (isOnline) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isOnline) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f) else MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("sync_status_pill")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            if (isSyncing) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Syncing",
                    tint = SyncBlue,
                    modifier = Modifier
                        .size(14.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Syncing...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = SyncBlue
                )
            } else if (!isOnline) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (pendingChangesCount > 0) "Offline ($pendingChangesCount queued)" else "Offline Mode",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFEF4444)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CollabGreen, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (pendingChangesCount > 0) "$pendingChangesCount pending" else "Synced",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CollaboratorsAvatarRow(
    collaborators: List<Collaborator>,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy((-6).dp),
        modifier = modifier
    ) {
        collaborators.take(3).forEach { collab ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(26.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.5.dp, if (collab.isOnline) CollabGreen else Color(0xFF94A3B8), CircleShape)
            ) {
                Text(
                    text = collab.avatarEmoji,
                    fontSize = 13.sp
                )
            }
        }
        if (collaborators.size > 3) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(24.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            ) {
                Text(
                    text = "+${collaborators.size - 3}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ManageSubjectsDialog(
    subjects: List<SubjectItem>,
    onAddSubject: (name: String, emoji: String, colorIndex: Int) -> Unit,
    onDeleteSubject: (SubjectItem) -> Unit,
    onDismiss: () -> Unit
) {
    var newSubjectName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("📚") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    val emojis = listOf("📚", "💻", "🔬", "⚡", "📐", "📖", "🧪", "🎨", "💼", "🧠", "🌍", "🎵")
    val colorIndices = (0..7).toList()
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
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(scrollState)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Manage Subjects",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your Subjects & Courses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject Items List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.forEach { subject ->
                            val (textColor, bgColor) = getSubjectColorByIndex(subject.colorIndex)
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = bgColor,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(subject.emoji, fontSize = 16.sp)
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = subject.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                            if (subject.isDefault) {
                                                Text(
                                                    text = "Default",
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }

                                    if (!subject.isDefault || subjects.size > 1) {
                                        IconButton(
                                            onClick = { onDeleteSubject(subject) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Subject",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Add New Subject Section
                    Text(
                        text = "Add New Subject",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newSubjectName,
                        onValueChange = { newSubjectName = it },
                        label = { Text("Subject / Course Name") },
                        placeholder = { Text("e.g. Organic Chemistry, Economics") },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Emoji Picker
                    Text(text = "Choose Icon:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(emojis) { emoji ->
                            val isSelected = selectedEmoji == emoji
                            Surface(
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedEmoji = emoji }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Swatches
                    Text(text = "Choose Color Tag:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colorIndices) { idx ->
                            val (pColor, _) = getSubjectColorByIndex(idx)
                            val isSelected = selectedColorIndex == idx
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(pColor, CircleShape)
                                    .clickable { selectedColorIndex = idx }
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (newSubjectName.isNotBlank()) {
                                onAddSubject(newSubjectName, selectedEmoji, selectedColorIndex)
                                newSubjectName = ""
                            }
                        },
                        enabled = newSubjectName.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add Subject", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
