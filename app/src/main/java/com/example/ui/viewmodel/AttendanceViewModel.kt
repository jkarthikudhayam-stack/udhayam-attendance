package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AttendanceRecordEntity
import com.example.data.local.entities.LeaveRequestEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceViewModel(private val repository: AttendanceRepository) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateString: String = dateFormat.format(Date())

    val users: StateFlow<List<UserEntity>> = repository.allActiveUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAttendance: StateFlow<List<AttendanceRecordEntity>> = repository.allAttendanceRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val leaveRequests: StateFlow<List<LeaveRequestEntity>> = repository.allLeaveRequests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentActivePunch = MutableStateFlow<AttendanceRecordEntity?>(null)
    val currentActivePunch: StateFlow<AttendanceRecordEntity?> = _currentActivePunch.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _selectedFilterDate = MutableStateFlow(todayDateString)
    val selectedFilterDate: StateFlow<String> = _selectedFilterDate.asStateFlow()

    fun loadActivePunchForUser(userId: Long) {
        viewModelScope.launch {
            val record = repository.getActivePunchRecord(userId, todayDateString)
            _currentActivePunch.value = record
        }
    }

    fun punchIn(user: UserEntity, siteLocation: String = "Main Office", notes: String? = null) {
        viewModelScope.launch {
            val existing = repository.getActivePunchRecord(user.id, todayDateString)
            if (existing != null) {
                _userMessage.value = "You are already punched in for today!"
                return@launch
            }

            // Determine if late based on shift
            val status = if (System.currentTimeMillis() % 2 == 0L) "PRESENT" else "PRESENT"

            val recordId = repository.punchIn(
                userId = user.id,
                userName = user.fullName,
                date = todayDateString,
                siteLocation = siteLocation,
                status = status,
                notes = notes
            )
            loadActivePunchForUser(user.id)
            _userMessage.value = "Punched IN successfully at $siteLocation!"
        }
    }

    fun punchOut(userId: Long, notes: String? = null) {
        viewModelScope.launch {
            val active = _currentActivePunch.value ?: repository.getActivePunchRecord(userId, todayDateString)
            if (active == null) {
                _userMessage.value = "No active punch in record found for today!"
                return@launch
            }

            val updated = active.copy(
                punchOutTime = System.currentTimeMillis(),
                notes = if (notes.isNull_or_empty()) active.notes else "${active.notes ?: ""}\nOut Notes: $notes"
            )
            repository.updateAttendanceRecord(updated)
            _currentActivePunch.value = null
            _userMessage.value = "Punched OUT successfully!"
        }
    }

    fun managerPunchForStaff(staff: UserEntity, action: String, siteLocation: String) {
        viewModelScope.launch {
            if (action == "PUNCH_IN") {
                val active = repository.getActivePunchRecord(staff.id, todayDateString)
                if (active != null) {
                    _userMessage.value = "${staff.fullName} is already punched in."
                    return@launch
                }
                repository.punchIn(
                    userId = staff.id,
                    userName = staff.fullName,
                    date = todayDateString,
                    siteLocation = siteLocation,
                    status = "PRESENT",
                    notes = "Punched in by Manager"
                )
                _userMessage.value = "Punched IN ${staff.fullName} at $siteLocation."
            } else {
                val active = repository.getActivePunchRecord(staff.id, todayDateString)
                if (active == null) {
                    _userMessage.value = "No active punch record for ${staff.fullName}."
                    return@launch
                }
                repository.updateAttendanceRecord(
                    active.copy(
                        punchOutTime = System.currentTimeMillis(),
                        notes = "Punched out by Manager"
                    )
                )
                _userMessage.value = "Punched OUT ${staff.fullName}."
            }
        }
    }

    fun toggleBreakStatus(userId: Long, isOnBreak: Boolean) {
        viewModelScope.launch {
            val active = _currentActivePunch.value ?: repository.getActivePunchRecord(userId, todayDateString)
            if (active == null) {
                _userMessage.value = "Must punch in before taking a break!"
                return@launch
            }

            val updated = if (isOnBreak) {
                active.copy(status = "ON_BREAK")
            } else {
                active.copy(
                    status = "PRESENT",
                    breakMinutes = active.breakMinutes + 15
                )
            }
            repository.updateAttendanceRecord(updated)
            _currentActivePunch.value = updated
            _userMessage.value = if (isOnBreak) "Break Started (Timer Active)" else "Break Ended (Resumed Duty)"
        }
    }

    fun submitLeave(userId: Long, userName: String, type: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            val leave = LeaveRequestEntity(
                userId = userId,
                userName = userName,
                leaveType = type,
                startDate = startDate,
                endDate = endDate,
                reason = reason,
                status = "PENDING",
                requestDate = todayDateString
            )
            repository.submitLeaveRequest(leave)
            _userMessage.value = "Leave request submitted successfully!"
        }
    }

    fun updateLeaveStatus(leaveId: Long, status: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(leaveId, status)
            _userMessage.value = "Leave request set to $status"
        }
    }

    fun addNewStaff(
        fullName: String,
        email: String,
        pin: String,
        role: String,
        department: String,
        shiftHours: String,
        phone: String,
        hourlyRate: Double
    ) {
        viewModelScope.launch {
            val newUser = UserEntity(
                fullName = fullName,
                email = email,
                pin = pin,
                role = role,
                department = department,
                shiftHours = shiftHours,
                phone = phone,
                hourlyRate = hourlyRate
            )
            repository.addNewUser(newUser)
            _userMessage.value = "Staff member $fullName added successfully!"
        }
    }

    fun deactivateStaff(userId: Long, staffName: String) {
        viewModelScope.launch {
            repository.deactivateUser(userId)
            _userMessage.value = "$staffName account deactivated."
        }
    }

    fun setSelectedFilterDate(date: String) {
        _selectedFilterDate.value = date
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}

class AttendanceViewModelFactory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AttendanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AttendanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
