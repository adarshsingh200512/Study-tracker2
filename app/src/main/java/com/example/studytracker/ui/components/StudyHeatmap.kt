package com.example.studytracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.data.model.StudySessionEntity
import com.example.studytracker.data.model.SubjectEntity
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.BrandTeal100
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.BrandTeal600
import com.example.ui.theme.BrandTeal700
import com.example.ui.theme.BrandTeal900
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.VibrantGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data representation of an individual day square in the Study Heatmap.
 */
data class HeatmapDay(
    val dateStr: String, // yyyy-MM-dd
    val displayDate: String, // e.g. "May 8, 2024"
    val dayOfWeek: Int, // 0 = Sunday, 1 = Monday, ... 6 = Saturday
    val dayOfMonth: Int,
    val monthLabel: String, // e.g. "May"
    val minutesStudied: Int,
    val hoursStudied: Float,
    val intensityLevel: Int, // 0 to 4
    val isToday: Boolean,
    val isFuture: Boolean
)

/**
 * Full interactive Study Heatmap visualization component for Dashboard/HomeScreen.
 * Tracks daily activity streaks and highlights study intensity squares based on hours studied.
 */
@Composable
fun StudyHeatmapCard(
    studySessions: List<StudySessionEntity>,
    subjects: List<SubjectEntity>,
    todayDateStr: String,
    modifier: Modifier = Modifier,
    onStartTimer: (() -> Unit)? = null
) {
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displaySdf = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val monthHeaderSdf = remember { SimpleDateFormat("MMM", Locale.getDefault()) }

    // Aggregate sessions by date
    val sessionsByDate = remember(studySessions) {
        studySessions.groupBy { it.date }
    }

    val dailyMinutesMap = remember(studySessions) {
        studySessions.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.durationMinutes } }
    }

    // Calculate streaks and statistics
    val todayCal = remember { Calendar.getInstance() }
    val todayDate = remember { todayCal.time }

    // Generate grid for past 15 weeks (105 days)
    val numberOfWeeks = 15
    val heatmapWeeks = remember(studySessions, todayDateStr) {
        val weeks = mutableListOf<List<HeatmapDay>>()
        val endCal = Calendar.getInstance().apply {
            time = todayDate
            // Align end to the end of the current week (Saturday)
            val currentDayOfWeek = get(Calendar.DAY_OF_WEEK)
            val daysUntilSaturday = Calendar.SATURDAY - currentDayOfWeek
            add(Calendar.DAY_OF_YEAR, daysUntilSaturday)
        }

        val totalDays = numberOfWeeks * 7
        val startCal = (endCal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -totalDays + 1)
        }

        val curCal = startCal.clone() as Calendar
        for (w in 0 until numberOfWeeks) {
            val weekDays = mutableListOf<HeatmapDay>()
            for (d in 0..6) {
                val dateString = sdf.format(curCal.time)
                val dayOfWeek = curCal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun .. 6=Sat
                val dayOfMonth = curCal.get(Calendar.DAY_OF_MONTH)
                val monthLabel = monthHeaderSdf.format(curCal.time)

                val isToday = dateString == todayDateStr
                val isFuture = curCal.time.after(todayDate) && !isToday

                val minutes = if (isFuture) 0 else (dailyMinutesMap[dateString] ?: 0)
                val hours = minutes / 60f

                // Intensity calculation:
                // Level 0: 0h (Rest day)
                // Level 1: < 1.5h
                // Level 2: 1.5h to < 3h
                // Level 3: 3h to < 5h
                // Level 4: 5h+ (Peak intensity)
                val intensity = when {
                    isFuture || minutes == 0 -> 0
                    minutes < 90 -> 1
                    minutes < 180 -> 2
                    minutes < 300 -> 3
                    else -> 4
                }

                weekDays.add(
                    HeatmapDay(
                        dateStr = dateString,
                        displayDate = displaySdf.format(curCal.time),
                        dayOfWeek = dayOfWeek,
                        dayOfMonth = dayOfMonth,
                        monthLabel = monthLabel,
                        minutesStudied = minutes,
                        hoursStudied = hours,
                        intensityLevel = intensity,
                        isToday = isToday,
                        isFuture = isFuture
                    )
                )

                curCal.add(Calendar.DAY_OF_YEAR, 1)
            }
            weeks.add(weekDays)
        }
        weeks
    }

    // Streaks calculation
    val streakStats = remember(studySessions, todayDateStr) {
        var currentStreak = 0
        var longestStreak = 0

        val checkCal = Calendar.getInstance().apply { time = todayDate }
        val todayMin = dailyMinutesMap[todayDateStr] ?: 0

        // If studied today, start from today. If not, start check from yesterday
        if (todayMin > 0) {
            currentStreak++
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Check yesterday
            val yesterdayCal = (checkCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
            val yesterdayStr = sdf.format(yesterdayCal.time)
            if ((dailyMinutesMap[yesterdayStr] ?: 0) > 0) {
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            }
        }

        while (true) {
            val dateStr = sdf.format(checkCal.time)
            val mins = dailyMinutesMap[dateStr] ?: 0
            if (mins > 0) {
                if (todayMin == 0 && currentStreak == 0) {
                    currentStreak = 1
                } else if (todayMin > 0 || currentStreak > 0) {
                    currentStreak++
                }
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        // Longest streak across all recorded dates
        val activeDates = dailyMinutesMap.filter { it.value > 0 }.keys.sorted()
        var tempStreak = 0
        var prevCal: Calendar? = null

        for (dateStr in activeDates) {
            val dCal = Calendar.getInstance().apply {
                time = sdf.parse(dateStr) ?: Date()
            }
            if (prevCal == null) {
                tempStreak = 1
            } else {
                val diffDays = ((dCal.timeInMillis - prevCal.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
                if (diffDays == 1) {
                    tempStreak++
                } else if (diffDays > 1) {
                    tempStreak = 1
                }
            }
            if (tempStreak > longestStreak) {
                longestStreak = tempStreak
            }
            prevCal = dCal
        }
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak
        }

        val totalActiveDays = dailyMinutesMap.count { it.value > 0 }
        val totalMinutes = dailyMinutesMap.values.sum()
        val totalHours = totalMinutes / 60f

        HeatmapStats(
            currentStreak = maxOf(1, currentStreak),
            longestStreak = maxOf(currentStreak, longestStreak, 1),
            totalActiveDays = maxOf(totalActiveDays, 1),
            totalHours = totalHours
        )
    }

    var selectedDay by remember {
        mutableStateOf<HeatmapDay?>(
            heatmapWeeks.flatten().find { it.isToday } ?: heatmapWeeks.lastOrNull()?.lastOrNull()
        )
    }

    val scrollState = rememberScrollState()

    // Auto scroll to the end (today) on initial load
    LaunchedEffect(Unit) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_heatmap_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title, Fire Badge & Streak
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BrandTeal900, BrandTeal700)
                                )
                            )
                            .border(1.dp, BrandTeal500.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "📊", fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = "STUDY ACTIVITY HEATMAP",
                            color = BrandTeal400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Daily Streaks & Consistency",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Current streak badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentAmber.copy(alpha = 0.15f))
                        .border(1.dp, AccentAmber.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = AccentAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${streakStats.currentStreak}d Streak",
                        color = AccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4 Key Stats Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Navy950)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak
                MetricItem(
                    label = "STREAK",
                    value = "${streakStats.currentStreak} Days",
                    valueColor = AccentAmber,
                    icon = "🔥"
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(DarkCardBorder)
                )

                // Longest Streak
                MetricItem(
                    label = "BEST RUN",
                    value = "${streakStats.longestStreak} Days",
                    valueColor = BrandTeal300Color,
                    icon = "🏆"
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(DarkCardBorder)
                )

                // Active Days
                MetricItem(
                    label = "ACTIVE DAYS",
                    value = "${streakStats.totalActiveDays}d",
                    valueColor = AccentEmerald,
                    icon = "📅"
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(DarkCardBorder)
                )

                // Total Hours
                MetricItem(
                    label = "TOTAL TIME",
                    value = "${String.format(Locale.getDefault(), "%.1f", streakStats.totalHours)}h",
                    valueColor = Color.White,
                    icon = "⏱️"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Heatmap Grid Container with Horizontal Scroll
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Day of Week Labels (Fixed on left)
                Column(
                    modifier = Modifier.padding(top = 22.dp, end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val daysLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                    daysLabels.forEachIndexed { index, label ->
                        // Show labels for Mon, Wed, Fri
                        val showText = index == 1 || index == 3 || index == 5
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (showText) {
                                Text(
                                    text = label,
                                    color = Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Scrollable Weeks Columns
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                ) {
                    // Month Labels Row above columns
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(18.dp)
                    ) {
                        var lastMonth = ""
                        heatmapWeeks.forEach { week ->
                            val firstDayOfMonthInWeek = week.find { it.dayOfMonth <= 7 }
                            val monthToDisplay = if (firstDayOfMonthInWeek != null && firstDayOfMonthInWeek.monthLabel != lastMonth) {
                                lastMonth = firstDayOfMonthInWeek.monthLabel
                                firstDayOfMonthInWeek.monthLabel
                            } else ""

                            Box(
                                modifier = Modifier.width(16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (monthToDisplay.isNotEmpty()) {
                                    Text(
                                        text = monthToDisplay,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Grid Columns: 7 rows of day squares
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        heatmapWeeks.forEach { week ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                week.forEach { day ->
                                    val isSelected = selectedDay?.dateStr == day.dateStr
                                    HeatmapSquare(
                                        day = day,
                                        isSelected = isSelected,
                                        onClick = {
                                            selectedDay = day
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Heatmap Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap any square to inspect",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )

                // Intensity scale: Less -> More
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Less",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    for (lvl in 0..4) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(getIntensityColor(lvl))
                                .border(
                                    0.5.dp,
                                    if (lvl == 0) DarkCardBorder else Color.Transparent,
                                    RoundedCornerShape(3.dp)
                                )
                        )
                    }

                    Text(
                        text = "More",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Interactive Day Inspector Panel
            selectedDay?.let { day ->
                Spacer(modifier = Modifier.height(14.dp))

                val daySessions = sessionsByDate[day.dateStr] ?: emptyList()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Navy950)
                        .border(1.dp, BrandTeal900.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(getIntensityColor(day.intensityLevel))
                                )
                                Text(
                                    text = day.displayDate,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BrandTeal900)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            color = BrandTeal300Color,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }

                            // Intensity Label
                            val intensityText = when (day.intensityLevel) {
                                0 -> if (day.isFuture) "Upcoming" else "Rest Day"
                                1 -> "Light • <1.5h"
                                2 -> "Moderate • 2h+"
                                3 -> "High • 4h+"
                                else -> "Peak 🔥 5h+"
                            }
                            Text(
                                text = intensityText,
                                color = if (day.intensityLevel > 0) BrandTeal400 else Color(0xFF64748B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (day.minutesStudied > 0) {
                                    "${day.minutesStudied / 60}h ${day.minutesStudied % 60}m studied (${daySessions.size} session${if (daySessions.size > 1) "s" else ""})"
                                } else {
                                    "No study sessions logged for this day"
                                },
                                color = if (day.minutesStudied > 0) Color(0xFFE2E8F0) else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )

                            if (day.isToday && onStartTimer != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandTeal500)
                                        .clickable { onStartTimer() }
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "+ Study Now",
                                        color = Navy950,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Breakdown of subjects studied on this selected day
                        if (daySessions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val subjectIds = daySessions.map { it.subjectId }.distinct()
                                subjectIds.take(3).forEach { sId ->
                                    val subj = subjects.find { it.id == sId }
                                    val subjMinutes = daySessions.filter { it.subjectId == sId }.sumOf { it.durationMinutes }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkCardBg)
                                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${subj?.name ?: "Subject"}: ${subjMinutes}m",
                                            color = Color(0xFFCBD5E1),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeatmapSquare(
    day: HeatmapDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val baseColor = getIntensityColor(day.intensityLevel)

    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(baseColor)
            .border(
                width = if (isSelected) 1.5.dp else if (day.isToday) 1.dp else 0.5.dp,
                color = when {
                    isSelected -> Color.White
                    day.isToday -> AccentAmber
                    day.intensityLevel == 0 -> DarkCardBorder
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = !day.isFuture) { onClick() }
            .testTag("heatmap_day_${day.dateStr}"),
        contentAlignment = Alignment.Center
    ) {
        // Today indicator subtle center dot
        if (day.isToday && day.intensityLevel == 0) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(AccentAmber)
            )
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    valueColor: Color,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = icon, fontSize = 11.sp)
            Text(
                text = value,
                color = valueColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = Color(0xFF64748B),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private val BrandTeal300Color = Color(0xFF5EEAD4)

private fun getIntensityColor(level: Int): Color {
    return when (level) {
        0 -> Color(0xFF131D33) // Empty / Rest day
        1 -> Color(0xFF0F4E46) // Level 1: Light (<1.5h)
        2 -> Color(0xFF0D7E71) // Level 2: Moderate (1.5-3h)
        3 -> Color(0xFF14B8A6) // Level 3: High (3-5h)
        4 -> Color(0xFF22C55E) // Level 4: Extreme / Peak (5h+)
        else -> Color(0xFF131D33)
    }
}

private data class HeatmapStats(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalActiveDays: Int,
    val totalHours: Float
)
