package com.example.model

enum class BlockType {
    PARAGRAPH,
    HEADING_1,
    HEADING_2,
    TODO,
    BULLET,
    CALLOUT,
    FLASHCARD,
    CODE_SNIPPET,
    QUOTE,
    IMAGE,
    ATTACHMENT_PDF
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class SubjectItem(
    val id: String,
    val name: String,
    val emoji: String = "📚",
    val colorIndex: Int = 0,
    val isDefault: Boolean = false
)

data class Block(
    val id: String,
    val pageId: String,
    val type: BlockType,
    val content: String,
    val isChecked: Boolean = false,
    val extra: String = "", // e.g. Flashcard answer, Callout emoji, or code language
    val orderIndex: Int = 0
)

enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    CONFLICT,
    OFFLINE_ONLY
}

data class Page(
    val id: String,
    val title: String,
    val emoji: String = "📝",
    val subject: String = "General",
    val coverColorIndex: Int = 0,
    val isFavorite: Boolean = false,
    val isCollaborative: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val sharedWith: List<String> = emptyList()
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

data class TaskItem(
    val id: String,
    val title: String,
    val description: String = "",
    val subject: String = "General",
    val dueDateMillis: Long = System.currentTimeMillis() + 86400000L,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

data class Collaborator(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val currentActivity: String,
    val role: String = "Editor",
    val isOnline: Boolean = true
)

data class PageComment(
    val id: String,
    val pageId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WorkspaceDevice(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val lastSyncedTime: Long,
    val isCurrentDevice: Boolean = false
)

enum class PomodoroTimerMode {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK
}

data class ClassScheduleItem(
    val id: String,
    val subject: String,
    val courseCode: String = "",
    val title: String,
    val professor: String = "",
    val room: String = "",
    val dayOfWeek: Int, // 1 = Monday, 2 = Tuesday, ... 7 = Sunday
    val startTime: String, // e.g. "09:00 AM"
    val endTime: String, // e.g. "10:30 AM"
    val colorIndex: Int = 0,
    val notes: String = ""
)

data class FocusSessionItem(
    val id: String,
    val subject: String,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "Focus Session",
    val notePageId: String? = null
)

