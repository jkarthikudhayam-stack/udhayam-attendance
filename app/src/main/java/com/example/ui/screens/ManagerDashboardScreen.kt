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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.RoleBadge
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.UserAvatar
import com.example.ui.components.UserProfileContent
import com.example.ui.components.UserProfileDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagerDashboardScreen(
    manager: UserEntity,
    authViewModel: AuthViewModel,
    attendanceViewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val users by attendanceViewModel.users.collectAsState()
    val allAttendance by attendanceViewModel.allAttendance.collectAsState()
    val leaveRequests by attendanceViewModel.leaveRequests.collectAsState()
    val userMessage by attendanceViewModel.userMessage.collectAsState()

    val staffMembers = remember(users) {
        users.filter { it.role == "STAFF" }
    }

    val todayStr = attendanceViewModel.todayDateString
    val todayRecords = remember(allAttendance, todayStr) {
        allAttendance.filter { it.date == todayStr }
    }

    val presentCount = todayRecords.count { it.status == "PRESENT" || it.status == "LATE" }
    val onBreakCount = todayRecords.count { it.status == "ON_BREAK" }
    val absentCount = staffMembers.size - presentCount

    var selectedTab by remember { mutableStateOf(0) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Quick Manager Punch State
    var selectedStaffForPunch by remember { mutableStateOf<UserEntity?>(null) }
    var selectedSiteLocation by remember { mutableStateOf("Main Office") }
    var staffDropdownExpanded by remember { mutableStateOf(false) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }

    val siteLocations = listOf("Main Office", "Warehouse A", "Field Site", "Remote Work")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Manager Header
        Card(
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = TealSecondary),
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
                        profileImageUri = manager.profileImageUri,
                        fullName = manager.fullName,
                        sizeDp = 48,
                        showCameraBadge = true,
                        onClick = { showProfileDialog = true }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = manager.fullName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            RoleBadge(role = manager.role)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Dept: ${manager.department} | Date: $todayStr",
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

                // Stat Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Team Staff",
                        value = staffMembers.size.toString(),
                        icon = Icons.Default.People,
                        iconTint = TealPrimary,
                        accentBg = TealPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Present Today",
                        value = presentCount.toString(),
                        icon = Icons.Default.HowToReg,
                        iconTint = Color(0xFF047857),
                        accentBg = EmeraldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "On Break",
                        value = onBreakCount.toString(),
                        icon = Icons.Default.Coffee,
                        iconTint = AmberWarning,
                        accentBg = AmberWarning.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Absent / Unpunched",
                        value = absentCount.coerceAtLeast(0).toString(),
                        icon = Icons.Default.PersonOff,
                        iconTint = RedDanger,
                        accentBg = RedDanger.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Manager Action Card: Punch on Behalf
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Manager Direct Punch Action",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Select Staff Dropdown
                        ExposedDropdownMenuBox(
                            expanded = staffDropdownExpanded,
                            onExpandedChange = { staffDropdownExpanded = !staffDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedStaffForPunch?.fullName ?: "Select Staff Member",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Staff Member") },
                                leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = staffDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = staffDropdownExpanded,
                                onDismissRequest = { staffDropdownExpanded = false }
                            ) {
                                staffMembers.forEach { staff ->
                                    DropdownMenuItem(
                                        text = { Text("${staff.fullName} (${staff.department})") },
                                        onClick = {
                                            selectedStaffForPunch = staff
                                            staffDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Select Site Dropdown
                        ExposedDropdownMenuBox(
                            expanded = siteDropdownExpanded,
                            onExpandedChange = { siteDropdownExpanded = !siteDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSiteLocation,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Work Site Location") },
                                leadingIcon = { Icon(imageVector = Icons.Default.LocationOn, contentDescription = null) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = siteDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = siteDropdownExpanded,
                                onDismissRequest = { siteDropdownExpanded = false }
                            ) {
                                siteLocations.forEach { site ->
                                    DropdownMenuItem(
                                        text = { Text(site) },
                                        onClick = {
                                            selectedSiteLocation = site
                                            siteDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedStaffForPunch != null) {
                                        attendanceViewModel.managerPunchForStaff(
                                            staff = selectedStaffForPunch!!,
                                            action = "PUNCH_IN",
                                            siteLocation = selectedSiteLocation
                                        )
                                    }
                                },
                                enabled = selectedStaffForPunch != null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                            ) {
                                Text("Punch IN Staff", fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (selectedStaffForPunch != null) {
                                        attendanceViewModel.managerPunchForStaff(
                                            staff = selectedStaffForPunch!!,
                                            action = "PUNCH_OUT",
                                            siteLocation = selectedSiteLocation
                                        )
                                    }
                                },
                                enabled = selectedStaffForPunch != null,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RedDanger)
                            ) {
                                Text("Punch OUT Staff", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (userMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = userMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = TealSecondary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Staff (${staffMembers.size})", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Leaves (${leaveRequests.count { it.status == "PENDING" }})", fontWeight = FontWeight.Bold) }
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
                items(staffMembers) { staff ->
                    val activeRecord = todayRecords.find { it.userId == staff.id && it.punchOutTime == null }
                    val statusText = if (activeRecord != null) activeRecord.status else "ABSENT"

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
                                    text = staff.fullName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${staff.department} | Shift: ${staff.shiftHours}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Phone: ${staff.phone}",
                                    fontSize = 11.sp,
                                    color = TealPrimary
                                )
                            }

                            StatusBadge(status = statusText)
                        }
                    }
                }
            } else if (selectedTab == 1) {
                if (leaveRequests.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No leave requests found.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(leaveRequests) { leave ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = leave.userName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    StatusBadge(status = leave.status)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Type: ${leave.leaveType} | ${leave.startDate} to ${leave.endDate}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Reason: ${leave.reason}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                )

                                if (leave.status == "PENDING") {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                attendanceViewModel.updateLeaveStatus(leave.id, "APPROVED")
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                                        ) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve", fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                attendanceViewModel.updateLeaveStatus(leave.id, "REJECTED")
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedDanger)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 12.sp, color = RedDanger)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                item {
                    UserProfileContent(
                        user = manager,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    if (showProfileDialog) {
        UserProfileDialog(
            user = manager,
            authViewModel = authViewModel,
            onDismiss = { showProfileDialog = false }
        )
    }
}
