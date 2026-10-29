package com.example.studytracker.ui.components

import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.Navy950
import java.util.Locale

data class DayStudyData(
    val dateStr: String,
    val dayLabel: String,
    val minutes: Int,
    val hours: Float
)

data class BadgeItem(
    val name: String,
    val emoji: String,
    val isUnlocked: Boolean
)

fun formatDecimal(value: Float): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}

fun formatDecimal(value: Double): String {
    return String.format(Locale.getDefault(), "%.1f", value)
}

@Composable
fun defaultTextFieldColors(): TextFieldColors = TextFieldDefaults.colors(
    focusedContainerColor = Navy950,
    unfocusedContainerColor = Navy950,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedIndicatorColor = BrandTeal400,
    unfocusedIndicatorColor = DarkCardBorder
)
