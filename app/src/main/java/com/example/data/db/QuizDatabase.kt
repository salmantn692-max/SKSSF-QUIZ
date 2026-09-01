package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.*

@Database(
    entities = [
        QuizEvent::class,
        QuizStage::class,
        QuizQuestion::class,
        Student::class,
        StudentSubmission::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuizDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getInstance(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_master_admin.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
