package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AttendanceRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loginWithPin(email: String, pin: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _loginError.value = null
            try {
                val user = repository.getUserByEmail(email)
                if (user == null) {
                    _loginError.value = "User not found with email: $email"
                } else if (user.pin != pin) {
                    _loginError.value = "Invalid PIN. Please try again."
                } else if (!user.isActive) {
                    _loginError.value = "Account is inactive. Contact Administrator."
                } else {
                    _currentUser.value = user
                }
            } catch (e: Exception) {
                _loginError.value = "Login error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun quickSelectRole(role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val email = when (role.uppercase()) {
                "OWNER" -> "owner@udhayam.com"
                "MANAGER" -> "manager@udhayam.com"
                else -> "staff@udhayam.com"
            }
            val user = repository.getUserByEmail(email)
            if (user != null) {
                _currentUser.value = user
                _loginError.value = null
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
    }

    fun updateProfilePicture(imageUri: String) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(profileImageUri = imageUri)
        viewModelScope.launch {
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun updateProfileDetails(fullName: String, phone: String, department: String) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(
            fullName = fullName.ifBlank { user.fullName },
            phone = phone.ifBlank { user.phone },
            department = department.ifBlank { user.department }
        )
        viewModelScope.launch {
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun clearError() {
        _loginError.value = null
    }
}

class AuthViewModelFactory(private val repository: AttendanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
