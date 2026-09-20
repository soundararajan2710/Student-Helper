package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import com.example.model.Block
import com.example.model.BlockType
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.NotionBlack
import com.example.ui.theme.SyncBlue

@Composable
fun BlockItemView(
    block: Block,
    onContentChange: (String) -> Unit,
    onExtraChange: (String) -> Unit,
    onToggleChecked: () -> Unit,
    onDelete: () -> Unit,
    onTypeChange: (BlockType) -> Unit,
    onStartStudyCard: ((Pair<String, String>) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isFlashcardFlipped by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("block_item_${block.id}")
    ) {
        // Main block body based on type
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        ) {
            when (block.type) {
                BlockType.HEADING_1 -> {
                    BasicTextField(
                        value = block.content,
                        onValueChange = onContentChange,
                        textStyle = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 28.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("block_h1_${block.id}"),
                        decorationBox = { innerTextField ->
                            if (block.content.isEmpty()) {
                                Text(
                                    text = "Heading 1",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                BlockType.HEADING_2 -> {
                    BasicTextField(
                        value = block.content,
                        onValueChange = onContentChange,
                        textStyle = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 24.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .testTag("block_h2_${block.id}"),
                        decorationBox = { innerTextField ->
                            if (block.content.isEmpty()) {
                                Text(
                                    text = "Heading 2",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                BlockType.PARAGRAPH -> {
                    BasicTextField(
                        value = block.content,
                        onValueChange = onContentChange,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 22.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .testTag("block_paragraph_${block.id}"),
                        decorationBox = { innerTextField ->
                            if (block.content.isEmpty()) {
                                Text(
                                    text = "Type something or tap AI to generate...",
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                BlockType.TODO -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = block.isChecked,
                            onCheckedChange = { onToggleChecked() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = NotionBlack,
                                checkmarkColor = Color.White
                            ),
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("block_todo_check_${block.id}")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = block.content,
                            onValueChange = onContentChange,
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = if (block.isChecked) FontWeight.Normal else FontWeight.Medium,
                                textDecoration = if (block.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                                color = if (block.isChecked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onBackground,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("block_todo_text_${block.id}"),
                            decorationBox = { innerTextField ->
                                if (block.content.isEmpty()) {
                                    Text(
                                        text = "To-do task...",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                BlockType.BULLET -> {
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp, start = 4.dp, end = 10.dp)
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.onBackground, CircleShape)
                        )
                        BasicTextField(
                            value = block.content,
                            onValueChange = onContentChange,
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onBackground,
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("block_bullet_${block.id}"),
                            decorationBox = { innerTextField ->
                                if (block.content.isEmpty()) {
                                    Text(
                                        text = "List item...",
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                BlockType.CALLOUT -> {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = block.extra.ifBlank { "💡" },
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 10.dp)
                            )
                            BasicTextField(
                                value = block.content,
                                onValueChange = onContentChange,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    lineHeight = 20.sp
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("block_callout_${block.id}"),
                                decorationBox = { innerTextField ->
                                    if (block.content.isEmpty()) {
                                        Text(
                                            text = "Callout text, note, or warning...",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                BlockType.FLASHCARD -> {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, AiSparklePurple.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Flashcard",
                                        tint = AiSparklePurple,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "STUDY FLASHCARD",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AiSparklePurple,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Surface(
                                    color = AiSparklePurple.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.clickable {
                                        isFlashcardFlipped = !isFlashcardFlipped
                                    }
                                ) {
                                    Text(
                                        text = if (isFlashcardFlipped) "Show Question" else "Reveal Answer",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AiSparklePurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Question
                            Text(
                                text = "Question:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            BasicTextField(
                                value = block.content,
                                onValueChange = onContentChange,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                ),
                                cursorBrush = SolidColor(AiSparklePurple),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (block.content.isEmpty()) {
                                        Text("Enter prompt / concept question...", color = Color.Gray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            )

                            AnimatedVisibility(visible = isFlashcardFlipped) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(AiSparklePurple.copy(alpha = 0.15f))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Answer / Solution:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollabGreen
                                    )
                                    BasicTextField(
                                        value = block.extra,
                                        onValueChange = onExtraChange,
                                        textStyle = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onBackground
                                        ),
                                        cursorBrush = SolidColor(CollabGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (block.extra.isEmpty()) {
                                                Text("Enter explanation or key answer...", color = Color.Gray, fontSize = 14.sp)
                                            }
                                            innerTextField()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                BlockType.CODE_SNIPPET -> {
                    Surface(
                        color = Color(0xFF1E222A),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "Code",
                                        tint = Color(0xFF93C5FD),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = block.extra.ifBlank { "Code / Equation" },
                                        fontSize = 11.sp,
                                        color = Color(0xFF93C5FD),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            BasicTextField(
                                value = block.content,
                                onValueChange = onContentChange,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFE2E8F0),
                                    lineHeight = 18.sp
                                ),
                                cursorBrush = SolidColor(Color(0xFF93C5FD)),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { innerTextField ->
                                    if (block.content.isEmpty()) {
                                        Text("// Write code snippet or mathematical formula...", color = Color(0xFF64748B), fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                BlockType.QUOTE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = block.content,
                            onValueChange = onContentChange,
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                                lineHeight = 22.sp
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (block.content.isEmpty()) {
                                    Text("Quote or citation...", fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                BlockType.IMAGE -> {
                    val context = LocalContext.current
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surface),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(if (block.content.startsWith("/")) File(block.content) else block.content)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = block.extra.ifBlank { "Attached Note Image" },
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Caption
                            BasicTextField(
                                value = block.extra,
                                onValueChange = onExtraChange,
                                textStyle = TextStyle(
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    fontStyle = FontStyle.Italic
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                                decorationBox = { innerTextField ->
                                    if (block.extra.isEmpty()) {
                                        Text("Add an image caption or lecture reference...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontStyle = FontStyle.Italic)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                BlockType.ATTACHMENT_PDF -> {
                    val context = LocalContext.current
                    val parts = block.extra.split("|")
                    val fileName = if (parts.isNotEmpty() && parts[0].isNotBlank()) parts[0] else "Document.pdf"
                    val fileSize = if (parts.size > 1) parts[1] else "PDF Attachment"

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                try {
                                    val file = File(block.content)
                                    if (file.exists()) {
                                        val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(contentUri, "application/pdf")
                                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                                        }
                                        context.startActivity(intent)
                                    }
                                } catch (e: Exception) {
                                    // Fallback toast or handling
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = Color(0xFFEF4444).copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.PictureAsPdf,
                                            contentDescription = "PDF Document",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = fileName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "$fileSize • Tap to preview",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Icon(
                                Icons.Default.OpenInNew,
                                contentDescription = "Open PDF",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Options Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("block_menu_btn_${block.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Block Options",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Paragraph") },
                    onClick = { onTypeChange(BlockType.PARAGRAPH); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Heading 1") },
                    onClick = { onTypeChange(BlockType.HEADING_1); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Heading 2") },
                    onClick = { onTypeChange(BlockType.HEADING_2); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("To-do Checklist") },
                    onClick = { onTypeChange(BlockType.TODO); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Bullet List") },
                    onClick = { onTypeChange(BlockType.BULLET); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.FormatListBulleted, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Callout Box") },
                    onClick = { onTypeChange(BlockType.CALLOUT); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Study Flashcard") },
                    onClick = { onTypeChange(BlockType.FLASHCARD); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Code / Formula") },
                    onClick = { onTypeChange(BlockType.CODE_SNIPPET); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Quote") },
                    onClick = { onTypeChange(BlockType.QUOTE); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.FormatQuote, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete Block", color = Color(0xFFEF4444)) },
                    onClick = { onDelete(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFEF4444)) }
                )
            }
        }
    }
}
