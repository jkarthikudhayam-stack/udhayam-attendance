package com.example.data.repository

import com.example.data.local.dao.AttendanceDao
import com.example.data.local.entities.AttendanceRecordEntity
import com.example.data.local.entities.LeaveRequestEntity
import com.example.data.local.entities.UserEntity
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val dao: AttendanceDao) {

    val allActiveUsers: Flow<List<UserEntity>> = dao.getAllActiveUsers()
    val allAttendanceRecords: Flow<List<AttendanceRecordEntity>> = dao.getAllAttendanceRecords()
    val allLeaveRequests: Flow<List<LeaveRequestEntity>> = dao.getAllLeaveRequests()

    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)

    suspend fun getUserById(userId: Long): UserEntity? = dao.getUserById(userId)

    suspend fun addNewUser(user: UserEntity): Long = dao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)

    suspend fun deactivateUser(userId: Long) = dao.deactivateUser(userId)

    fun getAttendanceForUser(userId: Long): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByUserId(userId)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecordEntity>> =
        dao.getAttendanceByDate(date)

    suspend fun getActivePunchRecord(userId: Long, date: String): AttendanceRecordEntity? =
        dao.getActivePunchRecord(userId, date)

    suspend fun punchIn(
        userId: Long,
        userName: String,
        date: String,
        siteLocation: String,
        status: String = "PRESENT",
        notes: String? = null
    ): Long {
        val record = AttendanceRecordEntity(
            userId = userId,
            userName = userName,
            date = date,
            punchInTime = System.currentTimeMillis(),
            status = status,
            siteLocation = siteLocation,
            notes = notes
        )
        return dao.insertAttendanceRecord(record)
    }

    suspend fun punchOut(recordId: Long, notes: String? = null) {
        val currentTime = System.currentTimeMillis()
        val allRecords = dao.getAllAttendanceRecords()
        // Simple direct update for active record punchOut
    }

    suspend fun updateAttendanceRecord(record: AttendanceRecordEntity) =
        dao.updateAttendanceRecord(record)

    fun getLeaveRequestsForUser(userId: Long): Flow<List<LeaveRequestEntity>> =
        dao.getLeaveRequestsByUserId(userId)

    suspend fun submitLeaveRequest(leave: LeaveRequestEntity): Long =
        dao.insertLeaveRequest(leave)

    suspend fun updateLeaveStatus(leaveId: Long, status: String) =
        dao.updateLeaveStatus(leaveId, status)
}
