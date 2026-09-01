package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.StageType
import com.example.data.model.Student
import com.example.data.model.StudentSubmission
import com.example.data.repository.QuizRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Quiz Admin", appName)
    }

    @Test
    fun `verify stage type names`() {
        assertEquals("MCQ", StageType.MCQ.name)
        assertEquals("IMAGE_BASED", StageType.IMAGE_BASED.name)
        assertEquals("AUDIO_BASED", StageType.AUDIO_BASED.name)
    }

    @Test
    fun `verify student score and status`() {
        val student = Student(
            id = 1L,
            name = "Aarav Sharma",
            registerNumber = "REG2026-001",
            departmentOrGrade = "Physics Dept",
            totalScore = 45,
            timeSpentSeconds = 54
        )
        assertEquals("Aarav Sharma", student.name)
        assertEquals(45, student.totalScore)
    }
}
