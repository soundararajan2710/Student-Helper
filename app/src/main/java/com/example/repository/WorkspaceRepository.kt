package com.example.repository

import android.content.Context
import com.example.database.AppDatabase
import com.example.database.dao.WorkspaceDao
import com.example.database.entity.BlockEntity
import com.example.database.entity.CommentEntity
import com.example.database.entity.FocusSessionEntity
import com.example.database.entity.PageEntity
import com.example.database.entity.ScheduleEntity
import com.example.database.entity.SubjectEntity
import com.example.database.entity.TaskEntity
import com.example.model.Block
import com.example.model.BlockType
import com.example.model.ClassScheduleItem
import com.example.model.FocusSessionItem
import com.example.model.Page
import com.example.model.PageComment
import com.example.model.SubjectItem
import com.example.model.SyncStatus
import com.example.model.TaskItem
import com.example.model.TaskPriority
import com.example.reminder.ReminderManager
import com.example.sync.SyncEngine
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class WorkspaceRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val db = AppDatabase.getDatabase(context)
    private val dao: WorkspaceDao = db.workspaceDao()
    val syncEngine = SyncEngine(context, dao, scope)
    val reminderManager = ReminderManager(context)

    init {
        scope.launch(Dispatchers.IO) {
            seedDefaultStudentWorkspaceIfNeeded()
        }
    }

    // --- Subjects Stream & Operations ---
    val allSubjectsFlow: Flow<List<SubjectItem>> = dao.getAllSubjectsFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun createSubject(name: String, emoji: String, colorIndex: Int): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val subject = SubjectItem(
            id = id,
            name = name.trim(),
            emoji = emoji.ifBlank { "📚" },
            colorIndex = colorIndex,
            isDefault = false
        )
        dao.insertOrUpdateSubject(subject.toEntity())
        return@withContext id
    }

    suspend fun updateSubject(subject: SubjectItem) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSubject(subject.toEntity())
    }

    suspend fun deleteSubject(subjectId: String) = withContext(Dispatchers.IO) {
        dao.deleteSubject(subjectId)
    }

    // --- Media File Saving (Internal storage for permanent URI persistence) ---
    suspend fun saveMediaFile(uri: Uri, defaultName: String): Pair<String, String> = withContext(Dispatchers.IO) {
        try {
            val mediaDir = File(context.filesDir, "notes_media").apply { if (!exists()) mkdirs() }
            val fileName = "${System.currentTimeMillis()}_${defaultName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")}"
            val destFile = File(mediaDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            val fileSizeKb = if (destFile.exists()) (destFile.length() / 1024).toInt() else 0
            val sizeStr = if (fileSizeKb > 1024) String.format("%.1f MB", fileSizeKb / 1024f) else "$fileSizeKb KB"
            return@withContext Pair(destFile.absolutePath, sizeStr)
        } catch (e: Exception) {
            return@withContext Pair(uri.toString(), "Attachment")
        }
    }

    // --- Pages Stream ---
    val allPagesFlow: Flow<List<Page>> = dao.getAllPagesFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getPageFlow(pageId: String): Flow<Page?> = dao.getPageByIdFlow(pageId).map { it?.toDomain() }

    fun getBlocksFlow(pageId: String): Flow<List<Block>> = dao.getBlocksForPageFlow(pageId).map { list ->
        list.map { it.toDomain() }
    }

    fun getCommentsFlow(pageId: String): Flow<List<PageComment>> = dao.getCommentsForPageFlow(pageId).map { list ->
        list.map { it.toDomain() }
    }

    // --- Tasks Stream ---
    val allTasksFlow: Flow<List<TaskItem>> = dao.getAllTasksFlow().map { list ->
        list.map { it.toDomain() }
    }

    // --- Schedules Stream & Operations ---
    val allSchedulesFlow: Flow<List<ClassScheduleItem>> = dao.getAllSchedulesFlow().map { list ->
        list.map { it.toDomain() }
    }

    fun getSchedulesForDayFlow(dayOfWeek: Int): Flow<List<ClassScheduleItem>> =
        dao.getSchedulesForDayFlow(dayOfWeek).map { list -> list.map { it.toDomain() } }

    suspend fun createSchedule(
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
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val schedule = ClassScheduleItem(
            id = id,
            subject = subject,
            courseCode = courseCode,
            title = title.ifBlank { "Class Lecture" },
            professor = professor,
            room = room,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            colorIndex = colorIndex,
            notes = notes
        )
        dao.insertOrUpdateSchedule(schedule.toEntity())
        return@withContext id
    }

    suspend fun updateSchedule(schedule: ClassScheduleItem) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateSchedule(schedule.toEntity())
    }

    suspend fun deleteSchedule(scheduleId: String) = withContext(Dispatchers.IO) {
        dao.deleteSchedule(scheduleId)
    }

    // --- Focus Sessions Stream & Operations ---
    val allFocusSessionsFlow: Flow<List<FocusSessionItem>> = dao.getAllFocusSessionsFlow().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun recordFocusSession(
        subject: String,
        durationMinutes: Int,
        tag: String = "Focus Session",
        notePageId: String? = null
    ): String = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val session = FocusSessionItem(
            id = id,
            subject = subject,
            durationMinutes = durationMinutes,
            timestamp = System.currentTimeMillis(),
            tag = tag,
            notePageId = notePageId
        )
        dao.insertFocusSession(session.toEntity())
        return@withContext id
    }

    suspend fun deleteFocusSession(sessionId: String) = withContext(Dispatchers.IO) {
        dao.deleteFocusSession(sessionId)
    }

    suspend fun clearFocusSessions() = withContext(Dispatchers.IO) {
        dao.clearAllFocusSessions()
    }


    // --- Page Operations ---
    suspend fun createPage(
        title: String,
        emoji: String = "📝",
        subject: String = "General",
        coverIndex: Int = 0,
        templateType: String? = null
    ): String = withContext(Dispatchers.IO) {
        val pageId = UUID.randomUUID().toString()
        val page = Page(
            id = pageId,
            title = title.ifBlank { "Untitled Note" },
            emoji = emoji,
            subject = subject,
            coverColorIndex = coverIndex,
            isFavorite = false,
            isCollaborative = true,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED,
            sharedWith = listOf("Lucas Chen", "Emma Watson")
        )
        dao.insertOrUpdatePage(page.toEntity())
        syncEngine.queueChange("PAGE", pageId, "INSERT", page)

        // Seed template blocks if requested
        val initialBlocks = when (templateType) {
            "LECTURE" -> listOf(
                Block(UUID.randomUUID().toString(), pageId, BlockType.CALLOUT, "💡 **Lecture Objective**: Capture core concepts, derivations, and exam questions.", extra = "💡", orderIndex = 0),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_1, "Main Topics & Theorem Overview", orderIndex = 1),
                Block(UUID.randomUUID().toString(), pageId, BlockType.PARAGRAPH, "Start typing or use the AI magic wand to generate structured lecture summaries.", orderIndex = 2),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_2, "Key Takeaways & Formulas", orderIndex = 3),
                Block(UUID.randomUUID().toString(), pageId, BlockType.BULLET, "Definition 1: Foundation principles and edge conditions", orderIndex = 4),
                Block(UUID.randomUUID().toString(), pageId, BlockType.BULLET, "Equation: f(x) = ax^2 + bx + c", orderIndex = 5),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_2, "Action Items / Homework", orderIndex = 6),
                Block(UUID.randomUUID().toString(), pageId, BlockType.TODO, "Review lecture slides 14-28", isChecked = false, orderIndex = 7),
                Block(UUID.randomUUID().toString(), pageId, BlockType.TODO, "Solve practice problem set #4", isChecked = false, orderIndex = 8)
            )
            "EXAM_PREP" -> listOf(
                Block(UUID.randomUUID().toString(), pageId, BlockType.CALLOUT, "🎯 **Exam Date**: In 2 Weeks | Target Grade: A+", extra = "🎯", orderIndex = 0),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_1, "High-Yield Review Topics", orderIndex = 1),
                Block(UUID.randomUUID().toString(), pageId, BlockType.FLASHCARD, "What is the Time Complexity of QuickSort?", extra = "Average: O(n log n), Worst-case: O(n^2)", orderIndex = 2),
                Block(UUID.randomUUID().toString(), pageId, BlockType.FLASHCARD, "Difference between Stack and Heap memory?", extra = "Stack is static & thread-bound; Heap is dynamic.", orderIndex = 3),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_2, "Mock Quiz Checklist", orderIndex = 4),
                Block(UUID.randomUUID().toString(), pageId, BlockType.TODO, "Complete 2025 Midterm Practice Paper", isChecked = false, orderIndex = 5),
                Block(UUID.randomUUID().toString(), pageId, BlockType.TODO, "Review tricky edge cases with study partner", isChecked = false, orderIndex = 6)
            )
            "BRAIN_DUMP" -> listOf(
                Block(UUID.randomUUID().toString(), pageId, BlockType.CALLOUT, "🧠 Capture quick student thoughts, group project ideas, and links.", extra = "🧠", orderIndex = 0),
                Block(UUID.randomUUID().toString(), pageId, BlockType.HEADING_1, "Ideas & Notes", orderIndex = 1),
                Block(UUID.randomUUID().toString(), pageId, BlockType.BULLET, "Project idea: Mobile decentralized study hub", orderIndex = 2),
                Block(UUID.randomUUID().toString(), pageId, BlockType.BULLET, "Group meeting this Thursday at library 4PM", orderIndex = 3)
            )
            else -> listOf(
                Block(UUID.randomUUID().toString(), pageId, BlockType.PARAGRAPH, "Start writing your student note here, add checklists, or tap AI to draft ideas...", orderIndex = 0)
            )
        }

        dao.insertBlocks(initialBlocks.map { it.toEntity() })
        return@withContext pageId
    }

    suspend fun updatePage(page: Page) = withContext(Dispatchers.IO) {
        val updated = page.copy(updatedAt = System.currentTimeMillis())
        dao.insertOrUpdatePage(updated.toEntity())
        syncEngine.queueChange("PAGE", updated.id, "UPDATE", updated)
    }

    suspend fun deletePage(pageId: String) = withContext(Dispatchers.IO) {
        dao.deleteBlocksForPage(pageId)
        dao.deletePage(pageId)
        syncEngine.queueChange("PAGE", pageId, "DELETE", mapOf("id" to pageId))
    }

    // --- Block Operations ---
    suspend fun addBlock(pageId: String, type: BlockType, content: String = "", extra: String = "", orderIndex: Int? = null) = withContext(Dispatchers.IO) {
        val currentBlocks = dao.getBlocksForPage(pageId)
        val nextIndex = orderIndex ?: (currentBlocks.maxOfOrNull { it.orderIndex }?.plus(1) ?: 0)
        val block = Block(
            id = UUID.randomUUID().toString(),
            pageId = pageId,
            type = type,
            content = content,
            isChecked = false,
            extra = extra,
            orderIndex = nextIndex
        )
        dao.insertOrUpdateBlock(block.toEntity())
        syncEngine.queueChange("BLOCK", block.id, "INSERT", block)
    }

    suspend fun updateBlock(block: Block) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateBlock(block.toEntity())
        syncEngine.queueChange("BLOCK", block.id, "UPDATE", block)
    }

    suspend fun deleteBlock(blockId: String) = withContext(Dispatchers.IO) {
        dao.deleteBlock(blockId)
        syncEngine.queueChange("BLOCK", blockId, "DELETE", mapOf("id" to blockId))
    }

    // --- Task Operations ---
    suspend fun createTask(
        title: String,
        description: String = "",
        subject: String = "General",
        dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
        priority: TaskPriority = TaskPriority.MEDIUM,
        hasReminder: Boolean = true
    ): String = withContext(Dispatchers.IO) {
        val taskId = UUID.randomUUID().toString()
        val task = TaskItem(
            id = taskId,
            title = title.ifBlank { "New Task" },
            description = description,
            subject = subject,
            dueDateMillis = dueDateMillis,
            priority = priority,
            isCompleted = false,
            hasReminder = hasReminder,
            syncStatus = SyncStatus.SYNCED
        )
        dao.insertOrUpdateTask(task.toEntity())
        if (hasReminder) {
            reminderManager.scheduleTaskReminder(task)
        }
        syncEngine.queueChange("TASK", taskId, "INSERT", task)
        return@withContext taskId
    }

    suspend fun updateTask(task: TaskItem) = withContext(Dispatchers.IO) {
        dao.insertOrUpdateTask(task.toEntity())
        if (task.hasReminder && !task.isCompleted) {
            reminderManager.scheduleTaskReminder(task)
        } else {
            reminderManager.cancelTaskReminder(task.id)
        }
        syncEngine.queueChange("TASK", task.id, "UPDATE", task)
    }

    suspend fun deleteTask(taskId: String) = withContext(Dispatchers.IO) {
        reminderManager.cancelTaskReminder(taskId)
        dao.deleteTask(taskId)
        syncEngine.queueChange("TASK", taskId, "DELETE", mapOf("id" to taskId))
    }

    suspend fun addComment(pageId: String, text: String, author: String = "Me (You)") = withContext(Dispatchers.IO) {
        val comment = PageComment(
            id = UUID.randomUUID().toString(),
            pageId = pageId,
            authorName = author,
            authorAvatar = "🧑‍🎓",
            text = text,
            timestamp = System.currentTimeMillis()
        )
        dao.insertComment(comment.toEntity())
        syncEngine.queueChange("COMMENT", comment.id, "INSERT", comment)
    }

    // --- Seed Default Workspace for Student Experience (One-time only on first run) ---
    private suspend fun seedDefaultStudentWorkspaceIfNeeded() {
        val prefs = context.getSharedPreferences("student_helper_repo_prefs", Context.MODE_PRIVATE)
        val hasSeeded = prefs.getBoolean("has_seeded_initial_data_v3", false)
        if (hasSeeded) return
        prefs.edit().putBoolean("has_seeded_initial_data_v3", true).apply()

        // 1. CS 101 Page
        val csPage = Page(
            id = "page-cs101",
            title = "CS 101: Data Structures & Algorithms",
            emoji = "💻",
            subject = "Computer Science",
            coverColorIndex = 0,
            isFavorite = true,
            isCollaborative = true,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED,
            sharedWith = listOf("Lucas Chen", "Emma Watson")
        )
        dao.insertOrUpdatePage(csPage.toEntity())

        val csBlocks = listOf(
            Block("cs-b1", "page-cs101", BlockType.CALLOUT, "⚡ **Midterm Exam**: Next Wednesday covering Binary Trees, Sorting, and Graphs.", extra = "⚡", orderIndex = 0),
            Block("cs-b2", "page-cs101", BlockType.HEADING_1, "Core Concepts: Graph Traversals", orderIndex = 1),
            Block("cs-b3", "page-cs101", BlockType.PARAGRAPH, "BFS uses a Queue (FIFO) for level-order exploration. DFS uses a Stack (or recursion) to explore as deep as possible before backtracking.", orderIndex = 2),
            Block("cs-b4", "page-cs101", BlockType.HEADING_2, "Flashcard Checkpoints", orderIndex = 3),
            Block("cs-b5", "page-cs101", BlockType.FLASHCARD, "What is the time complexity of BFS on graph G(V, E)?", extra = "O(V + E) using adjacency list representation", orderIndex = 4),
            Block("cs-b6", "page-cs101", BlockType.FLASHCARD, "When does Dijkstra's algorithm fail?", extra = "When edges have negative weights (use Bellman-Ford instead)", orderIndex = 5),
            Block("cs-b7", "page-cs101", BlockType.HEADING_2, "Homework Assignment #4", orderIndex = 6),
            Block("cs-b8", "page-cs101", BlockType.TODO, "Implement AVL Tree balancing rotation in Kotlin/Java", isChecked = true, orderIndex = 7),
            Block("cs-b9", "page-cs101", BlockType.TODO, "Submit GitHub repository link to Canvas portal", isChecked = false, orderIndex = 8)
        )
        dao.insertBlocks(csBlocks.map { it.toEntity() })

        // 2. Biology Page
        val bioPage = Page(
            id = "page-bio201",
            title = "Bio 201: Cellular Respiration & ATP",
            emoji = "🔬",
            subject = "Biology",
            coverColorIndex = 1,
            isFavorite = true,
            isCollaborative = true,
            updatedAt = System.currentTimeMillis() - 7200000,
            syncStatus = SyncStatus.SYNCED,
            sharedWith = listOf("Emma Watson")
        )
        dao.insertOrUpdatePage(bioPage.toEntity())

        val bioBlocks = listOf(
            Block("bio-b1", "page-bio201", BlockType.CALLOUT, "🧪 **Lab Session**: Mitochondria electron transport chain observation this Friday at 2 PM.", extra = "🧪", orderIndex = 0),
            Block("bio-b2", "page-bio201", BlockType.HEADING_1, "The 3 Stages of Respiration", orderIndex = 1),
            Block("bio-b3", "page-bio201", BlockType.BULLET, "1. Glycolysis in the cytosol (Yields 2 ATP + 2 NADH)", orderIndex = 2),
            Block("bio-b4", "page-bio201", BlockType.BULLET, "2. Krebs (Citric Acid) Cycle in mitochondrial matrix", orderIndex = 3),
            Block("bio-b5", "page-bio201", BlockType.BULLET, "3. Oxidative Phosphorylation & ATP Synthase", orderIndex = 4),
            Block("bio-b6", "page-bio201", BlockType.FLASHCARD, "What is the final electron acceptor in aerobic respiration?", extra = "Oxygen (O2), which reduces to form water (H2O)", orderIndex = 5)
        )
        dao.insertBlocks(bioBlocks.map { it.toEntity() })

        // 3. Weekly Planner
        val planPage = Page(
            id = "page-planner",
            title = "Weekly Study Schedule & Goals",
            emoji = "📅",
            subject = "General",
            coverColorIndex = 2,
            isFavorite = false,
            isCollaborative = false,
            updatedAt = System.currentTimeMillis() - 86400000,
            syncStatus = SyncStatus.SYNCED,
            sharedWith = emptyList()
        )
        dao.insertOrUpdatePage(planPage.toEntity())
        val planBlocks = listOf(
            Block("pl-b1", "page-planner", BlockType.HEADING_1, "Study Block Milestones", orderIndex = 0),
            Block("pl-b2", "page-planner", BlockType.TODO, "Monday: 2 hours CS Graph problem practice", isChecked = true, orderIndex = 1),
            Block("pl-b3", "page-planner", BlockType.TODO, "Tuesday: Read Bio Chapter 9 on ATP synthesis", isChecked = true, orderIndex = 2),
            Block("pl-b4", "page-planner", BlockType.TODO, "Thursday: Group study review with Lucas & Emma", isChecked = false, orderIndex = 3)
        )
        dao.insertBlocks(planBlocks.map { it.toEntity() })

        // Seed Initial Tasks with Reminders
        val now = System.currentTimeMillis()
        val defaultTasks = listOf(
            TaskItem(
                id = "task-1",
                title = "Submit CS 101 AVL Tree Homework",
                description = "Upload code zip and test report before midnight",
                subject = "Computer Science",
                dueDateMillis = now + 4 * 3600 * 1000L, // in 4 hours
                priority = TaskPriority.URGENT,
                isCompleted = false,
                hasReminder = true
            ),
            TaskItem(
                id = "task-2",
                title = "Read Biology Chapter 9 (Cellular Respiration)",
                description = "Focus on ATP yields and oxidative phosphorylation equations",
                subject = "Biology",
                dueDateMillis = now + 24 * 3600 * 1000L, // tomorrow
                priority = TaskPriority.HIGH,
                isCompleted = false,
                hasReminder = true
            ),
            TaskItem(
                id = "task-3",
                title = "Physics Lab Prep: Pendulum Oscillations",
                description = "Bring scientific calculator and lab notebook",
                subject = "Physics",
                dueDateMillis = now + 48 * 3600 * 1000L,
                priority = TaskPriority.MEDIUM,
                isCompleted = false,
                hasReminder = true
            ),
            TaskItem(
                id = "task-4",
                title = "Review CS 101 BFS/DFS Flashcards",
                description = "Complete 15-minute active recall drill",
                subject = "Computer Science",
                dueDateMillis = now - 3600 * 1000L,
                priority = TaskPriority.LOW,
                isCompleted = true,
                hasReminder = false
            )
        )
        dao.insertTasks(defaultTasks.map { it.toEntity() })
        defaultTasks.forEach { reminderManager.scheduleTaskReminder(it) }

        // Seed Comments
        val defaultComments = listOf(
            CommentEntity("c-1", "page-cs101", "Lucas Chen", "👨‍💻", "Hey! I added the graph time complexity flashcard. Let me know if we should add A* search too.", now - 3600000),
            CommentEntity("c-2", "page-cs101", "Emma Watson", "👩‍🎓", "Looks great! I'll test the AVL rotation test cases on my tablet tonight.", now - 1800000)
        )
        defaultComments.forEach { dao.insertComment(it) }

        // Seed Default Student Subjects
        val defaultSubjects = listOf(
            SubjectEntity("subj-cs", "Computer Science", "💻", 0, isDefault = true),
            SubjectEntity("subj-bio", "Biology", "🔬", 1, isDefault = true),
            SubjectEntity("subj-phy", "Physics", "⚡", 2, isDefault = true),
            SubjectEntity("subj-math", "Mathematics", "📐", 3, isDefault = true),
            SubjectEntity("subj-lit", "Literature", "📖", 4, isDefault = true),
            SubjectEntity("subj-gen", "General", "📝", 5, isDefault = true)
        )
        dao.insertSubjects(defaultSubjects)

        // Seed Default Class Schedules (Mon-Fri)
        val defaultSchedules = listOf(
            ScheduleEntity(
                id = "sch-1",
                subject = "Computer Science",
                courseCode = "CS 101",
                title = "Data Structures & Algorithms",
                professor = "Prof. Marcus Thorne",
                room = "Turing Hall 302",
                dayOfWeek = 1, // Monday
                startTime = "09:00 AM",
                endTime = "10:30 AM",
                colorIndex = 0,
                notes = "Bring laptop with IDE setup. Focus on Graph traversals."
            ),
            ScheduleEntity(
                id = "sch-2",
                subject = "Mathematics",
                courseCode = "MATH 201",
                title = "Calculus & Linear Algebra",
                professor = "Dr. Elena Rostova",
                room = "Euler Hall 104",
                dayOfWeek = 1, // Monday
                startTime = "11:00 AM",
                endTime = "12:30 PM",
                colorIndex = 3,
                notes = "Weekly problem set due before class."
            ),
            ScheduleEntity(
                id = "sch-3",
                subject = "Biology",
                courseCode = "BIO 201",
                title = "Cellular Respiration & Genetics",
                professor = "Dr. Arthur Vance",
                room = "Darwin Lab 201",
                dayOfWeek = 2, // Tuesday
                startTime = "10:00 AM",
                endTime = "11:30 AM",
                colorIndex = 1,
                notes = "Wear lab coats. Group lab reports assigned."
            ),
            ScheduleEntity(
                id = "sch-4",
                subject = "Physics",
                courseCode = "PHYS 150",
                title = "Classical Mechanics & Thermodynamics",
                professor = "Dr. James Maxwell",
                room = "Newton Hall 410",
                dayOfWeek = 2, // Tuesday
                startTime = "01:30 PM",
                endTime = "03:00 PM",
                colorIndex = 2,
                notes = "Review Newton's 2nd Law derivation."
            ),
            ScheduleEntity(
                id = "sch-5",
                subject = "Computer Science",
                courseCode = "CS 101",
                title = "Data Structures Lab & Practice",
                professor = "TA Lucas Chen",
                room = "Computer Lab 4B",
                dayOfWeek = 3, // Wednesday
                startTime = "09:30 AM",
                endTime = "11:00 AM",
                colorIndex = 0,
                notes = "Live coding challenge on AVL Trees."
            ),
            ScheduleEntity(
                id = "sch-6",
                subject = "Literature",
                courseCode = "ENG 110",
                title = "Modern World Literature",
                professor = "Prof. Claire Bennett",
                room = "Library Seminar Rm 2",
                dayOfWeek = 4, // Thursday
                startTime = "02:00 PM",
                endTime = "03:30 PM",
                colorIndex = 4,
                notes = "Discussion on 20th century poetry."
            ),
            ScheduleEntity(
                id = "sch-7",
                subject = "Physics",
                courseCode = "PHYS 150",
                title = "Physics Experimental Lab",
                professor = "Dr. James Maxwell",
                room = "Physics Lab B",
                dayOfWeek = 5, // Friday
                startTime = "10:00 AM",
                endTime = "12:00 PM",
                colorIndex = 2,
                notes = "Pendulum oscillation data collection."
            )
        )
        dao.insertSchedules(defaultSchedules)

        // Seed Sample Completed Focus Sessions
        val sampleFocusSessions = listOf(
            FocusSessionEntity("foc-1", "Computer Science", 25, now - 86400000L * 2, "AVL Trees Review", "page-cs101"),
            FocusSessionEntity("foc-2", "Biology", 25, now - 86400000L, "Cell Respiration Flashcards", "page-bio201"),
            FocusSessionEntity("foc-3", "Computer Science", 25, now - 3600000L * 3, "Homework Problem Set", "page-cs101"),
            FocusSessionEntity("foc-4", "Mathematics", 25, now - 3600000L, "Calculus Practice", null)
        )
        sampleFocusSessions.forEach { dao.insertFocusSession(it) }
    }
}

// --- Mapper Extensions ---
private fun ScheduleEntity.toDomain() = ClassScheduleItem(
    id = id,
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

private fun ClassScheduleItem.toEntity() = ScheduleEntity(
    id = id,
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

private fun FocusSessionEntity.toDomain() = FocusSessionItem(
    id = id,
    subject = subject,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    tag = tag,
    notePageId = notePageId
)

private fun FocusSessionItem.toEntity() = FocusSessionEntity(
    id = id,
    subject = subject,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    tag = tag,
    notePageId = notePageId
)

private fun SubjectEntity.toDomain() = SubjectItem(
    id = id,
    name = name,
    emoji = emoji,
    colorIndex = colorIndex,
    isDefault = isDefault
)

private fun SubjectItem.toEntity() = SubjectEntity(
    id = id,
    name = name,
    emoji = emoji,
    colorIndex = colorIndex,
    isDefault = isDefault
)

private fun PageEntity.toDomain() = Page(
    id = id,
    title = title,
    emoji = emoji,
    subject = subject,
    coverColorIndex = coverColorIndex,
    isFavorite = isFavorite,
    isCollaborative = isCollaborative,
    updatedAt = updatedAt,
    syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (e: Exception) { SyncStatus.SYNCED },
    sharedWith = if (sharedWithJson.isBlank()) emptyList() else sharedWithJson.split(",")
)

private fun Page.toEntity() = PageEntity(
    id = id,
    title = title,
    emoji = emoji,
    subject = subject,
    coverColorIndex = coverColorIndex,
    isFavorite = isFavorite,
    isCollaborative = isCollaborative,
    updatedAt = updatedAt,
    syncStatus = syncStatus.name,
    sharedWithJson = sharedWith.joinToString(",")
)

private fun BlockEntity.toDomain() = Block(
    id = id,
    pageId = pageId,
    type = try { BlockType.valueOf(type) } catch (e: Exception) { BlockType.PARAGRAPH },
    content = content,
    isChecked = isChecked,
    extra = extra,
    orderIndex = orderIndex
)

private fun Block.toEntity() = BlockEntity(
    id = id,
    pageId = pageId,
    type = type.name,
    content = content,
    isChecked = isChecked,
    extra = extra,
    orderIndex = orderIndex
)

private fun TaskEntity.toDomain() = TaskItem(
    id = id,
    title = title,
    description = description,
    subject = subject,
    dueDateMillis = dueDateMillis,
    priority = try { TaskPriority.valueOf(priority) } catch (e: Exception) { TaskPriority.MEDIUM },
    isCompleted = isCompleted,
    hasReminder = hasReminder,
    syncStatus = try { SyncStatus.valueOf(syncStatus) } catch (e: Exception) { SyncStatus.SYNCED }
)

private fun TaskItem.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    subject = subject,
    dueDateMillis = dueDateMillis,
    priority = priority.name,
    isCompleted = isCompleted,
    hasReminder = hasReminder,
    syncStatus = syncStatus.name
)

private fun CommentEntity.toDomain() = PageComment(
    id = id,
    pageId = pageId,
    authorName = authorName,
    authorAvatar = authorAvatar,
    text = text,
    timestamp = timestamp
)

private fun PageComment.toEntity() = CommentEntity(
    id = id,
    pageId = pageId,
    authorName = authorName,
    authorAvatar = authorAvatar,
    text = text,
    timestamp = timestamp
)
