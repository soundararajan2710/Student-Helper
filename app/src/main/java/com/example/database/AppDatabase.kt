package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.WorkspaceDao
import com.example.database.entity.BlockEntity
import com.example.database.entity.CommentEntity
import com.example.database.entity.FocusSessionEntity
import com.example.database.entity.PageEntity
import com.example.database.entity.ScheduleEntity
import com.example.database.entity.SubjectEntity
import com.example.database.entity.SyncQueueEntity
import com.example.database.entity.TaskEntity

@Database(
    entities = [
        PageEntity::class,
        BlockEntity::class,
        TaskEntity::class,
        CommentEntity::class,
        SubjectEntity::class,
        SyncQueueEntity::class,
        ScheduleEntity::class,
        FocusSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workspaceDao(): WorkspaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_notion_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
