package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class StageType {
    MCQ,
    IMAGE_BASED,
    AUDIO_BASED
}

enum class StudentStatus {
    REGISTERED,
    IN_PROGRESS,
    COMPLETED
}

@Entity(tableName = "quiz_events")
data class QuizEvent(
    @PrimaryKey val id: Long = 1L,
    val title: String = "National Science & Heritage Fest 2026",
    val subtitle: String = "Multi-Stage Quiz Championship for Schools & Colleges",
    val organizer: String = "Department of Academic Excellence",
    val themeColorHex: String = "#4F46E5", // Indigo
    val defaultTimerSeconds: Int = 30,
    val passingScorePercentage: Int = 50,
    val liveActiveStageId: Long = 1L,
    val isLiveQuizRunning: Boolean = false,
    val liveActiveQuestionIndex: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_stages")
data class QuizStage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val stageNumber: Int,
    val title: String,
    val description: String,
    val stageType: StageType,
    val timerSeconds: Int = 30,
    val totalPointsWeight: Int = 100,
    val isLocked: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "quiz_questions")
data class QuizQuestion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val stageId: Long,
    val questionText: String,
    val questionType: StageType,
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "A", // "A", "B", "C", "D" or direct answer string
    val mediaUri: String = "",       // Image URI/sample key or Audio URI/sample key
    val mediaCaption: String = "",
    val points: Int = 10,
    val timerSeconds: Int = 30,
    val explanation: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val registerNumber: String,
    val departmentOrGrade: String,
    val status: StudentStatus = StudentStatus.REGISTERED,
    val totalScore: Int = 0,
    val stagesCompleted: Int = 0,
    val timeSpentSeconds: Int = 0,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "student_submissions")
data class StudentSubmission(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val studentId: Long,
    val stageId: Long,
    val questionId: Long,
    val studentName: String,
    val registerNumber: String,
    val stageTitle: String,
    val questionText: String,
    val selectedAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val timeSpentSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)
