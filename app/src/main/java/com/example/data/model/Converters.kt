package com.example.data.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromStageType(value: StageType): String {
        return value.name
    }

    @TypeConverter
    fun toStageType(value: String): StageType {
        return try {
            StageType.valueOf(value)
        } catch (e: Exception) {
            StageType.MCQ
        }
    }

    @TypeConverter
    fun fromStudentStatus(value: StudentStatus): String {
        return value.name
    }

    @TypeConverter
    fun toStudentStatus(value: String): StudentStatus {
        return try {
            StudentStatus.valueOf(value)
        } catch (e: Exception) {
            StudentStatus.REGISTERED
        }
    }
}
