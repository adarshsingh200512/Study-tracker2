package com.example.studytracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.components.defaultTextFieldColors
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.studytracker.ui.viewmodel.TimerMode
import com.example.studytracker.ui.viewmodel.TimerStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MathBlue
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.TestPurple
import com.example.ui.theme.VibrantGreen

data class DistractionAppInfo(
    val name: String,
    val category: String,
    val brandColor: Color,
    val description: String
)

val allDistractionApps = listOf(
    DistractionAppInfo("Instagram", "Social", Color(0xFFE1306C), "Reels, DMs, Feed distractions"),
    DistractionAppInfo("YouTube", "Video", Color(0xFFFF0000), "Shorts, recommendations, long videos"),
    DistractionAppInfo("WhatsApp", "Messaging", Color(0xFF25D366), "Group chats, status updates, calls"),
    DistractionAppInfo("Games (PUBG/BGMI)", "Gaming", Color(0xFFF59E0B), "Action, competitive multiplayer"),
    DistractionAppInfo("TikTok / Reels", "Shorts", Color(0xFF00F2FE), "Algorithmic infinite feed"),
    DistractionAppInfo("Snapchat", "Social", Color(0xFFFFFC00), "Streaks, stories, quick snaps"),
    DistractionAppInfo("Netflix", "Streaming", Color(0xFFE50914), "Shows, movies, binge notifications"),
    DistractionAppInfo("Twitter / X", "Social", Color(0xFF1DA1F2), "Trending feed, breaking news"),
    DistractionAppInfo("Reddit", "Forums", Color(0xFFFF4500), "Subreddits, memes, comment loops"),
    DistractionAppInfo("Chrome Browser", "Web", Color(0xFF4285F4), "Random searches, entertainment tabs")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
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

    val blockedApps by viewModel.blockedApps.collectAsState()
    val isStrictShield by viewModel.isStrictShield.collectAsState()
    val activeAmbientSound by viewModel.activeAmbientSound.collectAsState()
    val shieldBlockedAttempt by viewModel.shieldBlockedAppAttempt.collectAsState()

    var subjectMenuExpanded by remember { mutableStateOf(false) }
    var topicMenuExpanded by remember { mutableStateOf(false) }

    val activeDisplaySeconds = if (timerMode == TimerMode.COUNTDOWN) remainingSeconds else elapsedSeconds
    val mins = activeDisplaySeconds / 60
    val secs = activeDisplaySeconds % 60
    val timeFormatted = String.format("%02d:%02d", mins, secs)

    val progressFraction = if (totalSeconds > 0) {
        ((totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val infiniteTransition = rememberInfiniteTransition(label = "shield_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Alert Dialog shown when user tests or tries to open a blocked app during focus mode
    shieldBlockedAttempt?.let { appName ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissAppBlockShieldAlert() },
            containerColor = DarkSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Blocked",
                        tint = AccentRed,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "$appName is Blocked!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Selective App Blocker is actively shielding your session. Opening distracting apps is restricted during Focus Mode.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Time Remaining: $timeFormatted",
                        color = VibrantGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissAppBlockShieldAlert() },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen)
                ) {
                    Text("Stay Focused", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy950)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        // ==========================================
        // 1. FOCUS SHIELD BANNER & STATUS
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (timerStatus == TimerStatus.RUNNING) VibrantGreen.copy(alpha = 0.12f) else DarkSurface
                ),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (timerStatus == TimerStatus.RUNNING) VibrantGreen.copy(alpha = 0.6f) else DarkCardBorder
                ),
                modifier = Modifier.fillMaxWidth()
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (timerStatus == TimerStatus.RUNNING) VibrantGreen else BrandTeal500.copy(alpha = 0.2f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (timerStatus == TimerStatus.RUNNING) Icons.Default.Security else Icons.Default.Smartphone,
                                contentDescription = "Shield",
                                tint = if (timerStatus == TimerStatus.RUNNING) Color.White else BrandTeal400,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (timerStatus == TimerStatus.RUNNING) "Study Shield Active" else "Focus Mode & App Blocker",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (timerStatus == TimerStatus.RUNNING) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .scale(pulseScale)
                                            .clip(CircleShape)
                                            .background(VibrantGreen)
                                    )
                                }
                            }
                            Text(
                                text = "${blockedApps.size} apps blocked • ${if (isStrictShield) "Strict Lock ON" else "Standard Shield"}",
                                color = if (timerStatus == TimerStatus.RUNNING) VibrantGreen else Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Strict Shield Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Strict",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isStrictShield,
                            onCheckedChange = { viewModel.toggleStrictShield() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = VibrantGreen,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = DarkCardBg
                            ),
                            modifier = Modifier.scale(0.8f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 2. BIG CIRCULAR COUNTDOWN TIMER
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Subject and Topic Header
                    val currentSubject = subjects.find { it.id == selectedSubjectId }
                    val currentTopic = topics.find { it.id == selectedTopicId }

                    Text(
                        text = currentSubject?.name ?: "Deep Focus Study",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = currentTopic?.name ?: selectedStudyMode,
                        color = BrandTeal400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Animated Countdown Circular Ring
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(200.dp)
                            .scale(if (timerStatus == TimerStatus.RUNNING) pulseScale else 1f)
                    ) {
                        Canvas(modifier = Modifier.size(190.dp)) {
                            val strokeWidthPx = 10.dp.toPx()
                            val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)

                            // Track
                            drawCircle(
                                color = DarkCardBorder,
                                radius = (size.minDimension - strokeWidthPx) / 2f,
                                style = stroke
                            )

                            // Animated Arc
                            val sweep = progressFraction * 360f
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(BrandTeal500, VibrantGreen, PhysicsGreen, BrandTeal500)
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
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = when (timerStatus) {
                                    TimerStatus.RUNNING -> "SHIELD ACTIVE"
                                    TimerStatus.PAUSED -> "PAUSED"
                                    TimerStatus.IDLE -> "READY TO FOCUS"
                                },
                                color = when (timerStatus) {
                                    TimerStatus.RUNNING -> VibrantGreen
                                    TimerStatus.PAUSED -> AccentAmber
                                    TimerStatus.IDLE -> Color(0xFF94A3B8)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Control Buttons (Start, Pause, Reset, Finish)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (timerStatus == TimerStatus.IDLE) {
                            Button(
                                onClick = { viewModel.toggleFocusMode(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("start_focus_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                                    Text("Start Focus Mode (Shield)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        } else {
                            if (timerStatus == TimerStatus.RUNNING) {
                                Button(
                                    onClick = { viewModel.pauseTimer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(46.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = null, tint = Color.White)
                                        Text("Pause", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.startTimer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.height(46.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                                        Text("Resume", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Finish Button (Opens Focus Rating)
                            Button(
                                onClick = { viewModel.finishTimerSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = PhysicsGreen),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.height(46.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    Text("Finish", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Reset
                            IconButton(
                                onClick = { viewModel.resetTimer() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkCardBg)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFCBD5E1))
                            }
                        }
                    }

                    if (timerStatus != TimerStatus.IDLE) {
                        Spacer(modifier = Modifier.height(10.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { viewModel.toggleFocusMode(true) },
                            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Fullscreen Focus Shield", color = VibrantGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Preset Duration Pills (15m, 25m, 45m, 60m)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(15, 25, 45, 60).forEach { minsPreset ->
                            val isSelected = (totalSeconds / 60) == minsPreset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) VibrantGreen.copy(alpha = 0.2f) else DarkCardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) VibrantGreen else DarkCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable(enabled = timerStatus == TimerStatus.IDLE) {
                                        viewModel.setTimerPreset(minsPreset)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${minsPreset}m",
                                    color = if (isSelected) VibrantGreen else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // 3. SELECTIVE APP BLOCKER LIST ("selective app band ho jaye")
        // ==========================================
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
                        Column {
                            Text(
                                text = "Selective App Blocker",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Select apps to shut down & silence during study",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }

                        // Quick Toggle All
                        val allBlocked = blockedApps.size == allDistractionApps.size
                        Text(
                            text = if (allBlocked) "Clear All" else "Block All",
                            color = VibrantGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(VibrantGreen.copy(alpha = 0.15f))
                                .clickable {
                                    viewModel.setAllAppsBlocked(!allBlocked)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // App Selection Grid / List
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allDistractionApps.forEach { app ->
                            val isBlocked = blockedApps.contains(app.name)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkCardBg)
                                    .border(
                                        1.dp,
                                        if (isBlocked && timerStatus == TimerStatus.RUNNING) VibrantGreen.copy(alpha = 0.5f) else DarkCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (timerStatus == TimerStatus.RUNNING && isBlocked) {
                                            // Test blocking demo!
                                            viewModel.triggerAppBlockShieldAlert(app.name)
                                        } else {
                                            viewModel.toggleBlockedApp(app.name)
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // App Color Icon Dot
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(app.brandColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isBlocked) Icons.Default.Block else Icons.Default.Smartphone,
                                            contentDescription = app.name,
                                            tint = if (isBlocked) app.brandColor else Color(0xFF94A3B8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = app.name,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color(0xFF1E293B))
                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = app.category,
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (isBlocked && timerStatus == TimerStatus.RUNNING) "🚫 Blocked by Study Shield" else app.description,
                                            color = if (isBlocked && timerStatus == TimerStatus.RUNNING) VibrantGreen else Color(0xFF64748B),
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                // Checkbox / Status
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isBlocked) VibrantGreen else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isBlocked) VibrantGreen else Color(0xFF64748B),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isBlocked) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Blocked",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (timerStatus == TimerStatus.RUNNING) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(VibrantGreen.copy(alpha = 0.12f))
                                .border(1.dp, VibrantGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "💡 Tip: Tap any blocked app above to test the Selective App Blocker Shield in real time!",
                                color = VibrantGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. FOCUS SOUNDS / AMBIENT AUDIO
        // ==========================================
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = VibrantGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Focus Ambient Sound",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("None", "Rain 🌧️", "White Noise 🌊", "Library 📚", "Binaural 40Hz 🧠", "Forest 🌲").forEach { sound ->
                            val isSelected = activeAmbientSound == sound
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) VibrantGreen.copy(alpha = 0.2f) else DarkCardBg)
                                    .border(
                                        1.dp,
                                        if (isSelected) VibrantGreen else DarkCardBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.setAmbientSound(sound) }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = sound,
                                    color = if (isSelected) VibrantGreen else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}
