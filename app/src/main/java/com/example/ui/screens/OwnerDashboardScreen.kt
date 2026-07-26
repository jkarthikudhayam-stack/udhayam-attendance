package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserEntity
import com.example.ui.components.AddStaffDialog
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.UserProfileContent
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TealPrimary
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OwnerDashboardScreen(
    owner: UserEntity,
    authViewModel: AuthViewModel,
    attendanceViewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val users by attendanceViewModel.users.collectAsState()
    val allAttendance by attendanceViewModel.allAttendance.collectAsState()
    val userMessage by attendanceViewModel.userMessage.collectAsState()

    val todayStr = attendanceViewModel.todayDateString
    val todayRecords = remember(allAttendance, todayStr) {
        allAttendance.filter { it.date == todayStr }
    }

    val totalStaff = users.size
    val presentToday = todayRecords.count { it.status == "PRESENT" || it.status == "LATE" }
    val attendanceRate = if (totalStaff > 0) (presentToday * 100) / totalStaff else 0

    val estimatedDailyPayroll = remember(users, presentToday) {
        users.sumOf { it.hourlyRate * 8.0 }
    }

    var selectedTab by remember { mutableStateOf(0) }
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredAttendance = remember(allAttendance, searchQuery) {
        if (searchQuery.isBlank()) allAttendance
        else allAttendance.filter {
            it.userName.contains(searchQuery, ignoreCase = true) ||
                    it.siteLocation.contains(searchQuery, ignoreCase = true) ||
                    it.date.contains(searchQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Executive Header
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = TealPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        profileImageUri = owner.profileImageUri,
                        fullName = owner.fullName,
                        sizeDp = 48,
                        showCameraBadge = true,
                        onClick = { showProfileDialog = true }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = owner.fullName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RoleBadge(role = owner.role)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Business Owner Portal | $todayStr",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                IconButton(
                    onClick = { authViewModel.logout() },
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout",
                        tint = Color.White
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Executive Stat Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Active Workforce",
                        value = "$totalStaff Members",
                        icon = Icons.Default.People,
                        iconTint = TealPrimary,
                        accentBg = TealPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Attendance Rate",
                        value = "$attendanceRate%",
                        icon = Icons.Default.HowToReg,
                        iconTint = Color(0xFF047857),
                        accentBg = EmeraldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f),
                        subtitle = "$presentToday / $totalStaff Present"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                StatCard(
                    title = "Estimated Daily Payroll Budget",
                    value = "₹${"%.2f".format(estimatedDailyPayroll)}",
                    icon = Icons.Default.AttachMoney,
                    iconTint = Color(0xFFB45309),
                    accentBg = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth(),
                    subtitle = "Based on standard 8-hr shift wage calculations"
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (userMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = userMessage!!,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealPrimary
                        )
                    }
                }

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TealPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Directory (${users.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Audit Reports", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Profile", fontWeight = FontWeight.Bold) },
                        icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (selectedTab == 0) {
                item {
                    Button(
                        onClick = { showAddStaffDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Staff Member")
                    }
                }

                items(users) { staff ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = staff.fullName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    RoleBadge(role = staff.role)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Dept: ${staff.department} | PIN: ${staff.pin}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Email: ${staff.email} | Wage: ₹${staff.hourlyRate}/hr",
                                    fontSize = 11.sp,
                                    color = TealPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            if (staff.role != "OWNER") {
                                IconButton(
                                    onClick = {
                                        attendanceViewModel.deactivateStaff(staff.id, staff.fullName)
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Deactivate",
                                        tint = RedDanger.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search staff, site, date...") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (filteredAttendance.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No attendance logs found matching search.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(filteredAttendance) { log ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(14.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = log.userName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val inTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.punchInTime))
                                    val outTime = if (log.punchOutTime != null)
                                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(log.punchOutTime))
                                    else "Active"

                                    Text(
                                        text = "Date: ${log.date} | In: $inTime | Out: $outTime",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Location: ${log.siteLocation} ${if (!log.notes.isNull_or_empty()) "| ${log.notes}" else ""}",
                                        fontSize = 11.sp,
                                        color = TealPrimary
                                    )
                                }

                                StatusBadge(status = log.status)
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                item {
                    UserProfileContent(
                        user = owner,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    if (showAddStaffDialog) {
        AddStaffDialog(
            onDismiss = { showAddStaffDialog = false },
            onAddStaff = { fullName, email, pin, role, department, shiftHours, phone, hourlyRate ->
                attendanceViewModel.addNewStaff(
                    fullName = fullName,
                    email = email,
                    pin = pin,
                    role = role,
                    department = department,
                    shiftHours = shiftHours,
                    phone = phone,
                    hourlyRate = hourlyRate
                )
            }
        )
    }

    if (showProfileDialog) {
        UserProfileDialog(
            user = owner,
            authViewModel = authViewModel,
            onDismiss = { showProfileDialog = false }
        )
    }
}

private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
