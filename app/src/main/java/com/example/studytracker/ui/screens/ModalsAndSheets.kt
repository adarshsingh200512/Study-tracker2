package com.example.studytracker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.studytracker.data.model.SubjectEntity
import com.example.studytracker.data.model.TopicEntity
import com.example.studytracker.ui.components.BadgeItem
import com.example.studytracker.ui.components.defaultTextFieldColors
import com.example.studytracker.ui.components.formatDecimal
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.BrandTeal900
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalsAndSheetsContainer(
    viewModel: StudyViewModel
) {
    val activeModal by viewModel.activeModal.collectAsState()

    when (activeModal) {
        ActiveModal.NONE -> { /* No modal */ }

        ActiveModal.FOCUS_RATING -> {
            FocusRatingDialog(viewModel = viewModel)
        }

        ActiveModal.ADD_SUBJECT -> {
            AddOrEditSubjectSheet(
                subject = null,
                onDismiss = { viewModel.closeModal() },
                onSave = { name, target, color, priority, examDate, examName, notes ->
                    viewModel.addSubject(name, target, color, priority, examDate, examName, notes)
                }
            )
        }

        ActiveModal.EDIT_SUBJECT -> {
            val sub = viewModel.editingSubject.collectAsState().value
            if (sub != null) {
                AddOrEditSubjectSheet(
                    subject = sub,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { name, target, color, priority, examDate, examName, notes ->
                        viewModel.updateSubject(
                            sub.copy(
                                name = name,
                                targetHours = target,
                                colorHex = color,
                                priority = priority,
                                examDate = examDate?.takeIf { it.isNotBlank() },
                                examName = examName?.takeIf { it.isNotBlank() },
                                notes = notes
                            )
                        )
                    },
                    onDelete = {
                        viewModel.deleteSubject(sub)
                    }
                )
            }
        }

        ActiveModal.ADD_TOPIC -> {
            val defaultSubId = viewModel.targetSubjectIdForNewTopic.collectAsState().value
                ?: viewModel.subjects.collectAsState().value.firstOrNull()?.id ?: ""
            AddOrEditTopicSheet(
                topic = null,
                defaultSubjectId = defaultSubId,
                subjects = viewModel.subjects.collectAsState().value,
                onDismiss = { viewModel.closeModal() },
                onSave = { subId, name, desc, est, priority, status, targetDate, notes ->
                    viewModel.addTopic(subId, name, desc, est, priority, status, targetDate, notes)
                }
            )
        }

        ActiveModal.EDIT_TOPIC -> {
            val top = viewModel.editingTopic.collectAsState().value
            if (top != null) {
                AddOrEditTopicSheet(
                    topic = top,
                    defaultSubjectId = top.subjectId,
                    subjects = viewModel.subjects.collectAsState().value,
                    onDismiss = { viewModel.closeModal() },
                    onSave = { subId, name, desc, est, priority, status, targetDate, notes ->
                        viewModel.updateTopic(
                            top.copy(
                                subjectId = subId,
                                name = name,
                                description = desc,
                                estimatedHours = est,
                                priority = priority,
                                status = status,
                                targetDate = targetDate?.takeIf { it.isNotBlank() },
                                notes = notes
                            )
                        )
                    },
                    onDelete = {
                        viewModel.deleteTopic(top)
                    }
                )
            }
        }

        ActiveModal.ADD_VIDEO -> {
            val currentTopic = viewModel.selectedTopicForDetail.collectAsState().value
            if (currentTopic != null) {
                AddVideoDialog(
                    topicId = currentTopic.id,
                    onDismiss = { viewModel.closeModal() },
                    onAdd = { name, duration, isWatched, notes ->
                        viewModel.addVideoLesson(currentTopic.id, name, duration, isWatched, notes)
                    }
                )
            }
        }

        ActiveModal.ADD_REVISION -> {
            val currentTopic = viewModel.selectedTopicForDetail.collectAsState().value
            if (currentTopic != null) {
                AddRevisionDialog(
                    topicId = currentTopic.id,
                    onDismiss = { viewModel.closeModal() },
                    onAdd = { date, notes ->
                        viewModel.addRevision(currentTopic.id, date, notes)
                    }
                )
            }
        }

        ActiveModal.ADD_TASK -> {
            AddTaskSheet(
                subjects = viewModel.subjects.collectAsState().value,
                onDismiss = { viewModel.closeModal() },
                onSave = { title, date, time, duration, priority, subId ->
                    viewModel.addTask(title, date, time, duration, priority, subId, null)
                }
            )
        }

        ActiveModal.ADD_EVENT -> {
            AddCalendarEventSheet(
                defaultDate = viewModel.calendarSelectedDate.collectAsState().value,
                subjects = viewModel.subjects.collectAsState().value,
                onDismiss = { viewModel.closeModal() },
                onSave = { title, type, date, time, subId, notes ->
                    viewModel.addCalendarEvent(title, type, date, time, subId, notes)
                }
            )
        }

        ActiveModal.ADD_NOTE -> {
            val currentTopic = viewModel.selectedTopicForDetail.collectAsState().value
            AddNoteDialog(
                defaultSubjectId = currentTopic?.subjectId,
                defaultTopicId = currentTopic?.id,
                onDismiss = { viewModel.closeModal() },
                onSave = { title, content ->
                    viewModel.addNote(title, content, currentTopic?.subjectId, currentTopic?.id)
                }
            )
        }

        ActiveModal.PLANNER -> {
            SmartPlannerSheet(viewModel = viewModel)
        }

        ActiveModal.DAILY_MISSION -> {
            DailyMissionSheet(viewModel = viewModel)
        }

        ActiveModal.EXAM_COUNTDOWN -> {
            ExamCountdownSheet(viewModel = viewModel)
        }

        ActiveModal.ALL_NOTES -> {
            AllNotesSheet(viewModel = viewModel)
        }

        ActiveModal.GAMIFICATION -> {
            GamificationSheet(viewModel = viewModel)
        }

        ActiveModal.SETTINGS -> {
            SettingsSheet(viewModel = viewModel)
        }
    }
}

// -----------------------------------------------------------------
// 1. FOCUS RATING DIALOG
// -----------------------------------------------------------------
@Composable
fun FocusRatingDialog(viewModel: StudyViewModel) {
    val durationMinutes by viewModel.pendingSessionMinutes.collectAsState()
    var selectedRating by remember { mutableIntStateOf(4) }
    var notesText by remember { mutableStateOf("") }

    val ratingLabels = listOf("", "1 = Very Poor", "2 = Poor", "3 = Average Focus", "4 = Good Focus", "5 = Locked In!")

    Dialog(onDismissRequest = { viewModel.closeModal() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BrandTeal500.copy(alpha = 0.15f))
                        .border(1.dp, BrandTeal400.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎯", fontSize = 24.sp)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Session Complete!",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged $durationMinutes minutes of focused study",
                        color = BrandTeal400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 1 to 5 Stars
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "How was your focus during this block?",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i Stars",
                                tint = if (i <= selectedRating) AccentAmber else DarkCardBorder,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clickable { selectedRating = i }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = ratingLabels[selectedRating],
                        color = AccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Notes input
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Session Notes (Optional)", color = Color(0xFF94A3B8), fontSize = 11.sp) },
                    placeholder = { Text("Key formulas mastered or takeaways...", color = Color(0xFF64748B), fontSize = 11.sp) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.saveCompletedSession(selectedRating, notesText)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("save_session_button")
                ) {
                    Text(
                        text = "Save Session & Claim XP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 2. ADD / EDIT SUBJECT SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditSubjectSheet(
    subject: SubjectEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, target: Float, color: String, priority: String, examDate: String?, examName: String?, notes: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(subject?.name ?: "") }
    var targetHours by remember { mutableStateOf(subject?.targetHours?.toInt()?.toString() ?: "50") }
    var priority by remember { mutableStateOf(subject?.priority ?: "High") }
    var colorHex by remember { mutableStateOf(subject?.colorHex ?: "#0D9488") }
    var examDate by remember { mutableStateOf(subject?.examDate ?: "") }
    var examName by remember { mutableStateOf(subject?.examName ?: "") }
    var notes by remember { mutableStateOf(subject?.notes ?: "") }

    val presetColors = listOf("#0D9488", "#3B82F6", "#8B5CF6", "#F59E0B", "#EF4444", "#10B981")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (subject == null) "Add New Subject" else "Edit Subject",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Subject Name *", color = BrandTeal400) },
                placeholder = { Text("e.g. Mathematics, Physics", color = Color(0xFF64748B)) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("subject_name_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Target Hours", color = BrandTeal400) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Priority", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("High", "Medium", "Low").forEach { p ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (priority == p) BrandTeal500 else Navy950)
                                    .clickable { priority = p }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p,
                                    color = if (priority == p) Navy950 else Color(0xFFCBD5E1),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Accent Color Chooser
            Column {
                Text(text = "Color Accent", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    presetColors.forEach { hex ->
                        val parsedColor = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(
                                    width = if (colorHex == hex) 3.dp else 0.dp,
                                    color = if (colorHex == hex) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("Exam Date (YYYY-MM-DD)", color = BrandTeal400) },
                    placeholder = { Text("2026-11-20", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = examName,
                    onValueChange = { examName = it },
                    label = { Text("Exam Name", color = BrandTeal400) },
                    placeholder = { Text("Midterm / Final", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Syllabus Notes", color = BrandTeal400) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onDelete != null) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed.copy(alpha = 0.2f),
                            contentColor = AccentRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val hours = targetHours.toFloatOrNull() ?: 50f
                            onSave(name, hours, colorHex, priority, examDate, examName, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("save_subject_button")
                ) {
                    Text("Save Subject", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 3. ADD / EDIT TOPIC SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditTopicSheet(
    topic: TopicEntity?,
    defaultSubjectId: String,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (subId: String, name: String, desc: String, estHours: Float, priority: String, status: String, targetDate: String?, notes: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var subjectId by remember { mutableStateOf(topic?.subjectId ?: defaultSubjectId) }
    var name by remember { mutableStateOf(topic?.name ?: "") }
    var description by remember { mutableStateOf(topic?.description ?: "") }
    var estimatedHours by remember { mutableStateOf(topic?.estimatedHours?.toInt()?.toString() ?: "6") }
    var priority by remember { mutableStateOf(topic?.priority ?: "High") }
    var status by remember { mutableStateOf(topic?.status ?: "Not Started") }
    var targetDate by remember { mutableStateOf(topic?.targetDate ?: "") }
    var notes by remember { mutableStateOf(topic?.notes ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (topic == null) "Add Topic / Chapter" else "Edit Topic",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Subject Chooser
            Column {
                Text(text = "Parent Subject", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { s ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (subjectId == s.id) BrandTeal500 else Navy950)
                                .clickable { subjectId = s.id }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.name,
                                color = if (subjectId == s.id) Navy950 else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Topic Name *", color = BrandTeal400) },
                placeholder = { Text("e.g. Time and Work, Calculus", color = Color(0xFF64748B)) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("topic_name_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = estimatedHours,
                    onValueChange = { estimatedHours = it },
                    label = { Text("Estimated Hours", color = BrandTeal400) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date", color = BrandTeal400) },
                    placeholder = { Text("YYYY-MM-DD", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Status Chooser
            Column {
                Text(text = "Status", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Not Started", "In Progress", "Revision Pending", "Completed").forEach { st ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (status == st) BrandTeal500 else Navy950)
                                .clickable { status = st }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = st,
                                color = if (status == st) Navy950 else Color(0xFFCBD5E1),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Objectives & Description", color = BrandTeal400) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (onDelete != null) {
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRed.copy(alpha = 0.2f),
                            contentColor = AccentRed
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(46.dp)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val est = estimatedHours.toFloatOrNull() ?: 6f
                            onSave(subjectId, name, description, est, priority, status, targetDate, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("save_topic_button")
                ) {
                    Text("Save Topic", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 4. ADD VIDEO LESSON DIALOG (CRITICAL FEATURE)
// -----------------------------------------------------------------
@Composable
fun AddVideoDialog(
    topicId: String,
    onDismiss: () -> Unit,
    onAdd: (name: String, duration: Int, isWatched: Boolean, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("30") }
    var isWatched by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Video Lesson",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title *", color = BrandTeal400) },
                    placeholder = { Text("e.g. Video 3 — Practice Problems", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("video_title_input")
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (mins)", color = BrandTeal400) },
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Already Watched?", color = Color(0xFF94A3B8), fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isWatched) BrandTeal500 else Navy950)
                                .clickable { isWatched = !isWatched }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (isWatched) "Yes ✓" else "No ✕",
                                color = if (isWatched) Navy950 else Color(0xFFCBD5E1),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)", color = BrandTeal400) },
                    placeholder = { Text("Key theorem at 12:30...", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            onAdd(title, duration.toIntOrNull() ?: 30, isWatched, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("confirm_add_video_button")
                ) {
                    Text("Add Video to Topic", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 5. ADD REVISION DIALOG
// -----------------------------------------------------------------
@Composable
fun AddRevisionDialog(
    topicId: String,
    onDismiss: () -> Unit,
    onAdd: (date: String, notes: String) -> Unit
) {
    val suggestedDate = remember {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, 3)
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(c.time)
    }

    var date by remember { mutableStateOf(suggestedDate) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Schedule Spaced Revision",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Revision Date (YYYY-MM-DD)", color = AccentAmber) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Revision Focus / Recall Goal", color = AccentAmber) },
                    placeholder = { Text("Re-solve problem set without hints...", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (date.isNotBlank()) {
                            onAdd(date, notes)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Schedule Revision", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 6. ADD CALENDAR EVENT SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCalendarEventSheet(
    defaultDate: String,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, type: String, date: String, time: String, subId: String?, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Test") }
    var date by remember { mutableStateOf(defaultDate) }
    var time by remember { mutableStateOf("10:00 AM") }
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: "") }
    var notes by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Add Event, Test or Reminder",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *", color = AccentAmber) },
                placeholder = { Text("e.g. Calculus Midterm Quiz", color = Color(0xFF64748B)) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Event Type
            Column {
                Text(text = "Event Type", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Test", "Quiz", "Exam", "Assignment", "Reminder").forEach { t ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (type == t) AccentAmber else Navy950)
                                .clickable { type = t }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = t,
                                color = if (type == t) Navy950 else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)", color = AccentAmber) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time", color = AccentAmber) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, type, date, time, selectedSubjectId.takeIf { it.isNotBlank() }, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    contentColor = Navy950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Save Event", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 7. ADD TASK SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskSheet(
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, date: String, time: String, duration: Int, priority: String, subId: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)) }
    var time by remember { mutableStateOf("03:00 PM") }
    var priority by remember { mutableStateOf("Medium") }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Create Daily Task",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Description *", color = BrandTeal400) },
                placeholder = { Text("e.g. Solve 20 rotational dynamics questions", color = Color(0xFF64748B)) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)", color = BrandTeal400) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time", color = BrandTeal400) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Priority
            Column {
                Text(text = "Priority", color = Color(0xFF94A3B8), fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (priority == p) BrandTeal500 else Navy950)
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                color = if (priority == p) Navy950 else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, date, time, 30, priority, selectedSubjectId)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTeal500,
                    contentColor = Navy950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Create Task", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 8. ADD NOTE DIALOG
// -----------------------------------------------------------------
@Composable
fun AddNoteDialog(
    defaultSubjectId: String?,
    defaultTopicId: String?,
    onDismiss: () -> Unit,
    onSave: (title: String, content: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Write Study Note",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title *", color = BrandTeal400) },
                    placeholder = { Text("Formula shortcut, derivation...", color = Color(0xFF64748B)) },
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content *", color = BrandTeal400) },
                    placeholder = { Text("Detailed notes...", color = Color(0xFF64748B)) },
                    minLines = 4,
                    colors = defaultTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onSave(title, content)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Text("Save Note", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -----------------------------------------------------------------
// 9. SMART STUDY PLANNER SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPlannerSheet(viewModel: StudyViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val plannedSessions by viewModel.plannedSessions.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()

    var selectedStrategy by remember { mutableStateOf(userProfile?.priorityMode ?: "High Priority Tag First") }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Study Planner",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.closeModal() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Strategy Picker Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy950),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "PLANNING ALGORITHM STRATEGY",
                        color = BrandTeal400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    listOf(
                        "High Priority Tag First",
                        "Weakest Subject First",
                        "Nearest Exam First"
                    ).forEach { strat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedStrategy == strat) BrandTeal900 else Color.Transparent)
                                .clickable {
                                    selectedStrategy = strat
                                    viewModel.generateSmartWeeklyPlan(strat)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (selectedStrategy == strat) "● " else "○ ",
                                color = if (selectedStrategy == strat) BrandTeal400 else Color(0xFF64748B),
                                fontSize = 14.sp
                            )
                            Text(
                                text = strat,
                                color = if (selectedStrategy == strat) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (selectedStrategy == strat) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Planned Sessions List
            Text(
                text = "ADAPTIVE WEEKLY SCHEDULE",
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            if (plannedSessions.isEmpty()) {
                Button(
                    onClick = { viewModel.generateSmartWeeklyPlan(selectedStrategy) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Text("Generate Adaptive Weekly Plan", fontWeight = FontWeight.Bold)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    plannedSessions.forEach { slot ->
                        val sub = subjects.find { it.id == slot.subjectId }
                        val top = topics.find { it.id == slot.topicId }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Navy950)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = slot.dayOfWeek,
                                    color = BrandTeal400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sub?.name ?: "Subject",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${slot.time} • ${top?.name ?: "All Topics Review"}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = {
                                        viewModel.startTimerForTopic(slot.subjectId, slot.topicId ?: "")
                                        viewModel.closeModal()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandTeal500.copy(alpha = 0.2f),
                                        contentColor = BrandTeal400
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Study Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                                IconButton(
                                    onClick = { viewModel.deletePlannedSession(slot) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFF64748B),
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 10. DAILY MISSION SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMissionSheet(viewModel: StudyViewModel) {
    val studySessions by viewModel.studySessions.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val revisions by viewModel.revisions.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val today = viewModel.todayDateString
    val todayMinutes = studySessions.filter { it.date == today }.sumOf { it.durationMinutes }
    val todayHours = todayMinutes / 60f
    val videosWatchedToday = videos.count { it.watchedDate == today }
    val revisionsDoneToday = revisions.count { it.date == today && it.status == "Completed" }

    val targetHours = (profile?.dailyTargetMinutes ?: 240) / 60f

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Study Mission",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hit targets consistently to level up faster",
                        color = BrandTeal400,
                        fontSize = 11.sp
                    )
                }
                IconButton(onClick = { viewModel.closeModal() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Mission 1: Study hours
            MissionItemCard(
                title = "Study ${targetHours.toInt()} Hours",
                current = "${formatDecimal(todayHours)}h / ${targetHours.toInt()}h",
                isComplete = todayHours >= targetHours
            )

            // Mission 2: Video Lessons
            MissionItemCard(
                title = "Watch 2 Video Lessons",
                current = "$videosWatchedToday / 2 videos",
                isComplete = videosWatchedToday >= 2
            )

            // Mission 3: Revisions
            MissionItemCard(
                title = "Complete 1 Spaced Revision",
                current = "$revisionsDoneToday / 1 revision",
                isComplete = revisionsDoneToday >= 1
            )

            Button(
                onClick = { viewModel.closeModal() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTeal500,
                    contentColor = Navy950
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Text("Keep Studying", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun MissionItemCard(title: String, current: String, isComplete: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Navy950),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = "Current: $current", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }

            Text(
                text = if (isComplete) "✓ Done (+50 XP)" else "Pending",
                color = if (isComplete) AccentEmerald else Color(0xFF64748B),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// -----------------------------------------------------------------
// 11. EXAM COUNTDOWN SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamCountdownSheet(viewModel: StudyViewModel) {
    val subjects by viewModel.subjects.collectAsState()
    val today = viewModel.todayDateString

    val examSubjects = subjects.filter { !it.examDate.isNullOrBlank() }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Exam Countdown",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.closeModal() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            if (examSubjects.isEmpty()) {
                Text(
                    text = "No exams scheduled. You can assign exam dates inside subject settings.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                examSubjects.forEach { sub ->
                    val diffDays = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val examTime = sdf.parse(sub.examDate ?: "")?.time ?: 0L
                        val nowTime = sdf.parse(today)?.time ?: 0L
                        ((examTime - nowTime) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
                    } catch (e: Exception) {
                        0
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Navy950),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = sub.examName ?: "${sub.name} Final",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${sub.name} • ${sub.examDate}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (diffDays <= 7) AccentRed.copy(alpha = 0.2f) else DarkCardBg)
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$diffDays Days Left",
                                    color = if (diffDays <= 7) AccentRed else AccentAmber,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 12. ALL NOTES SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllNotesSheet(viewModel: StudyViewModel) {
    val notes by viewModel.notes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Study Notes (${notes.size})",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { viewModel.openModal(ActiveModal.ADD_NOTE) },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandTeal500, contentColor = Navy950),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("+ Add Note", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (notes.isEmpty()) {
                Text(
                    text = "No study notes written yet.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                notes.forEach { note ->
                    val sub = subjects.find { it.id == note.subjectId }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Navy950),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = note.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(text = note.date, color = Color(0xFF64748B), fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = note.content, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                            if (sub != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Subject: ${sub.name}", color = BrandTeal400, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 13. GAMIFICATION & BADGES SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamificationSheet(viewModel: StudyViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val sessions by viewModel.studySessions.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val revisions by viewModel.revisions.collectAsState()

    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val badges = listOf(
        BadgeItem("First Session", "🌱", sessions.isNotEmpty()),
        BadgeItem("10 Hours Studied", "⏱️", totalMinutes >= 600),
        BadgeItem("7 Day Streak", "🔥", (profile?.streak ?: 0) >= 7),
        BadgeItem("25 Videos Completed", "🎬", videos.count { it.isCompleted } >= 25),
        BadgeItem("10 Topics Mastered", "🏆", topics.count { it.status == "Completed" } >= 10),
        BadgeItem("Revision Master", "🧠", revisions.count { it.status == "Completed" } >= 5)
    )

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Gamification & Badges",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Level ${profile?.level ?: 1} • ${profile?.xp ?: 0} Total XP",
                        color = AccentAmber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { viewModel.closeModal() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Badges Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                badges.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowBadges.forEach { badge ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (badge.isUnlocked) AccentAmber.copy(alpha = 0.1f) else Navy950
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (badge.isUnlocked) AccentAmber.copy(alpha = 0.4f) else DarkCardBorder
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = badge.emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = badge.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                                        color = if (badge.isUnlocked) AccentAmber else Color(0xFF64748B),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -----------------------------------------------------------------
// 14. SETTINGS & STRUCTURED JSON BACKUP / RESTORE SHEET
// -----------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(viewModel: StudyViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var studentName by remember { mutableStateOf(profile?.name ?: "") }
    var targetMinutes by remember { mutableStateOf(profile?.dailyTargetMinutes?.toString() ?: "240") }
    var studyMode by remember { mutableStateOf(profile?.studyMode ?: "Day Study") }
    var priorityMode by remember { mutableStateOf(profile?.priorityMode ?: "High Priority Tag First") }

    var jsonClipboardInput by remember { mutableStateOf("") }
    var showRestoreInput by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = { viewModel.closeModal() },
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Settings & Data Protection",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.closeModal() }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            // Profile Form
            OutlinedTextField(
                value = studentName,
                onValueChange = { studentName = it },
                label = { Text("Student Name", color = BrandTeal400) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = targetMinutes,
                onValueChange = { targetMinutes = it },
                label = { Text("Daily Study Target (Minutes)", color = BrandTeal400) },
                colors = defaultTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    val mins = targetMinutes.toIntOrNull() ?: 240
                    viewModel.updateProfile(studentName, mins, studyMode, priorityMode)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandTeal500, contentColor = Navy950),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(42.dp)
            ) {
                Text("Update Profile Preferences", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // GITHUB REPOSITORY CARD
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy950),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "LINKED GITHUB REPOSITORY",
                            color = BrandTeal400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentEmerald.copy(alpha = 0.15f))
                                .border(1.dp, AccentEmerald.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = AccentEmerald,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Text(
                        text = "adarshsingh200512/Study-tracker2",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owner: singhadarsh200512@gmail.com • Android Study Tracker",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("GitHubRepo", "https://github.com/adarshsingh200512/Study-tracker2")
                            clipboard.setPrimaryClip(clip)
                            viewModel.showToast("GitHub repository URL copied to clipboard!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg, contentColor = BrandTeal400),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandTeal500.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(38.dp)
                    ) {
                        Text("Copy GitHub Repo URL", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BACKUP & RESTORE SECTION
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy950),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "BACKUP & RESTORE (STRUCTURED JSON)",
                        color = AccentAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Export your entire Study Tracker database to JSON or restore from an existing JSON backup.",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.exportBackupJson()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("StudyTrackerBackup", json)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showToast("Backup JSON copied to clipboard!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTeal500, contentColor = Navy950),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Full JSON Backup to Clipboard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { showRestoreInput = !showRestoreInput },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkCardBg, contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore from JSON Backup", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (showRestoreInput) {
                        OutlinedTextField(
                            value = jsonClipboardInput,
                            onValueChange = { jsonClipboardInput = it },
                            label = { Text("Paste JSON Backup string", color = AccentAmber) },
                            minLines = 3,
                            colors = defaultTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (jsonClipboardInput.isNotBlank()) {
                                    viewModel.restoreBackupJson(jsonClipboardInput)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber, contentColor = Navy950),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Validate & Restore Database", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
