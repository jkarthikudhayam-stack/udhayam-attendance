package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AttendanceRecordEntity
import com.example.data.local.entities.LeaveRequestEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE isActive = 1 ORDER BY fullName ASC")
    fun getAllActiveUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    suspend fun deactivateUser(userId: Long)

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records ORDER BY punchInTime DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE userId = :userId ORDER BY punchInTime DESC")
    fun getAttendanceByUserId(userId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY punchInTime DESC")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE userId = :userId AND date = :date AND punchOutTime IS NULL ORDER BY punchInTime DESC LIMIT 1")
    suspend fun getActivePunchRecord(userId: Long, date: String): AttendanceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecordEntity): Long

    @Update
    suspend fun updateAttendanceRecord(record: AttendanceRecordEntity)

    // --- Leave Requests ---
    @Query("SELECT * FROM leave_requests ORDER BY id DESC")
    fun getAllLeaveRequests(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE userId = :userId ORDER BY id DESC")
    fun getLeaveRequestsByUserId(userId: Long): Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaveRequest(leave: LeaveRequestEntity): Long

    @Query("UPDATE leave_requests SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: String)

    // Initial count helper
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUsersCount(): Int
}
