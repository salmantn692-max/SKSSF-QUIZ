package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    // --- Quiz Event Branding ---
    @Query("SELECT * FROM quiz_events WHERE id = 1 LIMIT 1")
    fun getQuizEvent(): Flow<QuizEvent?>

    @Query("SELECT * FROM quiz_events WHERE id = 1 LIMIT 1")
    suspend fun getQuizEventDirect(): QuizEvent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuizEvent(event: QuizEvent)

    // --- Quiz Stages ---
    @Query("SELECT * FROM quiz_stages ORDER BY orderIndex ASC, stageNumber ASC")
    fun getAllStages(): Flow<List<QuizStage>>

    @Query("SELECT * FROM quiz_stages WHERE id = :id LIMIT 1")
    suspend fun getStageById(id: Long): QuizStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStage(stage: QuizStage): Long

    @Update
    suspend fun updateStage(stage: QuizStage)

    @Delete
    suspend fun deleteStage(stage: QuizStage)

    @Query("DELETE FROM quiz_stages WHERE id = :stageId")
    suspend fun deleteStageById(stageId: Long)

    // --- Quiz Questions ---
    @Query("SELECT * FROM quiz_questions ORDER BY orderIndex ASC, id ASC")
    fun getAllQuestions(): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE stageId = :stageId ORDER BY orderIndex ASC, id ASC")
    fun getQuestionsByStage(stageId: Long): Flow<List<QuizQuestion>>

    @Query("SELECT * FROM quiz_questions WHERE stageId = :stageId ORDER BY orderIndex ASC, id ASC")
    suspend fun getQuestionsByStageDirect(stageId: Long): List<QuizQuestion>

    @Query("SELECT * FROM quiz_questions WHERE id = :questionId LIMIT 1")
    suspend fun getQuestionById(questionId: Long): QuizQuestion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuizQuestion): Long

    @Update
    suspend fun updateQuestion(question: QuizQuestion)

    @Delete
    suspend fun deleteQuestion(question: QuizQuestion)

    @Query("DELETE FROM quiz_questions WHERE id = :questionId")
    suspend fun deleteQuestionById(questionId: Long)

    @Query("DELETE FROM quiz_questions WHERE stageId = :stageId")
    suspend fun deleteQuestionsByStageId(stageId: Long)

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY totalScore DESC, registeredAt DESC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Query("SELECT * FROM students WHERE registerNumber = :regNo LIMIT 1")
    suspend fun getStudentByRegNo(regNo: String): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudentById(studentId: Long)

    @Query("DELETE FROM students")
    suspend fun clearAllStudents()

    // --- Student Submissions ---
    @Query("SELECT * FROM student_submissions ORDER BY timestamp DESC")
    fun getAllSubmissions(): Flow<List<StudentSubmission>>

    @Query("SELECT * FROM student_submissions WHERE studentId = :studentId ORDER BY timestamp ASC")
    fun getSubmissionsForStudent(studentId: Long): Flow<List<StudentSubmission>>

    @Query("SELECT * FROM student_submissions WHERE stageId = :stageId ORDER BY timestamp DESC")
    fun getSubmissionsForStage(stageId: Long): Flow<List<StudentSubmission>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: StudentSubmission): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<StudentSubmission>)

    @Query("DELETE FROM student_submissions")
    suspend fun clearAllSubmissions()

    @Query("DELETE FROM student_submissions WHERE studentId = :studentId")
    suspend fun deleteSubmissionsForStudent(studentId: Long)
}
