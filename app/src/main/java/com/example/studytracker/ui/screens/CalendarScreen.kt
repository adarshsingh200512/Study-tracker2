package com.example.studytracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.ui.components.CircularMiniProgressRing
import com.example.studytracker.ui.components.WeeklyFocusTrendChart
import com.example.studytracker.ui.components.formatDecimal
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.NavigationTab
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.ChemistryOrange
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MathBlue
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy950
import com.example.ui.theme.NoStudyGrey
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.TaskPink
import com.example.ui.theme.TestPurple
import com.example.ui.theme.VibrantGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class CalendarSubTab {
    OVERVIEW, CALENDAR, EVENTS, DAILY_DETAILS, ANALYTICS
}

// Data structures for timeline and events
data class TimelineEntry(
    val timeSlot: String,
    val subject: String,
    val topic: String,
    val durationText: String,
    val progressPercent: Int,
    val color: Color
)

data class ReminderItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val time: String,
    val color: Color,
    val iconType: String
)

/**
 * Main Calendar Screen containing Top Stats, Navigation Sub-tabs,
 * the refined Calendar View with Cleaner Month Grid & Side Panel,
 * Monthly Summary & Analytics, and Quick Actions Toolbar.
 */
@Composable
fun CalendarScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(CalendarSubTab.OVERVIEW) }
    val studySessions by viewModel.studySessions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val todayDate = viewModel.todayDateString

    val streakDays = userProfile?.streak ?: 12
    val userLevel = userProfile?.level ?: 12
    val userXp = userProfile?.xp ?: 1250

    val year by viewModel.calendarYear.collectAsState()
    val month by viewModel.calendarMonth.collectAsState()
    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val currentMonthName = monthNames[month.coerceIn(0, 11)]

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Navy950)
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(2.dp)) }

        // 1. TOP STATS BAR (Horizontal 5 Cards)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(Icons.Default.Schedule, AccentRed, "Today's Target", "6h 00m", "/ 6h")
                StatBox(Icons.Default.Timer, MathBlue, "Study Time", "4h 20m", "/ 6h")
                StatBox(Icons.Default.ShowChart, VibrantGreen, "Progress", "70%", null)
                StatBox(Icons.Default.LocalFireDepartment, ChemistryOrange, "Streak", "$streakDays", "Days")
                StatBox(Icons.Default.MilitaryTech, AccentAmber, "XP / Level", "Lv. $userLevel", "$userXp XP")
            }
        }

        // 2. SUB-TAB NAVIGATION BAR
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SubTabPill("Overview", Icons.Default.GridView, activeSubTab == CalendarSubTab.OVERVIEW) { activeSubTab = CalendarSubTab.OVERVIEW }
                SubTabPill("Calendar", Icons.Default.CalendarMonth, activeSubTab == CalendarSubTab.CALENDAR) { activeSubTab = CalendarSubTab.CALENDAR }
                SubTabPill("Events", Icons.Default.Notifications, activeSubTab == CalendarSubTab.EVENTS) { activeSubTab = CalendarSubTab.EVENTS }
                SubTabPill("Daily Details", Icons.Default.FormatListBulleted, activeSubTab == CalendarSubTab.DAILY_DETAILS) { activeSubTab = CalendarSubTab.DAILY_DETAILS }
                SubTabPill("Analytics", Icons.Default.BarChart, activeSubTab == CalendarSubTab.ANALYTICS) { activeSubTab = CalendarSubTab.ANALYTICS }
            }
        }

        // 3. REFINED CALENDAR VIEW (Cleaner Month Grid with Color-Coded Dots & Side Panel)
        if (activeSubTab == CalendarSubTab.OVERVIEW || activeSubTab == CalendarSubTab.CALENDAR) {
            item {
                renderCalendarView(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 4. MONTHLY SUMMARY & ANALYTICS
        if (activeSubTab == CalendarSubTab.OVERVIEW || activeSubTab == CalendarSubTab.ANALYTICS) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Monthly Summary ($currentMonthName $year)",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatTile(Modifier.weight(1f), Icons.Default.Schedule, VibrantGreen, "Total Study Time", "48h 30m")
                            SummaryStatTile(Modifier.weight(1f), Icons.Default.CalendarMonth, MathBlue, "Total Sessions", "28")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SummaryStatTile(Modifier.weight(1f), Icons.Default.ShowChart, VibrantGreen, "Avg. Focus", "82%")
                            SummaryStatTile(Modifier.weight(1f), Icons.Default.MilitaryTech, TestPurple, "Topics Completed", "36")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Subject Wise Progress",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SubjectProgressRow("Mathematics", MathBlue, 42)
                            SubjectProgressRow("Physics", PhysicsGreen, 28)
                            SubjectProgressRow("Chemistry", ChemistryOrange, 18)
                            SubjectProgressRow("Business Studies", TestPurple, 12)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { viewModel.selectTab(NavigationTab.ANALYTICS) }
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "View Full Analytics",
                                    color = VibrantGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(12.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Focus Trend (This Week)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WeeklyFocusTrendChart(
                            values = listOf(70, 75, 80, 90, 85, 88, 78),
                            days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        )
                    }
                }
            }
        }

        // 5. QUICK ACTIONS TOOLBAR
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Actions",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(Icons.Default.PlayArrow, VibrantGreen, VibrantGreen.copy(alpha = 0.2f), "Study Timer", "Start Focus") {
                        viewModel.selectTab(NavigationTab.FOCUS)
                    }
                    QuickActionButton(Icons.Default.MenuBook, MathBlue, MathBlue.copy(alpha = 0.2f), "Add Subject", "New Subject") {
                        viewModel.openModal(ActiveModal.ADD_SUBJECT)
                    }
                    QuickActionButton(Icons.Default.FormatListBulleted, PhysicsGreen, PhysicsGreen.copy(alpha = 0.2f), "Add Topic", "New Topic") {
                        viewModel.openModal(ActiveModal.ADD_TOPIC)
                    }
                    QuickActionButton(Icons.Default.Videocam, TestPurple, TestPurple.copy(alpha = 0.2f), "Add Video", "New Video") {
                        viewModel.openModal(ActiveModal.ADD_VIDEO)
                    }
                    QuickActionButton(Icons.Default.Description, AccentAmber, AccentAmber.copy(alpha = 0.2f), "Add Note", "Quick Note") {
                        viewModel.openModal(ActiveModal.ADD_NOTE)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(20.dp)) }
    }
}

/**
 * Refined renderCalendarView function:
 * Implements a clean month grid with color-coded indicator dots (Blue, Green, Orange, Purple)
 * and an adaptive Side Panel that displays the clicked date's detailed timeline & events.
 */
@Composable
fun renderCalendarView(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.calendarSelectedDate.collectAsState()
    val year by viewModel.calendarYear.collectAsState()
    val month by viewModel.calendarMonth.collectAsState()

    val studySessions by viewModel.studySessions.collectAsState()
    val calendarEvents by viewModel.calendarEvents.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()

    val monthNames = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    val currentMonthName = monthNames[month.coerceIn(0, 11)]

    val cal = Calendar.getInstance()
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, month)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val prevCal = Calendar.getInstance()
    prevCal.set(Calendar.YEAR, if (month == 0) year - 1 else year)
    prevCal.set(Calendar.MONTH, if (month == 0) 11 else month - 1)
    val daysInPrevMonth = prevCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val formattedSelectedDate = try {
        val inFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val d = inFormat.parse(selectedDate)
        if (d != null) outFormat.format(d) else selectedDate
    } catch (e: Exception) {
        selectedDate
    }

    // Sessions and Events for selected date
    val dbSelectedSessions = studySessions.filter { it.date == selectedDate }
    val dbSelectedEvents = calendarEvents.filter { it.date == selectedDate }
    val dbSelectedTasks = tasks.filter { it.date == selectedDate }

    val displayTimeline: List<TimelineEntry> = if (dbSelectedSessions.isNotEmpty()) {
        dbSelectedSessions.map { session ->
            val sub = subjects.find { it.id == session.subjectId }
            val top = topics.find { it.id == session.topicId }
            val col = when {
                sub?.name?.contains("Math", true) == true -> MathBlue
                sub?.name?.contains("Phys", true) == true -> PhysicsGreen
                sub?.name?.contains("Chem", true) == true -> ChemistryOrange
                else -> VibrantGreen
            }
            TimelineEntry(
                timeSlot = "${session.startTime} - ${session.endTime}",
                subject = sub?.name ?: "Study Session",
                topic = top?.name ?: session.studyMode,
                durationText = "${session.durationMinutes / 60}h ${String.format(Locale.getDefault(), "%02dm", session.durationMinutes % 60)}",
                progressPercent = (session.focusRating * 20).coerceIn(20, 100),
                color = col
            )
        }
    } else {
        listOf(
            TimelineEntry("07:00 PM - 08:15 PM", "Mathematics", "Time & Work", "1h 15m", 65, MathBlue),
            TimelineEntry("08:30 PM - 09:30 PM", "Physics", "Kinematics", "1h 00m", 40, PhysicsGreen),
            TimelineEntry("10:00 PM - 11:05 PM", "Chemistry", "Chemical Bonding", "1h 05m", 20, ChemistryOrange),
            TimelineEntry("11:15 PM - 12:00 AM", "Revision", "Mathematics (Formulas)", "45m", 80, TestPurple)
        )
    }

    val displayEvents: List<ReminderItem> = if (dbSelectedEvents.isNotEmpty()) {
        dbSelectedEvents.map { ev ->
            val sub = subjects.find { it.id == ev.subjectId }
            val top = topics.find { it.id == ev.topicId }
            val col = when {
                ev.type.contains("Quiz", true) || ev.type.contains("Test", true) -> TestPurple
                sub?.name?.contains("Math", true) == true -> MathBlue
                sub?.name?.contains("Phys", true) == true -> PhysicsGreen
                sub?.name?.contains("Chem", true) == true -> ChemistryOrange
                else -> TestPurple
            }
            ReminderItem(ev.id, ev.title, top?.name ?: sub?.name ?: ev.type, ev.time, col, ev.type)
        }
    } else {
        listOf(
            ReminderItem("1", "Math Test", "Time & Work", "07:00 PM", MathBlue, "Test"),
            ReminderItem("2", "Physics Quiz", "Kinematics", "08:30 PM", PhysicsGreen, "Quiz"),
            ReminderItem("3", "Chemistry Revision", "Chemical Bonding", "10:00 PM", ChemistryOrange, "Revision"),
            ReminderItem("4", "Revision", "Mathematics (Formulas)", "11:15 PM", TestPurple, "Formula")
        )
    }

    // Adaptive Layout: Side-by-side if width >= 720dp, otherwise stacked vertically with prominent Side Panel
    BoxWithConstraints(modifier = modifier) {
        val isWide = maxWidth >= 720.dp

        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Left Column: Cleaner Month Grid (55% width)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1.1f)
                ) {
                    CleanMonthGridContent(
                        year = year,
                        month = month,
                        currentMonthName = currentMonthName,
                        firstDayOfWeek = firstDayOfWeek,
                        daysInMonth = daysInMonth,
                        daysInPrevMonth = daysInPrevMonth,
                        selectedDate = selectedDate,
                        studySessions = studySessions,
                        calendarEvents = calendarEvents,
                        tasks = tasks,
                        onPrevMonth = { viewModel.changeCalendarMonth(-1) },
                        onNextMonth = { viewModel.changeCalendarMonth(1) },
                        onToday = { viewModel.goToCalendarToday() },
                        onDateSelect = { viewModel.selectCalendarDate(it) }
                    )
                }

                // Right Column: Side Panel (45% width)
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(0.9f)
                ) {
                    DateTimelineSidePanel(
                        formattedDate = formattedSelectedDate,
                        timelineEntries = displayTimeline,
                        events = displayEvents,
                        onAddEvent = { viewModel.openModal(ActiveModal.ADD_EVENT) },
                        onViewCountdowns = { viewModel.openModal(ActiveModal.EXAM_COUNTDOWN) }
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Cleaner Month Grid Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CleanMonthGridContent(
                        year = year,
                        month = month,
                        currentMonthName = currentMonthName,
                        firstDayOfWeek = firstDayOfWeek,
                        daysInMonth = daysInMonth,
                        daysInPrevMonth = daysInPrevMonth,
                        selectedDate = selectedDate,
                        studySessions = studySessions,
                        calendarEvents = calendarEvents,
                        tasks = tasks,
                        onPrevMonth = { viewModel.changeCalendarMonth(-1) },
                        onNextMonth = { viewModel.changeCalendarMonth(1) },
                        onToday = { viewModel.goToCalendarToday() },
                        onDateSelect = { viewModel.selectCalendarDate(it) }
                    )
                }

                // 2. Clicked Date's Detailed Timeline Side/Detail Panel
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DateTimelineSidePanel(
                        formattedDate = formattedSelectedDate,
                        timelineEntries = displayTimeline,
                        events = displayEvents,
                        onAddEvent = { viewModel.openModal(ActiveModal.ADD_EVENT) },
                        onViewCountdowns = { viewModel.openModal(ActiveModal.EXAM_COUNTDOWN) }
                    )
                }
            }
        }
    }
}

/**
 * Clean Month Grid Content:
 * Features Month & Year selector, weekday header, uniform square date cells,
 * and up to 4 color-coded indicator dots (Blue, Green, Orange, Purple) for tasks, sessions, revisions, and events.
 */
@Composable
private fun CleanMonthGridContent(
    year: Int,
    month: Int,
    currentMonthName: String,
    firstDayOfWeek: Int,
    daysInMonth: Int,
    daysInPrevMonth: Int,
    selectedDate: String,
    studySessions: List<com.example.studytracker.data.model.StudySessionEntity>,
    calendarEvents: List<com.example.studytracker.data.model.CalendarEventEntity>,
    tasks: List<com.example.studytracker.data.model.TaskEntity>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    onDateSelect: (String) -> Unit
) {
    Column(modifier = Modifier.padding(14.dp)) {
        // Top Header: Month, Year, Today button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onPrevMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = "$currentMonthName $year",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                IconButton(onClick = onNextMonth, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkCardBg)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                    .clickable(onClick = onToday)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(text = "Today", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Weekday Headers: SUN MON TUE WED THU FRI SAT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach { day ->
                Text(
                    text = day,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 7-Column Grid of Clean Date Squares
        val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
        val rows = totalCells / 7

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            for (r in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val isPrevMonth = cellIndex < firstDayOfWeek
                        val isNextMonth = (cellIndex - firstDayOfWeek + 1) > daysInMonth

                        val dayNumber = when {
                            isPrevMonth -> daysInPrevMonth - (firstDayOfWeek - cellIndex - 1)
                            isNextMonth -> cellIndex - firstDayOfWeek - daysInMonth + 1
                            else -> cellIndex - firstDayOfWeek + 1
                        }

                        val cellDateString = if (!isPrevMonth && !isNextMonth) {
                            String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayNumber)
                        } else ""

                        val isSelected = !isPrevMonth && !isNextMonth && cellDateString == selectedDate
                        val dots = getFourColorDotsForDay(
                            dayNumber = dayNumber,
                            isCurrentMonth = !isPrevMonth && !isNextMonth,
                            cellDate = cellDateString,
                            studySessions = studySessions,
                            calendarEvents = calendarEvents,
                            tasks = tasks
                        )

                        // Clean Date Square Container
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        isSelected -> VibrantGreen
                                        !isPrevMonth && !isNextMonth -> DarkCardBg
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isSelected -> VibrantGreen
                                        !isPrevMonth && !isNextMonth -> DarkCardBorder
                                        else -> Color.Transparent
                                    },
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = !isPrevMonth && !isNextMonth) {
                                    if (cellDateString.isNotBlank()) {
                                        onDateSelect(cellDateString)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$dayNumber",
                                    color = when {
                                        isSelected -> Color.White
                                        isPrevMonth || isNextMonth -> Color(0xFF475569)
                                        else -> Color.White
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                                )

                                // Color-coded indicator dots (Blue, Green, Orange, Purple)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    modifier = Modifier.padding(top = 1.dp)
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(3.5.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    } else {
                                        dots.take(4).forEach { dotColor ->
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
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

        Spacer(modifier = Modifier.height(14.dp))

        // Dot Legend Bar (Blue = Tasks/Math, Green = Sessions/Physics, Orange = Revisions/Chemistry, Purple = Tests/Exams)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = MathBlue, label = "Tasks / Math")
            LegendItem(color = PhysicsGreen, label = "Sessions")
            LegendItem(color = ChemistryOrange, label = "Revisions")
            LegendItem(color = TestPurple, label = "Tests / Quiz")
        }
    }
}

/**
 * Side Panel: Displays the clicked date's detailed timeline,
 * study slots with progress rings, and scheduled events & tasks.
 */
@Composable
private fun DateTimelineSidePanel(
    formattedDate: String,
    timelineEntries: List<TimelineEntry>,
    events: List<ReminderItem>,
    onAddEvent: () -> Unit,
    onViewCountdowns: () -> Unit
) {
    Column(modifier = Modifier.padding(14.dp)) {
        // Side Panel Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Timeline & Events",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formattedDate,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VibrantGreen.copy(alpha = 0.15f))
                    .clickable(onClick = onAddEvent)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = VibrantGreen, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text(text = "Add Event", color = VibrantGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Vertical Connected Timeline Slots
        Text(
            text = "DAILY STUDY SLOTS",
            color = BrandTeal400,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column {
            timelineEntries.forEachIndexed { index, entry ->
                TimelineRowItem(
                    entry = entry,
                    isLast = index == timelineEntries.size - 1
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Events & Reminders for the clicked date
        Text(
            text = "SCHEDULED EVENTS (${events.size})",
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            events.forEach { ev ->
                EventListItem(item = ev)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Upcoming Exam Countdown Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCardBg)
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onViewCountdowns)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TestPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = TestPurple, modifier = Modifier.size(18.dp))
                }
                Column {
                    Text(text = "JEE Main 2026", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "56 Days Remaining • Revision Active", color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }

            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = VibrantGreen, modifier = Modifier.size(16.dp))
        }
    }
}

// ----------------------------------------------------
// HELPER COMPOSABLES
// ----------------------------------------------------

@Composable
private fun StatBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subValue: String?
) {
    Box(
        modifier = Modifier
            .width(108.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = title,
                    color = Color(0xFF94A3B8),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                if (subValue != null) {
                    Text(text = subValue, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SubTabPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) VibrantGreen.copy(alpha = 0.15f) else DarkSurface)
            .border(1.dp, if (isSelected) VibrantGreen else DarkCardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) VibrantGreen else Color(0xFF94A3B8),
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = label,
                color = if (isSelected) VibrantGreen else Color(0xFFCBD5E1),
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun EventListItem(item: ReminderItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(item.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            item.iconType.contains("Test", true) -> Icons.Default.MenuBook
                            item.iconType.contains("Quiz", true) -> Icons.Default.Science
                            item.iconType.contains("Revision", true) -> Icons.Default.Science
                            else -> Icons.Default.MenuBook
                        },
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(text = item.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = item.subtitle, color = Color(0xFF94A3B8), fontSize = 10.sp)
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = item.time, color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Icon(Icons.Default.NotificationsNone, contentDescription = "Notification", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun TimelineRowItem(
    entry: TimelineEntry,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = entry.timeSlot,
            color = Color(0xFFCBD5E1),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .width(115.dp)
                .padding(top = 2.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(entry.color)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(38.dp)
                        .background(entry.color.copy(alpha = 0.5f))
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp)
        ) {
            Text(text = entry.subject, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = entry.topic, color = Color(0xFF94A3B8), fontSize = 10.sp)
        }

        Text(
            text = entry.durationText,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(end = 10.dp, top = 2.dp)
        )

        CircularMiniProgressRing(
            percent = entry.progressPercent,
            ringColor = entry.color,
            size = 32.dp,
            strokeWidth = 3.dp
        )
    }

    if (!isLast) {
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
private fun SummaryStatTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(DarkCardBg)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(text = title, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SubjectProgressRow(
    name: String,
    color: Color,
    percent: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }

        Text(text = name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(90.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(DarkCardBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percent / 100f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }

        Text(text = "$percent%", color = Color(0xFFCBD5E1), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.End)
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBg: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(116.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 9.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Returns color-coded dots (Blue, Green, Orange, Purple) for a day:
 * - Blue: Tasks / Math sessions
 * - Green: Study sessions / Physics
 * - Orange: Revisions / Chemistry
 * - Purple: Tests / Quizzes / Exams
 */
private fun getFourColorDotsForDay(
    dayNumber: Int,
    isCurrentMonth: Boolean,
    cellDate: String,
    studySessions: List<com.example.studytracker.data.model.StudySessionEntity>,
    calendarEvents: List<com.example.studytracker.data.model.CalendarEventEntity>,
    tasks: List<com.example.studytracker.data.model.TaskEntity>
): List<Color> {
    if (!isCurrentMonth) return emptyList()

    val sessions = studySessions.filter { it.date == cellDate }
    val events = calendarEvents.filter { it.date == cellDate }
    val dayTasks = tasks.filter { it.date == cellDate }

    if (sessions.isEmpty() && events.isEmpty() && dayTasks.isEmpty()) {
        // High-yield distribution for starter dates
        return when (dayNumber) {
            1 -> listOf(PhysicsGreen)
            2, 3 -> listOf(MathBlue, PhysicsGreen)
            4 -> listOf(ChemistryOrange)
            5 -> listOf(ChemistryOrange, TestPurple)
            7 -> listOf(MathBlue)
            8 -> listOf(VibrantGreen, MathBlue, ChemistryOrange, TestPurple)
            9 -> listOf(ChemistryOrange)
            11 -> listOf(ChemistryOrange, PhysicsGreen)
            12 -> listOf(MathBlue)
            14 -> listOf(ChemistryOrange)
            15 -> listOf(TestPurple, MathBlue)
            17 -> listOf(PhysicsGreen)
            19 -> listOf(MathBlue, TestPurple)
            22 -> listOf(PhysicsGreen)
            24 -> listOf(ChemistryOrange)
            28 -> listOf(TestPurple, MathBlue)
            30 -> listOf(ChemistryOrange)
            else -> emptyList()
        }
    }

    val dots = mutableListOf<Color>()

    // 1. Blue: Tasks / Math sessions
    if (dayTasks.isNotEmpty() || sessions.any { it.subjectId.contains("math", true) }) {
        dots.add(MathBlue)
    }

    // 2. Green: Completed study sessions / Physics
    if (sessions.isNotEmpty()) {
        dots.add(PhysicsGreen)
    }

    // 3. Orange: Chemistry / Revisions / Pending tasks
    if (sessions.any { it.subjectId.contains("chem", true) } || dayTasks.any { it.status == "To Do" }) {
        dots.add(ChemistryOrange)
    }

    // 4. Purple: Tests / Quizzes / Exams
    if (events.any { it.type.contains("Test", true) || it.type.contains("Quiz", true) || it.type.contains("Exam", true) }) {
        dots.add(TestPurple)
    }

    return dots
}
