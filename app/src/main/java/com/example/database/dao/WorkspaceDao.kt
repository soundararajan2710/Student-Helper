package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.database.entity.BlockEntity
import com.example.database.entity.CommentEntity
import com.example.database.entity.FocusSessionEntity
import com.example.database.entity.PageEntity
import com.example.database.entity.ScheduleEntity
import com.example.database.entity.SubjectEntity
import com.example.database.entity.SyncQueueEntity
import com.example.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkspaceDao {

    // --- Pages ---
    @Query("SELECT * FROM pages ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllPagesFlow(): Flow<List<PageEntity>>

    @Query("SELECT * FROM pages WHERE id = :pageId")
    fun getPageByIdFlow(pageId: String): Flow<PageEntity?>

    @Query("SELECT * FROM pages WHERE id = :pageId")
    suspend fun getPageById(pageId: String): PageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePage(page: PageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPages(pages: List<PageEntity>)

    @Query("DELETE FROM pages WHERE id = :pageId")
    suspend fun deletePage(pageId: String)

    // --- Blocks ---
    @Query("SELECT * FROM blocks WHERE pageId = :pageId ORDER BY orderIndex ASC")
    fun getBlocksForPageFlow(pageId: String): Flow<List<BlockEntity>>

    @Query("SELECT * FROM blocks WHERE pageId = :pageId ORDER BY orderIndex ASC")
    suspend fun getBlocksForPage(pageId: String): List<BlockEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBlock(block: BlockEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<BlockEntity>)

    @Query("DELETE FROM blocks WHERE id = :blockId")
    suspend fun deleteBlock(blockId: String)

    @Query("DELETE FROM blocks WHERE pageId = :pageId")
    suspend fun deleteBlocksForPage(pageId: String)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateMillis ASC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: String)

    // --- Comments ---
    @Query("SELECT * FROM comments WHERE pageId = :pageId ORDER BY timestamp ASC")
    fun getCommentsForPageFlow(pageId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY isDefault DESC, name ASC")
    fun getAllSubjectsFlow(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubject(subjectId: String)

    // --- Sync Queue ---
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getSyncQueueFlow(): Flow<List<SyncQueueEntity>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getPendingSyncItems(): List<SyncQueueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueSyncItem(item: SyncQueueEntity)

    @Query("DELETE FROM sync_queue WHERE id = :syncId")
    suspend fun removeSyncItem(syncId: String)

    @Query("DELETE FROM sync_queue")
    suspend fun clearSyncQueue()

    // --- Schedules ---
    @Query("SELECT * FROM schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSchedulesForDayFlow(dayOfWeek: Int): Flow<List<ScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSchedule(schedule: ScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Query("DELETE FROM schedules WHERE id = :scheduleId")
    suspend fun deleteSchedule(scheduleId: String)

    // --- Focus Sessions ---
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllFocusSessionsFlow(): Flow<List<FocusSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSessionEntity)

    @Query("DELETE FROM focus_sessions WHERE id = :sessionId")
    suspend fun deleteFocusSession(sessionId: String)

    @Query("DELETE FROM focus_sessions")
    suspend fun clearAllFocusSessions()
}

