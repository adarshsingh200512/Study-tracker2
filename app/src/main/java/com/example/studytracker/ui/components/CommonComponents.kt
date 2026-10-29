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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.NavigationTab
import com.example.studytracker.ui.viewmodel.TimerStatus
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.BrandTeal600
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.VibrantGreen

@Composable
fun StudyTrackerHeader(
    xp: Int,
    level: Int,
    isMissionComplete: Boolean,
    onOpenMission: () -> Unit,
    onOpenGamification: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepNavyBg.copy(alpha = 0.98f),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .border(width = 1.dp, color = DarkCardBorder.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Hamburger Menu + Logo Badge + Title & Motto
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Menu icon button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("menu_drawer_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // White rounded card with Study Tracker emblem
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Logo Emblem",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "STUDY",
                            color = Color(0xFF0D9488),
                            fontSize = 6.sp,
                            fontWeight = FontWeight.Black,
                            lineHeight = 7.sp
                        )
                    }
                }

                // Title + Subtitle + 2-line motto
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Study Tracker",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.2.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(VibrantGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "All in One",
                                color = VibrantGreen,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Plan your days • Track your study",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Analyze your progress • Achieve your goals",
                        color = Color(0xFF94A3B8),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side: Search, Notification (3), More options
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search button
                IconButton(
                    onClick = onOpenMission,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Notification Bell with red badge circle showing "3"
                Box(contentAlignment = Alignment.TopEnd) {
                    IconButton(
                        onClick = onOpenGamification,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("notification_bell_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(AccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 3 Vertical Dots menu button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StudyTrackerBottomBar(
    currentTab: NavigationTab,
    timerStatus: TimerStatus,
    onSelectTab: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepNavyBg.copy(alpha = 0.98f),
        tonalElevation = 8.dp,
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = DarkCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // 1. Home
            BottomNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = currentTab == NavigationTab.HOME,
                onClick = { onSelectTab(NavigationTab.HOME) }
            )

            // 2. Calendar (Alag Tab - Dedicated)
            BottomNavItem(
                label = "Calendar",
                icon = Icons.Outlined.CalendarMonth,
                isSelected = currentTab == NavigationTab.CALENDAR,
                onClick = { onSelectTab(NavigationTab.CALENDAR) }
            )

            // 3. Center Elevated Focus Mode Button (Alag Tab - Focus Mode with App Blocker)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF34D399), VibrantGreen, Color(0xFF059669))
                        )
                    )
                    .clickable {
                        onSelectTab(NavigationTab.FOCUS)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (timerStatus == TimerStatus.RUNNING) Icons.Default.Security else Icons.Default.Timer,
                        contentDescription = "Focus Shield",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "FOCUS",
                        color = Color.White,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // 4. Notes (Alag Tab - Dedicated Study Notes)
            BottomNavItem(
                label = "Notes",
                icon = Icons.Outlined.Description,
                isSelected = currentTab == NavigationTab.NOTES,
                onClick = { onSelectTab(NavigationTab.NOTES) }
            )

            // 5. Syllabus (Subjects & Topics)
            BottomNavItem(
                label = "Syllabus",
                icon = Icons.Outlined.MenuBook,
                isSelected = currentTab == NavigationTab.SYLLABUS,
                onClick = { onSelectTab(NavigationTab.SYLLABUS) }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VibrantGreen else Color(0xFF94A3B8),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) VibrantGreen else Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun CircularProgressIndicatorWithCenterText(
    progressPercent: Int,
    size: Dp = 90.dp,
    strokeWidth: Dp = 8.dp,
    progressColor: Color = VibrantGreen,
    trackColor: Color = DarkCardBorder,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            // Background track
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = stroke
            )
            // Progress arc
            val sweepAngle = (progressPercent.coerceIn(0, 100) / 100f) * 360f
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$progressPercent%",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "DONE",
                color = progressColor,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CircularMiniProgressRing(
    percent: Int,
    ringColor: Color = VibrantGreen,
    trackColor: Color = DarkCardBorder,
    size: Dp = 38.dp,
    strokeWidth: Dp = 3.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            drawCircle(
                color = trackColor,
                radius = (size.toPx() - strokeWidth.toPx()) / 2f,
                style = stroke
            )
            val sweepAngle = (percent.coerceIn(0, 100) / 100f) * 360f
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
        }
        Text(
            text = "$percent%",
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WeeklyFocusTrendChart(
    values: List<Int> = listOf(70, 75, 80, 90, 85, 88, 78),
    days: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    lineColor: Color = VibrantGreen,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
                val width = size.width
                val height = size.height
                val count = values.size
                if (count < 2) return@Canvas

                val stepX = width / (count - 1)
                val minY = 50f
                val maxY = 100f

                val points = values.mapIndexed { index, v ->
                    val x = index * stepX
                    val normY = (v - minY) / (maxY - minY)
                    val y = height - (normY * (height - 30f)) - 10f
                    Offset(x, y)
                }

                // Draw gradient filled area under the line
                val fillPath = Path().apply {
                    moveTo(points.first().x, height)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.35f),
                            lineColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )

                // Draw curve / polyline
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        val p0 = points[i - 1]
                        val p1 = points[i]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = lineColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw glowing nodes and percentage labels
                val textPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 9.sp.toPx()
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                }

                points.forEachIndexed { i, pt ->
                    // Outer dot
                    drawCircle(
                        color = lineColor,
                        radius = 4.5.dp.toPx(),
                        center = pt
                    )
                    // Inner white dot
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = pt
                    )
                    // Percentage text above node
                    drawContext.canvas.nativeCanvas.drawText(
                        "${values[i]}%",
                        pt.x,
                        pt.y - 8.dp.toPx(),
                        textPaint
                    )
                }
            }
        }

        // X-axis Day Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Text(
                    text = day,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}
