package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ThemeMode
import com.example.ui.components.FlashcardStudyDialog
import com.example.ui.screens.AiStudyTutorScreen
import com.example.ui.screens.FocusTimerScreen
import com.example.ui.screens.PageEditorScreen
import com.example.ui.screens.PagesListScreen
import com.example.ui.screens.ScheduleTimetableScreen
import com.example.ui.screens.TasksListScreen
import com.example.ui.theme.AiSparklePurple
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.WorkspaceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: WorkspaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                // Request Notification Permission on Android 13+
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* granted or denied */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: WorkspaceViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedPageId by viewModel.selectedPageId.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val studySession by viewModel.studySession.collectAsStateWithLifecycle()

    if (selectedPageId != null) {
        // Page Editor View
        PageEditorScreen(
            viewModel = viewModel,
            onBack = { viewModel.selectPage(null) }
        )
    } else {
        // Main Tab Layout
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
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
                                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                ) {
                                    Text(text = "🎓", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Student Helper",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Notes • Tasks • AI Tutor",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                // Theme Mode Toggle (Light / Dark / Auto)
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .clickable { viewModel.toggleThemeMode() }
                                        .testTag("theme_mode_toggle_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (themeMode) {
                                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                                ThemeMode.DARK -> Icons.Default.DarkMode
                                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                            },
                                            contentDescription = "Toggle Theme",
                                            tint = when (themeMode) {
                                                ThemeMode.LIGHT -> Color(0xFFF59E0B)
                                                ThemeMode.DARK -> Color(0xFF818CF8)
                                                ThemeMode.SYSTEM -> MaterialTheme.colorScheme.primary
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when (themeMode) {
                                                ThemeMode.LIGHT -> "Light"
                                                ThemeMode.DARK -> "Dark"
                                                ThemeMode.SYSTEM -> "Auto"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // 0: Notes
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.currentTab.value = 0 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.Description else Icons.Outlined.Description,
                                contentDescription = "Notes"
                            )
                        },
                        label = { Text("Notes", fontSize = 10.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_notes")
                    )

                    // 1: Schedule Timetable
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.currentTab.value = 1 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                contentDescription = "Schedule"
                            )
                        },
                        label = { Text("Schedule", fontSize = 10.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_schedule")
                    )

                    // 2: Focus Pomodoro Timer
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.currentTab.value = 2 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Filled.Timer else Icons.Outlined.Timer,
                                contentDescription = "Focus Timer"
                            )
                        },
                        label = { Text("Focus", fontSize = 10.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_focus_timer")
                    )

                    // 3: Tasks
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.currentTab.value = 3 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                contentDescription = "Tasks"
                            )
                        },
                        label = { Text("Tasks", fontSize = 10.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_tasks")
                    )

                    // 4: Gemini AI Tutor
                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { viewModel.currentTab.value = 4 },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 4) Icons.Filled.AutoAwesome else Icons.Outlined.AutoAwesome,
                                contentDescription = "AI Copilot"
                            )
                        },
                        label = { Text("AI Tutor", fontSize = 10.sp, fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("tab_ai_tutor")
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tab_transition"
                ) { tab ->
                    when (tab) {
                        0 -> PagesListScreen(viewModel = viewModel)
                        1 -> ScheduleTimetableScreen(viewModel = viewModel)
                        2 -> FocusTimerScreen(viewModel = viewModel)
                        3 -> TasksListScreen(viewModel = viewModel)
                        4 -> AiStudyTutorScreen(viewModel = viewModel)
                        else -> PagesListScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Flashcard Study Practice Dialog Overlay
    FlashcardStudyDialog(
        session = studySession,
        onFlip = { viewModel.flipCurrentCard() },
        onAnswer = { isKnown -> viewModel.recordCardAnswer(isKnown) },
        onClose = { viewModel.closeStudySession() },
        onRestart = {
            viewModel.startFlashcardStudy(studySession.title, studySession.cards)
        }
    )
}
