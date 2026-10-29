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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.components.DayStudyData
import com.example.studytracker.ui.components.formatDecimal
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.BrandTeal600
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    var analyticsPeriod by remember { mutableStateOf("Overview") } // Overview, Weekly, Monthly

    val studySessions by viewModel.studySessions.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    // Real dynamic calculations
    val totalMinutes = studySessions.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60f
    val totalSessionsCount = studySessions.size
    val completedTopicsCount = topics.count { it.status == "Completed" }
    val completedVideosCount = videos.count { it.isCompleted }
    val avgFocus = if (studySessions.isNotEmpty()) {
        String.format(Locale.getDefault(), "%.1f", studySessions.map { it.focusRating }.average())
    } else "0.0"

    // Past 7 Days Dynamic Data for Bar Chart
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val daySdf = SimpleDateFormat("EE", Locale.getDefault())

    val past7DaysData: List<DayStudyData> = remember(studySessions) {
        val list = mutableListOf<DayStudyData>()
        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = sdf.format(c.time)
            val dayLabel = daySdf.format(c.time).take(2)
            val minutes = studySessions.filter { it.date == dStr }.sumOf { it.durationMinutes }
            list.add(DayStudyData(dStr, dayLabel, minutes, minutes / 60f))
        }
        list
    }

    val maxDayMinutes = maxOf(60, past7DaysData.maxOfOrNull { it.minutes } ?: 60)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header & Sub-period Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Performance Analytics",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Data-driven insights to optimize mastery",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Sub-tabs
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    listOf("Overview", "Weekly", "Monthly").forEach { tab ->
                        val isSelected = analyticsPeriod == tab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) BrandTeal500 else Color.Transparent)
                                .clickable { analyticsPeriod = tab }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Navy950 else Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Metrics Grid (4 cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 1: Total Study Time
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOTAL STUDY TIME",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatDecimal(totalHours)}h",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "$totalSessionsCount recorded sessions",
                            color = BrandTeal400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Card 2: Average Focus
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "AVERAGE FOCUS",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$avgFocus / 5",
                            color = AccentAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Target score: 4.0+",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card 3: Topics Mastered
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TOPICS MASTERED",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedTopicsCount / ${topics.size}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${if (topics.isNotEmpty()) (completedTopicsCount * 100 / topics.size) else 0}% syllabus progress",
                            color = BrandTeal400,
                            fontSize = 10.sp
                        )
                    }
                }

                // Card 4: Videos Watched
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "VIDEOS WATCHED",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$completedVideosCount / ${videos.size}",
                            color = AccentBlue,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "${if (videos.isNotEmpty()) (completedVideosCount * 100 / videos.size) else 0}% video progress",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Real Dynamic Study Hours Bar Chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PAST 7 DAYS STUDY HOURS",
                            color = BrandTeal400,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        val past7DaysTotalMins = past7DaysData.sumOf { it.minutes }
                        Text(
                            text = "${past7DaysTotalMins / 60}h ${past7DaysTotalMins % 60}m total",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bar Chart
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        past7DaysData.forEach { data ->
                            val heightFrac = (data.minutes.toFloat() / maxDayMinutes.toFloat()).coerceIn(0.06f, 1f)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = formatDecimal(data.hours),
                                    color = if (data.minutes > 0) Color.White else Color(0xFF64748B),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Box(
                                    modifier = Modifier
                                        .width(26.dp)
                                        .height(84.dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(Navy950),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height(84.dp * heightFrac)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(BrandTeal400, BrandTeal600)
                                                )
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = data.dayLabel,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subject Study Distribution Progress Bars
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "SUBJECT STUDY DISTRIBUTION",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    if (subjects.isEmpty()) {
                        Text(
                            text = "No subjects available yet.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    } else {
                        subjects.forEach { subject ->
                            val subMinutes = studySessions.filter { it.subjectId == subject.id }.sumOf { it.durationMinutes }
                            val subHours = subMinutes / 60f
                            val targetHours = subject.targetHours.coerceAtLeast(1f)
                            val progress = ((subHours / targetHours) * 100).toInt().coerceIn(0, 100)

                            val subjectColor = try {
                                Color(android.graphics.Color.parseColor(subject.colorHex))
                            } catch (e: Exception) {
                                BrandTeal400
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                                                .background(subjectColor)
                                        )
                                        Text(
                                            text = subject.name,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "${formatDecimal(subHours)}h / ${targetHours.toInt()}h ($progress%)",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Navy950)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progress / 100f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(subjectColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}
