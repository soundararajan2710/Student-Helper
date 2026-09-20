package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.CollabGreen
import com.example.ui.theme.NotionBlack
import com.example.ui.theme.UrgentRed
import com.example.viewmodel.FlashcardStudySession

@Composable
fun FlashcardStudyDialog(
    session: FlashcardStudySession,
    onFlip: () -> Unit,
    onAnswer: (Boolean) -> Unit,
    onClose: () -> Unit,
    onRestart: () -> Unit
) {
    if (!session.isOpen) return

    val totalCards = session.cards.size
    val isFinished = session.currentIndex >= totalCards

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("flashcard_study_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(AiSparklePurple.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = AiSparklePurple,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Active Recall Practice",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = session.title.ifBlank { "Study Session" },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_study_dialog_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isFinished && totalCards > 0) {
                    // Progress
                    val progress = session.currentIndex.toFloat() / totalCards
                    LinearProgressIndicator(
                        progress = { progress },
                        color = AiSparklePurple,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Card ${session.currentIndex + 1} of $totalCards",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row {
                            Text(
                                text = "✓ ${session.knownCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = CollabGreen
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "↺ ${session.reviewCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = UrgentRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current Flashcard
                    val currentPair = session.cards.getOrNull(session.currentIndex) ?: Pair("", "")
                    val rotation by animateFloatAsState(
                        targetValue = if (session.isFlipped) 180f else 0f,
                        animationSpec = tween(400),
                        label = "card_flip"
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (session.isFlipped) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .clickable { onFlip() }
                            .testTag("active_flashcard_card")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            if (rotation <= 90f) {
                                // Front (Question)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        color = AiSparklePurple.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "QUESTION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AiSparklePurple,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Text(
                                        text = currentPair.first,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        lineHeight = 26.sp
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text(
                                        text = "👆 Tap anywhere to reveal answer",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            } else {
                                // Back (Answer) - Mirror correction
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.graphicsLayer { rotationY = 180f }
                                ) {
                                    Surface(
                                        color = CollabGreen.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ANSWER & EXPLANATION",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CollabGreen,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(18.dp))
                                    Text(
                                        text = currentPair.second.ifBlank { "Concept answer verified." },
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        color = Color.White,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = { onAnswer(false) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UrgentRed),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("flashcard_review_btn")
                        ) {
                            Text("↺ Review Again", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onAnswer(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = CollabGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("flashcard_know_btn")
                        ) {
                            Text("✓ Mastered!", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    // Summary Session Complete
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Text(text = "🎉", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Study Session Complete!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Great active recall session! Here is your study breakdown:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                color = CollabGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "${session.knownCount}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CollabGreen
                                    )
                                    Text(text = "Mastered", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Surface(
                                color = UrgentRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.width(130.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "${session.reviewCount}",
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = UrgentRed
                                    )
                                    Text(text = "Needs Review", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = onRestart,
                            colors = ButtonDefaults.buttonColors(containerColor = NotionBlack),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Practice Again", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onClose,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Return to Note")
                        }
                    }
                }
            }
        }
    }
}
