package com.example.studytracker.ui.components

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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.studytracker.ui.viewmodel.TimerMode
import com.example.studytracker.ui.viewmodel.TimerStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.VibrantGreen

@Composable
fun FullScreenFocusOverlay(
    viewModel: StudyViewModel,
    onStopFocusMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timerStatus by viewModel.timerStatus.collectAsState()
    val timerMode by viewModel.timerMode.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val totalSeconds by viewModel.totalDurationSeconds.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()

    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val selectedSubjectId by viewModel.timerSubjectId.collectAsState()
    val selectedTopicId by viewModel.timerTopicId.collectAsState()
    val selectedStudyMode by viewModel.timerStudyMode.collectAsState()

    val blockedApps by viewModel.blockedApps.collectAsState()
    val isStrictShield by viewModel.isStrictShield.collectAsState()
    val activeAmbientSound by viewModel.activeAmbientSound.collectAsState()

    var showExitConfirmDialog by remember { mutableStateOf(false) }

    val activeDisplaySeconds = if (timerMode == TimerMode.COUNTDOWN) remainingSeconds else elapsedSeconds
    val mins = activeDisplaySeconds / 60
    val secs = activeDisplaySeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    val progressFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val currentSubject = subjects.find { it.id == selectedSubjectId }
    val currentTopic = topics.find { it.id == selectedTopicId }

    val infiniteTransition = rememberInfiniteTransition(label = "overlay_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Stop Focus Mode Confirmation Dialog (for Strict Focus protection)
    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            containerColor = DarkSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = AccentAmber,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Stop Focus Mode?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "You still have $timeFormatted remaining in your focus session. Ending now will pause your study streak.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExitConfirmDialog = false
                        viewModel.stopFocusModeFromOverlay()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                ) {
                    Text("Stop Anyway", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Keep Focusing", color = VibrantGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Root full-screen semi-transparent overlay
    // Consumes all click and drag interactions to disable navigation interaction underneath
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF5060A14)) // Deep semi-transparent dark veil (approx 96% opacity)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Swallows all clicks completely to disable navigation interaction with underlying screens
            }
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ==========================================
            // 1. TOP HEADER: SHIELD STATUS
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(VibrantGreen.copy(alpha = 0.15f))
                        .border(1.dp, VibrantGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(VibrantGreen)
                    )
                    Text(
                        text = "FOCUS SHIELD ACTIVE",
                        color = VibrantGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = currentSubject?.name ?: "Deep Study Focus",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                if (currentTopic != null || selectedStudyMode.isNotBlank()) {
                    Text(
                        text = currentTopic?.name ?: selectedStudyMode,
                        color = BrandTeal400,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                // Blocked Apps Indicator Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkCardBg)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${blockedApps.size} Distracting Apps Blocked & Silenced",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 2. CENTER: ANIMATED TIMER PROGRESS RING
            // ==========================================
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(230.dp)
                    .scale(pulseScale)
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidthPx = 12.dp.toPx()
                    val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

                    // Background track
                    drawCircle(
                        color = Color(0xFF1E293B),
                        radius = (size.minDimension - strokeWidthPx) / 2f,
                        style = stroke
                    )

                    // Glowing progress sweep
                    val sweep = progressFraction * 360f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(BrandTeal400, VibrantGreen, PhysicsGreen, BrandTeal400)
                        ),
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
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = when (timerStatus) {
                            TimerStatus.RUNNING -> "COUNTING DOWN"
                            TimerStatus.PAUSED -> "PAUSED"
                            TimerStatus.IDLE -> "READY"
                        },
                        color = if (timerStatus == TimerStatus.RUNNING) VibrantGreen else AccentAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
            // 3. MOTIVATIONAL PILL & AMBIENT AUDIO
            // ==========================================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (activeAmbientSound != "None") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = BrandTeal400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Ambient Audio: $activeAmbientSound",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Text(
                    text = "“Focus is saying no to 1,000 other distractions.”",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ==========================================
            // 4. CONTROLS: PAUSE / RESUME & STOP FOCUS MODE BUTTON
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Secondary Pause/Resume Control
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (timerStatus == TimerStatus.RUNNING) {
                        OutlinedButton(
                            onClick = { viewModel.pauseTimer() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentAmber),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause Timer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    } else if (timerStatus == TimerStatus.PAUSED) {
                        OutlinedButton(
                            onClick = { viewModel.startTimer() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VibrantGreen),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume Timer", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // PRIMARY 'STOP FOCUS MODE' BUTTON (Required by user)
                Button(
                    onClick = {
                        if (isStrictShield && timerStatus == TimerStatus.RUNNING) {
                            showExitConfirmDialog = true
                        } else {
                            viewModel.stopFocusModeFromOverlay()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("stop_focus_mode_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stop Focus Mode",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
