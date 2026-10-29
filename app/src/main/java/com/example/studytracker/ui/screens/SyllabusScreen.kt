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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircleOutline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.data.model.SubjectEntity
import com.example.studytracker.data.model.TopicEntity
import com.example.studytracker.data.model.VideoLessonEntity
import com.example.studytracker.ui.components.formatDecimal
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950

@Composable
fun SyllabusScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val videos by viewModel.videos.collectAsState()

    val totalTopics = topics.size
    val completedTopics = topics.count { it.status == "Completed" }
    val syllabusPercent = if (totalTopics > 0) ((completedTopics.toFloat() / totalTopics.toFloat()) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Header & Add Subject Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Syllabus Architecture",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Overall Progress: $syllabusPercent% ($completedTopics/$totalTopics topics)",
                        color = BrandTeal400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = { viewModel.openModal(ActiveModal.ADD_SUBJECT) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_subject_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Subject",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overall Syllabus Progress Bar
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(DarkCardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(syllabusPercent / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(BrandTeal400)
                )
            }
        }

        // Subject Cards List
        if (subjects.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "📚", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No subjects created yet",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Subject' above to construct your study curriculum.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(subjects, key = { it.id }) { subject ->
                SubjectCardItem(
                    subject = subject,
                    topics = topics.filter { it.subjectId == subject.id },
                    videos = videos,
                    onEditSubject = {
                        viewModel.editingSubject.value = subject
                        viewModel.openModal(ActiveModal.EDIT_SUBJECT)
                    },
                    onAddTopic = {
                        viewModel.targetSubjectIdForNewTopic.value = subject.id
                        viewModel.openModal(ActiveModal.ADD_TOPIC)
                    },
                    onOpenTopic = { topic ->
                        viewModel.openTopicDetail(topic)
                    }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
fun SubjectCardItem(
    subject: SubjectEntity,
    topics: List<TopicEntity>,
    videos: List<VideoLessonEntity>,
    onEditSubject: () -> Unit,
    onAddTopic: () -> Unit,
    onOpenTopic: (TopicEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedTopics = topics.count { it.status == "Completed" }
    val progressPercent = if (topics.isNotEmpty()) ((completedTopics.toFloat() / topics.size.toFloat()) * 100).toInt() else 0
    val totalHoursCompleted = topics.sumOf { it.completedHours.toDouble() }.toFloat()

    val subjectColor = try {
        Color(android.graphics.Color.parseColor(subject.colorHex))
    } catch (e: Exception) {
        BrandTeal400
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Color dot, Subject Name, Target hours, Priority pill, Edit icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                    Column {
                        Text(
                            text = subject.name,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${topics.size} Topics • ${formatDecimal(totalHoursCompleted)} / ${subject.targetHours.toInt()}h Target",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (subject.priority) {
                                    "High" -> AccentRed.copy(alpha = 0.2f)
                                    "Medium" -> AccentAmber.copy(alpha = 0.2f)
                                    else -> DarkCardBg
                                }
                            )
                            .border(
                                1.dp,
                                when (subject.priority) {
                                    "High" -> AccentRed.copy(alpha = 0.5f)
                                    "Medium" -> AccentAmber.copy(alpha = 0.5f)
                                    else -> DarkCardBorder
                                },
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = subject.priority,
                            color = when (subject.priority) {
                                "High" -> Color(0xFFFCA5A5)
                                "Medium" -> AccentAmber
                                else -> Color(0xFFCBD5E1)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onEditSubject,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Subject",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Navy950)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressPercent / 100f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(subjectColor)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Topics list inside this subject
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                topics.forEach { topic ->
                    val topicVideos = videos.filter { it.topicId == topic.id }
                    val completedVids = topicVideos.count { it.isCompleted }
                    val vidPercent = if (topicVideos.isNotEmpty()) ((completedVids.toFloat() / topicVideos.size.toFloat()) * 100).toInt() else 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy950.copy(alpha = 0.8f))
                            .border(1.dp, DarkCardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { onOpenTopic(topic) }
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (topic.status == "Completed") Icons.Default.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (topic.status == "Completed") BrandTeal400 else Color(0xFF64748B),
                                modifier = Modifier.size(16.dp)
                            )
                            Column {
                                Text(
                                    text = topic.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${topicVideos.size} Videos ($completedVids watched) • ${topic.status}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$vidPercent%",
                                color = BrandTeal400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "›",
                                color = Color(0xFF64748B),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer of Subject Card: + Add Topic & Exam info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+ Add Topic",
                    color = BrandTeal400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onAddTopic() }
                        .padding(vertical = 4.dp)
                )

                if (!subject.examName.isNullOrBlank()) {
                    Text(
                        text = "Exam: ${subject.examName}",
                        color = AccentAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
