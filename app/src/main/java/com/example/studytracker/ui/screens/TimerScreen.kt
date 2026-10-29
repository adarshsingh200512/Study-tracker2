package com.example.studytracker.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.studytracker.ui.viewmodel.TimerMode
import com.example.studytracker.ui.viewmodel.TimerStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val timerStatus by viewModel.timerStatus.collectAsState()
    val timerMode by viewModel.timerMode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    val selectedSubjectId by viewModel.timerSubjectId.collectAsState()
    val selectedTopicId by viewModel.timerTopicId.collectAsState()
    val selectedStudyMode by viewModel.timerStudyMode.collectAsState()

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var topicMenuExpanded by remember { mutableStateOf(false) }

    val activeDisplaySeconds = if (timerMode == TimerMode.COUNTDOWN) remainingSeconds else elapsedSeconds
    val mins = activeDisplaySeconds / 60
    val secs = activeDisplaySeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    // Progress fraction for the circular ring
    val progressFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Focus Study Timer",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Deliberate focus builds academic retention",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
        }

        // Configuration Card: Subject, Topic, Study Mode
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
                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = subjectMenuExpanded,
                    onExpandedChange = { subjectMenuExpanded = !subjectMenuExpanded }
                ) {
                    val currentSubjectName = subjects.find { it.id == selectedSubjectId }?.name ?: "Select Subject"
                    OutlinedTextField(
                        value = currentSubjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject", color = BrandTeal400, fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectMenuExpanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Navy950,
                            unfocusedContainerColor = Navy950,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = BrandTeal400,
                            unfocusedIndicatorColor = DarkCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        subjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name, color = Color.White) },
                                onClick = {
                                    viewModel.timerSubjectId.value = sub.id
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Topic Dropdown
                val filteredTopics = topics.filter { it.subjectId == selectedSubjectId }
                ExposedDropdownMenuBox(
                    expanded = topicMenuExpanded,
                    onExpandedChange = { topicMenuExpanded = !topicMenuExpanded }
                ) {
                    val currentTopicName = filteredTopics.find { it.id == selectedTopicId }?.name ?: "General (No specific topic)"
                    OutlinedTextField(
                        value = currentTopicName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Topic / Chapter", color = BrandTeal400, fontSize = 11.sp) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicMenuExpanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Navy950,
                            unfocusedContainerColor = Navy950,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = BrandTeal400,
                            unfocusedIndicatorColor = DarkCardBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = topicMenuExpanded,
                        onDismissRequest = { topicMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("General (No specific topic)", color = Color.White) },
                            onClick = {
                                viewModel.timerTopicId.value = ""
                                topicMenuExpanded = false
                            }
                        )
                        filteredTopics.forEach { top ->
                            DropdownMenuItem(
                                text = { Text(top.name, color = Color.White) },
                                onClick = {
                                    viewModel.timerTopicId.value = top.id
                                    topicMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Study Mode Selector
                Column {
                    Text(
                        text = "STUDY MODE",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Deep Study", "Exam Prep", "Quick Recall", "Lecture").forEach { mode ->
                            val isSelected = selectedStudyMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) BrandTeal500.copy(alpha = 0.2f) else Navy950)
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandTeal400 else DarkCardBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.timerStudyMode.value = mode }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    color = if (isSelected) BrandTeal400 else Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Circular Countdown Clock Display Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(28.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive SVG/Canvas Circular Ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(if (timerStatus == TimerStatus.RUNNING) ringPulse else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val stroke = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                        // Background track
                        drawCircle(
                            color = DarkCardBorder,
                            radius = (size.width - 10.dp.toPx()) / 2f,
                            style = stroke
                        )
                        // Progress arc
                        val sweep = progressFraction * 360f
                        drawArc(
                            color = BrandTeal400,
                            startAngle = -90f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = stroke
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeFormatted,
                            color = Color.White,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = timerStatus.name,
                            color = when (timerStatus) {
                                TimerStatus.RUNNING -> BrandTeal400
                                TimerStatus.PAUSED -> AccentAmber
                                else -> Color(0xFF94A3B8)
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = selectedStudyMode,
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Presets Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(15, 25, 45, 60).forEach { presetMin ->
                        val isPresetActive = totalSeconds == presetMin * 60
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPresetActive) BrandTeal500 else Navy950)
                                .border(
                                    1.dp,
                                    if (isPresetActive) BrandTeal400 else DarkCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.setTimerPreset(presetMin) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${presetMin}m",
                                color = if (isPresetActive) Navy950 else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Custom 30m / 90m
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Navy950)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setCustomTimerDuration(90) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "90m",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Control Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset Button
                    IconButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Navy950)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .testTag("reset_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = Color(0xFF94A3B8)
                        )
                    }

                    // Start / Pause Primary Button
                    if (timerStatus != TimerStatus.RUNNING) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandTeal500,
                                contentColor = Navy950
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("start_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (timerStatus == TimerStatus.PAUSED) "Resume Focus" else "Start Focus",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentAmber,
                                contentColor = Navy950
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("pause_timer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pause Timer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Finish Early & Log
                    IconButton(
                        onClick = { viewModel.finishTimerSession() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Navy950)
                            .border(1.dp, DarkCardBorder, CircleShape)
                            .testTag("finish_timer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Finish & Save",
                            tint = BrandTeal400
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
