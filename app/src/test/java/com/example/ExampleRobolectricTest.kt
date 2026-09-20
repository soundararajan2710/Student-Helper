package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.database.AppDatabase
import com.example.database.entity.FocusSessionEntity
import com.example.database.entity.ScheduleEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Student Helper", appName)
    }

    @Test
    fun `insert and query weekly schedule`() = runBlocking {
        val scheduleId = UUID.randomUUID().toString()
        val item = ScheduleEntity(
            id = scheduleId,
            subject = "Computer Science",
            courseCode = "CS 101",
            title = "Data Structures",
            professor = "Dr. Turing",
            room = "Room 304",
            dayOfWeek = 1,
            startTime = "09:00 AM",
            endTime = "10:30 AM",
            colorIndex = 0,
            notes = "Bring laptop"
        )
        database.workspaceDao().insertOrUpdateSchedule(item)

        val schedules = database.workspaceDao().getAllSchedulesFlow().first()
        assertEquals(1, schedules.size)
        assertEquals("Data Structures", schedules[0].title)
        assertEquals("CS 101", schedules[0].courseCode)
    }

    @Test
    fun `insert and query focus session records`() = runBlocking {
        val sessionId = UUID.randomUUID().toString()
        val session = FocusSessionEntity(
            id = sessionId,
            subject = "Mathematics",
            durationMinutes = 25,
            timestamp = System.currentTimeMillis(),
            tag = "Calculus Problem Set",
            notePageId = null
        )
        database.workspaceDao().insertFocusSession(session)

        val sessions = database.workspaceDao().getAllFocusSessionsFlow().first()
        assertEquals(1, sessions.size)
        assertEquals(25, sessions[0].durationMinutes)
        assertEquals("Mathematics", sessions[0].subject)
    }

    @Test
    fun `gemini service generates structured note summary`() = runBlocking {
        val service = com.example.ai.GeminiAiService()
        val result = service.summarizeNote("Binary Search Trees", "Binary search tree has left subtree with keys less than root and right subtree with keys greater than root.")
        assertNotNull(result)
        val text = result.getOrNull()
        assertNotNull(text)
        org.junit.Assert.assertTrue(text!!.contains("Binary Search Trees") || text.contains("Core Concept"))
    }

    @Test
    fun `gemini service generates practice quiz`() = runBlocking {
        val service = com.example.ai.GeminiAiService()
        val result = service.generateQuiz("Photosynthesis", "Photosynthesis converts light energy into chemical energy.")
        val quiz = result.getOrNull()
        assertNotNull(quiz)
        org.junit.Assert.assertTrue(quiz!!.contains("Q1") || quiz.contains("Quiz"))
    }

    @Test
    fun `gemini service generates weekly study plan`() = runBlocking {
        val service = com.example.ai.GeminiAiService()
        val result = service.generateStudyPlan("CS 101: 9am-10:30am", "Math Homework Due Friday")
        val plan = result.getOrNull()
        assertNotNull(plan)
        org.junit.Assert.assertTrue(plan!!.contains("Study Plan") || plan.contains("Pomodoro"))
    }
}

