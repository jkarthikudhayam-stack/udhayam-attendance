package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.AttendanceRepository
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ManagerDashboardScreen
import com.example.ui.screens.OwnerDashboardScreen
import com.example.ui.screens.StaffDashboardScreen
import com.example.ui.theme.UdhayamAttendanceTheme
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.AttendanceViewModelFactory
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.AuthViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = AttendanceRepository(database.attendanceDao())

        val authViewModel: AuthViewModel by viewModels { AuthViewModelFactory(repository) }
        val attendanceViewModel: AttendanceViewModel by viewModels { AttendanceViewModelFactory(repository) }

        setContent {
            UdhayamAttendanceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(
                        authViewModel = authViewModel,
                        attendanceViewModel = attendanceViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainNavigation(
    authViewModel: AuthViewModel,
    attendanceViewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    when (val user = currentUser) {
        null -> LoginScreen(authViewModel = authViewModel, modifier = modifier)
        else -> when (user.role.uppercase()) {
            "OWNER" -> OwnerDashboardScreen(
                owner = user,
                authViewModel = authViewModel,
                attendanceViewModel = attendanceViewModel,
                modifier = modifier
            )
            "MANAGER" -> ManagerDashboardScreen(
                manager = user,
                authViewModel = authViewModel,
                attendanceViewModel = attendanceViewModel,
                modifier = modifier
            )
            else -> StaffDashboardScreen(
                user = user,
                authViewModel = authViewModel,
                attendanceViewModel = attendanceViewModel,
                modifier = modifier
            )
        }
    }
}

