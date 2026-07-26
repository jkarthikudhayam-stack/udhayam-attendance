package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RedDanger
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.TealSecondary

@Composable
fun RoleBadge(role: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (role.uppercase()) {
        "OWNER" -> Triple(TealPrimary, Color.White, "BUSINESS OWNER")
        "MANAGER" -> Triple(TealSecondary, Color.White, "MANAGER")
        else -> Triple(Color(0xFF0284C7), Color.White, "STAFF MEMBER")
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "PRESENT", "APPROVED" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        "LATE", "PENDING" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "ON_BREAK" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        "ABSENT", "REJECTED" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replace("_", " "),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
