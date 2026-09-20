package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiAiService
import com.example.model.Block
import com.example.model.BlockType
import com.example.model.ClassScheduleItem
import com.example.model.FocusSessionItem
import com.example.model.Page
import com.example.model.PageComment
import com.example.model.PomodoroTimerMode
import com.example.model.SubjectItem
import com.example.model.SyncStatus
import com.example.model.TaskItem
import com.example.model.TaskPriority
import com.example.model.ThemeMode
import com.example.repository.WorkspaceRepository
import com.example.sync.SyncEngineState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class AiTutorState(
    val isGenerating: Boolean = false,
    val activeAction: String? = null,
    val lastResponse: String? = null,
    val generatedTasks: List<String> = emptyList(),
    val generatedCards: List<Pair<String, String>> = emptyList(),
    val chatHistory: List<Pair<String, String>> = listOf(
        Pair("assistant", "👋 Hi! I'm your AI Study Copilot. How can I help you ace your classes today? I can summarize your notes, generate flashcards, extract homework tasks, or explain any tough concept step-by-step.")
    )
)

data class FlashcardStudySession(
    val isOpen: Boolean = false,
    val title: String = "",
    val cards: List<Pair<String, String>> = emptyList(),
    val currentIndex: Int = 0,
    val isFlipped: Boolean = false,
    val knownCount: Int = 0,
    val reviewCount: Int = 0
)

class WorkspaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkspaceRepository(application, viewModelScope)
    private val aiService = GeminiAiService()
    private val prefs = application.getSharedPreferences("student_helper_prefs", Context.MODE_PRIVATE)

    // --- Theme State (Light / Dark / System) ---
    private val savedThemeMode = try {
        ThemeMode.valueOf(prefs.getString("theme_mode", ThemeMode.LIGHT.name) ?: ThemeMode.LIGHT.name)
    } catch (e: Exception) {
        ThemeMode.LIGHT
    }
    private val _themeMode = MutableStateFlow(savedThemeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // --- Gemini API Key State ---
    private val _geminiApiKey = MutableStateFlow(prefs.getString("custom_gemini_api_key", "") ?: "")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    val isGeminiApiActive: StateFlow<Boolean> = _geminiApiKey.map {
        aiService.isApiKeyConfigured(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), aiService.isApiKeyConfigured(_geminiApiKey.value))

    fun saveGeminiApiKey(key: String) {
        val trimmed = key.trim()
        _geminiApiKey.value = trimmed
        prefs.edit().putString("custom_gemini_api_key", trimmed).apply()
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun toggleThemeMode() {
        val nextMode = when (_themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
        }
        setThemeMode(nextMode)
    }

    // --- Subjects Reactive Flow ---
    val subjects: StateFlow<List<SubjectItem>> = repository.allSubjectsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    fun createSubject(name: String, emoji: String, colorIndex: Int) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createSubject(name, emoji, colorIndex)
        }
    }

    fun deleteSubject(subject: SubjectItem) {
        viewModelScope.launch {
            repository.deleteSubject(subject.id)
            // If currently filtered by this subject, reset filter to "All"
            if (selectedSubjectFilter.value.equals(subject.name, ignoreCase = true)) {
                selectedSubjectFilter.value = "All"
            }
            if (selectedTaskSubjectFilter.value.equals(subject.name, ignoreCase = true)) {
                selectedTaskSubjectFilter.value = "All"
            }
        }
    }

    fun updateSubject(subject: SubjectItem) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    // --- Search & Filter State ---
    val searchQuery = MutableStateFlow("")
    val selectedSubjectFilter = MutableStateFlow("All")
    val selectedTaskFilter = MutableStateFlow("All") // "All", "Today", "Upcoming", "Completed"
    val selectedTaskSubjectFilter = MutableStateFlow("All")

    // --- UI Navigation State ---
    val currentTab = MutableStateFlow(0) // 0: Notes, 1: Tasks, 2: AI Tutor, 3: Sync & Collab
    val selectedPageId = MutableStateFlow<String?>(null)

    // --- AI Tutor State ---
    private val _aiState = MutableStateFlow(AiTutorState())
    val aiState: StateFlow<AiTutorState> = _aiState.asStateFlow()

    // --- Flashcard Review Session ---
    private val _studySession = MutableStateFlow(FlashcardStudySession())
    val studySession: StateFlow<FlashcardStudySession> = _studySession.asStateFlow()

    // --- Sync Engine State ---
    val syncState: StateFlow<SyncEngineState> = repository.syncEngine.syncState

    // --- Schedule State ---
    private fun getTodayDayOfWeek(): Int {
        val cal = Calendar.getInstance()
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    val selectedScheduleDay = MutableStateFlow(getTodayDayOfWeek()) // 0: All Week, 1: Mon, 2: Tue, ... 7: Sun

    val allSchedules: StateFlow<List<ClassScheduleItem>> = repository.allSchedulesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredSchedules: StateFlow<List<ClassScheduleItem>> = combine(
        allSchedules,
        selectedScheduleDay
    ) { list, day ->
        if (day == 0) list else list.filter { it.dayOfWeek == day }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createSchedule(
        subject: String,
        courseCode: String,
        title: String,
        professor: String,
        room: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        colorIndex: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.createSchedule(
                subject = subject,
                courseCode = courseCode,
                title = title,
                professor = professor,
                room = room,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                colorIndex = colorIndex,
                notes = notes
            )
        }
    }

    fun updateSchedule(schedule: ClassScheduleItem) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }

    fun deleteSchedule(scheduleId: String) {
        viewModelScope.launch {
            repository.deleteSchedule(scheduleId)
        }
    }

    fun createLectureNoteFromSchedule(schedule: ClassScheduleItem) {
        viewModelScope.launch {
            val emoji = when (schedule.subject.lowercase()) {
                "computer science" -> "💻"
                "biology" -> "🔬"
                "physics" -> "⚡"
                "mathematics" -> "📐"
                "literature" -> "📖"
                else -> "📝"
            }
            val title = "${schedule.courseCode.ifBlank { schedule.subject }}: ${schedule.title}"
            val newPageId = repository.createPage(
                title = title,
                emoji = emoji,
                subject = schedule.subject,
                coverIndex = schedule.colorIndex,
                templateType = "LECTURE"
            )
            selectPage(newPageId)
        }
    }

    // --- Focus Pomodoro Timer State ---
    val timerMode = MutableStateFlow(PomodoroTimerMode.FOCUS)
    val timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds = MutableStateFlow(25 * 60)
    val isTimerRunning = MutableStateFlow(false)
    val timerSubject = MutableStateFlow("Computer Science")
    val timerTag = MutableStateFlow("Deep Focus")
    val timerCelebrationMessage = MutableStateFlow<String?>(null)

    private var timerJob: Job? = null

    val allFocusSessions: StateFlow<List<FocusSessionItem>> = repository.allFocusSessionsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Focus Session Stats
    val todayFocusMinutes: StateFlow<Int> = allFocusSessions.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        list.filter { it.timestamp >= startOfToday }.sumOf { it.durationMinutes }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayFocusCount: StateFlow<Int> = allFocusSessions.map { list ->
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        list.count { it.timestamp >= startOfToday }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val subjectFocusStats: StateFlow<Map<String, Int>> = allFocusSessions.map { list ->
        list.groupBy { it.subject }.mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun startTimer() {
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        timerCelebrationMessage.value = null

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value && timerRemainingSeconds.value > 0) {
                delay(1000L)
                timerRemainingSeconds.value = (timerRemainingSeconds.value - 1).coerceAtLeast(0)
            }
            if (timerRemainingSeconds.value == 0) {
                onTimerFinished()
            }
        }
    }

    fun pauseTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
        timerJob = null
    }

    fun resetTimer() {
        pauseTimer()
        timerRemainingSeconds.value = timerTotalSeconds.value
        timerCelebrationMessage.value = null
    }

    fun adjustTimer(deltaSeconds: Int) {
        val newRemaining = (timerRemainingSeconds.value + deltaSeconds).coerceAtLeast(60)
        val newTotal = (timerTotalSeconds.value + deltaSeconds).coerceAtLeast(60)
        timerRemainingSeconds.value = newRemaining
        timerTotalSeconds.value = newTotal
    }

    fun switchTimerMode(mode: PomodoroTimerMode) {
        pauseTimer()
        timerMode.value = mode
        val durationMinutes = when (mode) {
            PomodoroTimerMode.FOCUS -> 25
            PomodoroTimerMode.SHORT_BREAK -> 5
            PomodoroTimerMode.LONG_BREAK -> 15
        }
        timerTotalSeconds.value = durationMinutes * 60
        timerRemainingSeconds.value = durationMinutes * 60
        timerCelebrationMessage.value = null
    }

    fun setCustomTimerMinutes(minutes: Int) {
        pauseTimer()
        val clamped = minutes.coerceIn(1, 180)
        timerTotalSeconds.value = clamped * 60
        timerRemainingSeconds.value = clamped * 60
        timerCelebrationMessage.value = null
    }

    private fun onTimerFinished() {
        isTimerRunning.value = false
        triggerTimerHaptic()

        if (timerMode.value == PomodoroTimerMode.FOCUS) {
            val minutesLogged = (timerTotalSeconds.value / 60).coerceAtLeast(1)
            viewModelScope.launch {
                repository.recordFocusSession(
                    subject = timerSubject.value,
                    durationMinutes = minutesLogged,
                    tag = timerTag.value
                )
            }
            timerCelebrationMessage.value = "🎉 Awesome focus session! Logged $minutesLogged min for ${timerSubject.value}. Take a well-deserved break!"
            // Switch to break mode
            switchTimerMode(PomodoroTimerMode.SHORT_BREAK)
        } else {
            timerCelebrationMessage.value = "☕ Break finished! Ready to jump into your next focus block?"
            switchTimerMode(PomodoroTimerMode.FOCUS)
        }
    }

    private fun triggerTimerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getApplication<Application>().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400), -1)
            }
        } catch (e: Exception) {
            // Ignore haptic failures
        }
    }

    fun deleteFocusSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteFocusSession(sessionId)
        }
    }

    fun clearAllFocusSessions() {
        viewModelScope.launch {
            repository.clearFocusSessions()
        }
    }


    // --- Pages Reactive Flow ---
    val pages: StateFlow<List<Page>> = combine(
        repository.allPagesFlow,
        searchQuery,
        selectedSubjectFilter
    ) { allPages, query, subject ->
        allPages.filter { page ->
            val matchesQuery = query.isBlank() || page.title.contains(query, ignoreCase = true) || page.subject.contains(query, ignoreCase = true)
            val matchesSubject = subject == "All" || page.subject.equals(subject, ignoreCase = true)
            matchesQuery && matchesSubject
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Current Page & Blocks ---
    private val _currentBlocks = MutableStateFlow<List<Block>>(emptyList())
    val currentBlocks: StateFlow<List<Block>> = _currentBlocks.asStateFlow()

    private val _currentPage = MutableStateFlow<Page?>(null)
    val currentPage: StateFlow<Page?> = _currentPage.asStateFlow()

    private val _currentComments = MutableStateFlow<List<PageComment>>(emptyList())
    val currentComments: StateFlow<List<PageComment>> = _currentComments.asStateFlow()

    // --- Tasks Reactive Flow (Using independent Task Subject Filter) ---
    val tasks: StateFlow<List<TaskItem>> = combine(
        repository.allTasksFlow,
        searchQuery,
        selectedTaskFilter,
        selectedTaskSubjectFilter
    ) { allTasks, query, filter, subject ->
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 3600 * 1000L
        allTasks.filter { task ->
            val matchesQuery = query.isBlank() || task.title.contains(query, ignoreCase = true) || task.subject.contains(query, ignoreCase = true)
            val matchesSubject = subject == "All" || task.subject.equals(subject, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Today" -> task.dueDateMillis in (now - oneDayMillis)..(now + oneDayMillis) && !task.isCompleted
                "Upcoming" -> task.dueDateMillis > now && !task.isCompleted
                "Completed" -> task.isCompleted
                else -> true
            }
            matchesQuery && matchesSubject && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPage(pageId: String?) {
        selectedPageId.value = pageId
        if (pageId != null) {
            viewModelScope.launch {
                repository.getPageFlow(pageId).collect { page ->
                    _currentPage.value = page
                }
            }
            viewModelScope.launch {
                repository.getBlocksFlow(pageId).collect { blocks ->
                    _currentBlocks.value = blocks
                }
            }
            viewModelScope.launch {
                repository.getCommentsFlow(pageId).collect { comments ->
                    _currentComments.value = comments
                }
            }
        } else {
            _currentPage.value = null
            _currentBlocks.value = emptyList()
            _currentComments.value = emptyList()
        }
    }

    // --- Page Actions ---
    fun createNewPage(title: String, emoji: String, subject: String, coverIndex: Int, template: String? = null) {
        viewModelScope.launch {
            val newId = repository.createPage(title, emoji, subject, coverIndex, template)
            selectPage(newId)
        }
    }

    fun updatePageTitle(title: String) {
        val current = _currentPage.value ?: return
        viewModelScope.launch {
            repository.updatePage(current.copy(title = title))
        }
    }

    fun updatePageEmoji(emoji: String) {
        val current = _currentPage.value ?: return
        viewModelScope.launch {
            repository.updatePage(current.copy(emoji = emoji))
        }
    }

    fun updatePageSubject(subject: String) {
        val current = _currentPage.value ?: return
        viewModelScope.launch {
            repository.updatePage(current.copy(subject = subject))
        }
    }

    fun togglePageFavorite(page: Page) {
        viewModelScope.launch {
            repository.updatePage(page.copy(isFavorite = !page.isFavorite))
        }
    }

    fun deleteCurrentPage() {
        val current = _currentPage.value ?: return
        viewModelScope.launch {
            repository.deletePage(current.id)
            selectPage(null)
        }
    }

    fun updatePageCover(coverIndex: Int) {
        val current = _currentPage.value ?: return
        viewModelScope.launch {
            repository.updatePage(current.copy(coverColorIndex = coverIndex))
        }
    }

    // --- Block Actions ---
    fun addBlock(type: BlockType, content: String = "", extra: String = "") {
        val pageId = selectedPageId.value ?: return
        viewModelScope.launch {
            repository.addBlock(pageId, type, content, extra)
        }
    }

    fun addMediaBlock(type: BlockType, uri: Uri, defaultName: String, caption: String = "") {
        val pageId = selectedPageId.value ?: return
        viewModelScope.launch {
            val (savedPath, sizeInfo) = repository.saveMediaFile(uri, defaultName)
            val extraInfo = if (type == BlockType.ATTACHMENT_PDF) {
                "$defaultName|$sizeInfo"
            } else {
                caption.ifBlank { defaultName }
            }
            repository.addBlock(pageId, type, savedPath, extraInfo)
        }
    }

    fun updateBlockContent(block: Block, newContent: String) {
        viewModelScope.launch {
            repository.updateBlock(block.copy(content = newContent))
        }
    }

    fun updateBlockExtra(block: Block, newExtra: String) {
        viewModelScope.launch {
            repository.updateBlock(block.copy(extra = newExtra))
        }
    }

    fun toggleBlockChecked(block: Block) {
        viewModelScope.launch {
            repository.updateBlock(block.copy(isChecked = !block.isChecked))
        }
    }

    fun deleteBlock(blockId: String) {
        viewModelScope.launch {
            repository.deleteBlock(blockId)
        }
    }

    // --- Task Actions ---
    fun createTask(
        title: String,
        description: String = "",
        subject: String = "General",
        dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
        priority: TaskPriority = TaskPriority.MEDIUM,
        hasReminder: Boolean = true
    ) {
        viewModelScope.launch {
            repository.createTask(title, description, subject, dueDateMillis, priority, hasReminder)
        }
    }

    fun toggleTaskCompleted(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // --- Comments & Collaboration Actions ---
    fun addCommentToCurrentPage(text: String) {
        val pageId = selectedPageId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.addComment(pageId, text)
        }
    }

    fun simulatePeerActivity() {
        val pageId = selectedPageId.value ?: "page-cs101"
        repository.syncEngine.simulateCollaboratorAction(pageId, "Lucas Chen")
    }

    fun triggerSyncNow() {
        repository.syncEngine.triggerSyncNow()
    }

    fun toggleOfflineMode(forceOffline: Boolean) {
        repository.syncEngine.toggleManualOfflineMode(forceOffline)
    }

    // --- Flashcard Study Mode ---
    fun startFlashcardStudy(title: String, cards: List<Pair<String, String>>) {
        if (cards.isEmpty()) return
        _studySession.value = FlashcardStudySession(
            isOpen = true,
            title = title,
            cards = cards,
            currentIndex = 0,
            isFlipped = false,
            knownCount = 0,
            reviewCount = 0
        )
    }

    fun flipCurrentCard() {
        _studySession.value = _studySession.value.copy(isFlipped = !_studySession.value.isFlipped)
    }

    fun recordCardAnswer(isKnown: Boolean) {
        val current = _studySession.value
        val nextIndex = current.currentIndex + 1
        val newKnown = if (isKnown) current.knownCount + 1 else current.knownCount
        val newReview = if (!isKnown) current.reviewCount + 1 else current.reviewCount

        if (nextIndex < current.cards.size) {
            _studySession.value = current.copy(
                currentIndex = nextIndex,
                isFlipped = false,
                knownCount = newKnown,
                reviewCount = newReview
            )
        } else {
            // Finished
            _studySession.value = current.copy(
                currentIndex = nextIndex,
                knownCount = newKnown,
                reviewCount = newReview
            )
        }
    }

    fun closeStudySession() {
        _studySession.value = FlashcardStudySession(isOpen = false)
    }

    // --- AI Assistant Actions ---
    fun summarizeCurrentPage() {
        val page = _currentPage.value ?: return
        val blocks = _currentBlocks.value
        val contentText = blocks.joinToString("\n") { "${it.type}: ${it.content} ${it.extra}" }

        _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Summarizing Page")
        viewModelScope.launch {
            val result = aiService.summarizeNote(page.title, contentText, _geminiApiKey.value)
            val summaryText = result.getOrDefault("Failed to generate summary.")
            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                lastResponse = summaryText
            )
            // Also append summary callout block to page
            repository.addBlock(
                pageId = page.id,
                type = BlockType.CALLOUT,
                content = summaryText,
                extra = "✨"
            )
        }
    }

    fun generateFlashcardsForCurrentPage() {
        val page = _currentPage.value ?: return
        val blocks = _currentBlocks.value
        val contentText = blocks.joinToString("\n") { "${it.type}: ${it.content} ${it.extra}" }

        _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Generating Flashcards")
        viewModelScope.launch {
            val result = aiService.generateFlashcards(page.title, contentText, _geminiApiKey.value)
            val cards = result.getOrDefault(emptyList())
            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                generatedCards = cards
            )
            // Add flashcards as blocks to page
            cards.forEach { (q, a) ->
                repository.addBlock(
                    pageId = page.id,
                    type = BlockType.FLASHCARD,
                    content = q,
                    extra = a
                )
            }
        }
    }

    fun extractTasksFromCurrentPage() {
        val page = _currentPage.value ?: return
        val blocks = _currentBlocks.value
        val contentText = blocks.joinToString("\n") { "${it.type}: ${it.content} ${it.extra}" }

        _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Extracting Homework Tasks")
        viewModelScope.launch {
            val result = aiService.extractTasks(page.title, contentText, _geminiApiKey.value)
            val tasksList = result.getOrDefault(emptyList())
            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                generatedTasks = tasksList
            )
            // Add tasks to both page as TODO blocks and workspace Tasks table with reminder!
            tasksList.forEach { taskTitle ->
                repository.addBlock(
                    pageId = page.id,
                    type = BlockType.TODO,
                    content = taskTitle
                )
                repository.createTask(
                    title = taskTitle,
                    description = "Extracted from note: ${page.title}",
                    subject = page.subject,
                    dueDateMillis = System.currentTimeMillis() + 86400000L,
                    priority = TaskPriority.HIGH,
                    hasReminder = true
                )
            }
        }
    }

    fun askAiStudyBuddy(userPrompt: String) {
        if (userPrompt.isBlank()) return
        val currentHistory = _aiState.value.chatHistory
        val historyWithUser = currentHistory + Pair("user", userPrompt)
        _aiState.value = _aiState.value.copy(
            isGenerating = true,
            activeAction = "Consulting Gemini AI Study Copilot",
            chatHistory = historyWithUser
        )

        viewModelScope.launch {
            val contextText = _currentPage.value?.let { page ->
                "Active Study Note: ${page.title} (${page.subject})\n"
            } ?: ""
            val fullPrompt = if (contextText.isNotBlank()) "$contextText\nQuestion: $userPrompt" else userPrompt
            val result = aiService.generateChatResponse(currentHistory, fullPrompt, _geminiApiKey.value)
            val answer = result.getOrDefault("I'm here to help! Could you clarify or provide more details?")

            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                chatHistory = _aiState.value.chatHistory + Pair("assistant", answer)
            )
        }
    }

    fun clearAiChatHistory() {
        _aiState.value = _aiState.value.copy(
            chatHistory = listOf(
                Pair("assistant", "👋 Hi! I'm your AI Study Copilot powered by Gemini 3.5 Flash. How can I help you ace your classes today? I can summarize your notes, generate flashcards, create practice quizzes, extract homework tasks, or explain any tough concept step-by-step.")
            )
        )
    }

    fun generateQuizForCurrentPage() {
        val page = _currentPage.value ?: return
        val blocks = _currentBlocks.value
        val contentText = blocks.joinToString("\n") { "${it.type}: ${it.content} ${it.extra}" }

        _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Creating Practice Quiz with Gemini")
        viewModelScope.launch {
            val result = aiService.generateQuiz(page.title, contentText, _geminiApiKey.value)
            val quizText = result.getOrDefault("Could not generate quiz.")
            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                lastResponse = quizText
            )
            repository.addBlock(
                pageId = page.id,
                type = BlockType.CALLOUT,
                content = quizText,
                extra = "📝"
            )
        }
    }

    fun analyzeCurrentPageImage(bitmap: Bitmap) {
        val page = _currentPage.value ?: return
        _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Analyzing Diagram with Gemini Multimodal AI")
        viewModelScope.launch {
            val result = aiService.analyzeImage(
                bitmap = bitmap,
                prompt = "Please analyze this study diagram/image from '${page.title}'. Explain key mechanisms, formulas, and high-yield exam takeaways:",
                customApiKey = _geminiApiKey.value
            )
            val analysisText = result.getOrDefault("Image analyzed.")
            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                lastResponse = analysisText
            )
            repository.addBlock(
                pageId = page.id,
                type = BlockType.CALLOUT,
                content = analysisText,
                extra = "🔬"
            )
        }
    }

    fun generateWeeklyStudyPlan() {
        val schedules = allSchedules.value
        val tasks = repository.allTasksFlow
        viewModelScope.launch {
            _aiState.value = _aiState.value.copy(isGenerating = true, activeAction = "Synthesizing Weekly Study Plan with Gemini")
            val classSummary = schedules.joinToString("\n") {
                "• ${it.courseCode} - ${it.title} (${it.startTime} to ${it.endTime}) in ${it.room}"
            }.ifBlank { "Standard Monday-Friday classes" }

            val taskSummary = "Urgent assignments and review blocks"
            val result = aiService.generateStudyPlan(classSummary, taskSummary, _geminiApiKey.value)
            val planText = result.getOrDefault("Study plan generated.")

            _aiState.value = _aiState.value.copy(
                isGenerating = false,
                activeAction = null,
                chatHistory = _aiState.value.chatHistory + Pair("assistant", planText)
            )
        }
    }
}
