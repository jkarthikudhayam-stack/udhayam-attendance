package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val email: String,
    val pin: String,
    val role: String, // "OWNER", "MANAGER", "STAFF"
    val department: String,
    val shiftHours: String = "09:00 AM - 06:00 PM",
    val phone: String = "",
    val hourlyRate: Double = 150.0,
    val joinDate: String = "2024-01-15",
    val isActive: Boolean = true,
    val profileImageUri: String? = null
)
