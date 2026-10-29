package com.example.studytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.components.CircularProgressIndicatorWithCenterText
import com.example.studytracker.ui.components.StudyHeatmapCard
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.NavigationTab
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal100
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.BrandTeal600
import com.example.ui.theme.BrandTeal900
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val studySessions by viewModel.studySessions.collectAsState()
    val revisions by viewModel.revisions.collectAsState()
    val tasks by viewModel.tasks.collectAsState()

    val today = viewModel.todayDateString
    val todayDateFormatted = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Calendar.getInstance().time)

    // Daily statistics calculations
    val todaySessions = studySessions.filter { it.date == today }
    val todayMinutes = todaySessions.sumOf { it.durationMinutes }
    val targetMinutes = userProfile?.dailyTargetMinutes ?: 240
    val progressPercent = ((todayMinutes.toFloat() / targetMinutes.toFloat()) * 100).toInt().coerceIn(0, 100)
    val remainingMinutes = maxOf(0, targetMinutes - todayMinutes)

    val avgFocus = if (todaySessions.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.1f", todaySessions.map { it.focusRating }.average())
    } else {
        "0.0"
    }

    // Nearest upcoming exam
    val nearestExamSubject = subjects
        .filter { !it.examDate.isNullOrBlank() && (it.examDate ?: "") >= today }
        .minByOrNull { it.examDate ?: "2099-12-31" }

    val daysUntilExam = nearestExamSubject?.examDate?.let { examDateStr ->
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val examTime = sdf.parse(examDateStr)?.time ?: 0L
            val nowTime = sdf.parse(today)?.time ?: 0L
            ((examTime - nowTime) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } catch (e: Exception) {
            null
        }
    }

    // Next recommended topic (Highest priority topic not completed)
    val nextTopic = topics.firstOrNull { it.status != "Completed" && it.priority == "High" }
        ?: topics.firstOrNull { it.status != "Completed" }

    val nextTopicSubject = nextTopic?.let { t -> subjects.find { it.id == t.subjectId } }

    // Pending revisions and pending tasks
    val pendingRevisionsToday = revisions.filter { it.status == "Pending" && it.date <= today }
    val pendingTasksToday = tasks.filter { it.date == today && it.status != "Completed" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Personalized Greeting & Active Streak Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = todayDateFormatted,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Hello, ${userProfile?.name ?: "Adarsh Singh"} 👋",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandTeal900.copy(alpha = 0.6f))
                                    .border(1.dp, BrandTeal600.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "LEVEL ${userProfile?.level ?: 12} SCHOLAR",
                                    color = BrandTeal400,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BrandTeal900.copy(alpha = 0.3f))
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Study-tracker2",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Streak Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Navy950)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${userProfile?.streak ?: 1} Days",
                                color = AccentAmber,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "ACTIVE STREAK",
                                color = Color(0xFF94A3B8),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 2. Circular Target Progress Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BrandTeal900.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(DarkSurface, BrandTeal900.copy(alpha = 0.35f))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TODAY'S STUDY TARGET",
                                color = BrandTeal400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${todayMinutes / 60}h ${todayMinutes % 60}m",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = " / ${targetMinutes / 60}h",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (remainingMinutes > 0) {
                                    "${remainingMinutes / 60}h ${remainingMinutes % 60}m remaining"
                                } else {
                                    "🎉 Daily target accomplished!"
                                },
                                color = if (remainingMinutes > 0) Color(0xFF94A3B8) else AccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = AccentAmber,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Focus: $avgFocus/5",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "•  ${todaySessions.size} Sessions",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Circular Progress Indicator
                        CircularProgressIndicatorWithCenterText(
                            progressPercent = progressPercent,
                            size = 88.dp,
                            strokeWidth = 7.dp,
                            progressColor = BrandTeal400
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.selectTab(NavigationTab.TIMER)
                                viewModel.startTimer()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandTeal500,
                                contentColor = Navy950
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("quick_start_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Quick Timer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.pendingSessionMinutes.value = 45
                                viewModel.openModal(ActiveModal.FOCUS_RATING)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF1F5F9)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("quick_log_session_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Log Session",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Study Heatmap & Activity Streaks Card
        item {
            StudyHeatmapCard(
                studySessions = studySessions,
                subjects = subjects,
                todayDateStr = today,
                onStartTimer = {
                    viewModel.selectTab(NavigationTab.TIMER)
                    viewModel.startTimer()
                }
            )
        }

        // 4. Exam Countdown Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.openModal(ActiveModal.EXAM_COUNTDOWN) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentAmber.copy(alpha = 0.15f))
                                .border(1.dp, AccentAmber.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = AccentAmber,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "EXAM COUNTDOWN",
                                color = AccentAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (nearestExamSubject != null && daysUntilExam != null) {
                                    "${nearestExamSubject.name} Exam in $daysUntilExam Days"
                                } else {
                                    "No upcoming exams scheduled"
                                },
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (nearestExamSubject?.examName != null) {
                                Text(
                                    text = nearestExamSubject.examName ?: "",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Text(
                        text = "View All →",
                        color = BrandTeal400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 4. Next Recommended Topic Card
        if (nextTopic != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NEXT RECOMMENDED TOPIC",
                                color = BrandTeal400,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (nextTopic.priority == "High") AccentRed.copy(alpha = 0.2f)
                                        else DarkCardBg
                                    )
                                    .border(
                                        1.dp,
                                        if (nextTopic.priority == "High") AccentRed.copy(alpha = 0.6f)
                                        else DarkCardBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${nextTopic.priority} Priority",
                                    color = if (nextTopic.priority == "High") Color(0xFFFCA5A5) else Color(0xFFCBD5E1),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = nextTopic.name,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = nextTopicSubject?.name ?: "",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.openTopicDetail(nextTopic) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandTeal500.copy(alpha = 0.15f),
                                    contentColor = BrandTeal400
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BrandTeal500.copy(alpha = 0.4f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = "Open Topic",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Revisions & Tasks Quick Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending Revisions Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openModal(ActiveModal.PLANNER) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🔄", fontSize = 18.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (pendingRevisionsToday.isNotEmpty()) AccentAmber.copy(alpha = 0.2f)
                                        else DarkCardBg
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${pendingRevisionsToday.size} Due",
                                    color = if (pendingRevisionsToday.isNotEmpty()) AccentAmber else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Revisions",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (pendingRevisionsToday.isNotEmpty()) "Recall schedule pending" else "All caught up!",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }

                // Today's Tasks Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.selectTab(NavigationTab.TASKS) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📝", fontSize = 18.sp)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (pendingTasksToday.isNotEmpty()) AccentBlue.copy(alpha = 0.2f)
                                        else DarkCardBg
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${pendingTasksToday.size} Left",
                                    color = if (pendingTasksToday.isNotEmpty()) AccentBlue else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Today's Tasks",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (pendingTasksToday.isNotEmpty()) "Action items pending" else "Zero pending tasks",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // 6. Quick Launch Tool Cards (Smart Planner, Notes, History)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Smart Planner
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openModal(ActiveModal.PLANNER) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📅", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Smart Planner",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Notes
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openModal(ActiveModal.ALL_NOTES) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📒", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Quick Notes",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Daily Mission
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.openModal(ActiveModal.DAILY_MISSION) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎯", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Daily Mission",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
