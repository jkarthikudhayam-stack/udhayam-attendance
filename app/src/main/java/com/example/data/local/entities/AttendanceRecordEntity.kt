package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val userName: String,
    val date: String, // "YYYY-MM-DD"
    val punchInTime: Long, // epoch millis
    val punchOutTime: Long? = null, // epoch millis
    val breakMinutes: Int = 0,
    val status: String, // "PRESENT", "LATE", "HALF_DAY", "ABSENT", "ON_BREAK"
    val siteLocation: String = "Main Office", // "Main Office", "Warehouse A", "Field Site", "Remote"
    val notes: String? = null,
    val verifiedByManager: Boolean = true
)
