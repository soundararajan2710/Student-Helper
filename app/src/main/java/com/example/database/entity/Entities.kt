package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BlockType
import com.example.model.SyncStatus
import com.example.model.TaskPriority

@Entity(tableName = "pages")
data class PageEntity(
    @PrimaryKey val id: String,
    val title: String,
    val emoji: String,
    val subject: String,
    val coverColorIndex: Int,
    val isFavorite: Boolean,
    val isCollaborative: Boolean,
    val updatedAt: Long,
    val syncStatus: String, // String representation of SyncStatus
    val sharedWithJson: String // Serialized comma/json string
)

@Entity(tableName = "blocks")
data class BlockEntity(
    @PrimaryKey val id: String,
    val pageId: String,
    val type: String, // String representation of BlockType
    val content: String,
    val isChecked: Boolean,
    val extra: String,
    val orderIndex: Int
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val dueDateMillis: Long,
    val priority: String, // TaskPriority
    val isCompleted: Boolean,
    val hasReminder: Boolean,
    val syncStatus: String
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val pageId: String,
    val authorName: String,
    val authorAvatar: String,
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val colorIndex: Int,
    val isDefault: Boolean
)

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    val entityType: String, // "PAGE", "BLOCK", "TASK", "COMMENT"
    val entityId: String,
    val action: String, // "INSERT", "UPDATE", "DELETE"
    val timestamp: Long,
    val payloadJson: String
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val courseCode: String,
    val title: String,
    val professor: String,
    val room: String,
    val dayOfWeek: Int, // 1=Mon .. 7=Sun
    val startTime: String,
    val endTime: String,
    val colorIndex: Int,
    val notes: String
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val tag: String,
    val notePageId: String?
)

