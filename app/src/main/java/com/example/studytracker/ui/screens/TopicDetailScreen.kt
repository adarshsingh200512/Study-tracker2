package com.example.studytracker.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.data.model.TopicEntity
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

@Composable
fun TopicDetailScreen(
    topic: TopicEntity,
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeTopicDetail()
    }

    val subjects by viewModel.subjects.collectAsState()
    val allVideos by viewModel.videos.collectAsState()
    val allRevisions by viewModel.revisions.collectAsState()
    val allNotes by viewModel.notes.collectAsState()

    val topicVideos = allVideos.filter { it.topicId == topic.id }
    val topicRevisions = allRevisions.filter { it.topicId == topic.id }
    val topicNotes = allNotes.filter { it.topicId == topic.id }
    val parentSubject = subjects.find { it.id == topic.subjectId }

    val completedVideosCount = topicVideos.count { it.isCompleted }
    val remainingVideosCount = topicVideos.size - completedVideosCount
    val videoPercent = if (topicVideos.isNotEmpty()) ((completedVideosCount.toFloat() / topicVideos.size.toFloat()) * 100).toInt() else 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Navy950)
    ) {
        // Top app bar with back button & actions
        Surface(
            color = Navy950,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.closeTopicDetail() },
                    modifier = Modifier.testTag("back_from_topic_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = parentSubject?.name ?: "Subject",
                        color = BrandTeal400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Topic Details & Tracking",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(
                        onClick = {
                            viewModel.editingTopic.value = topic
                            viewModel.openModal(ActiveModal.EDIT_TOPIC)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Topic",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.deleteTopic(topic)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Topic",
                            tint = AccentRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Scrollable Topic Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(6.dp)) }

            // 1. Topic Overview Card
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic.name,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (topic.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = topic.description,
                                        color = Color(0xFF94A3B8),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when (topic.status) {
                                            "Completed" -> AccentEmerald.copy(alpha = 0.2f)
                                            "In Progress" -> BrandTeal900
                                            "Revision Pending" -> AccentAmber.copy(alpha = 0.2f)
                                            else -> DarkCardBg
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        when (topic.status) {
                                            "Completed" -> AccentEmerald.copy(alpha = 0.6f)
                                            "In Progress" -> BrandTeal400.copy(alpha = 0.6f)
                                            "Revision Pending" -> AccentAmber.copy(alpha = 0.6f)
                                            else -> DarkCardBorder
                                        },
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = topic.status,
                                    color = when (topic.status) {
                                        "Completed" -> AccentEmerald
                                        "In Progress" -> BrandTeal400
                                        "Revision Pending" -> AccentAmber
                                        else -> Color(0xFFCBD5E1)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Progress statistics: study time + target date
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Navy950)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "STUDY TIME LOGGED",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${formatDecimal(topic.completedHours)} / ${topic.estimatedHours.toInt()} Hours",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "TARGET COMPLETION",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = topic.targetDate ?: "Open",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. CRITICAL FEATURE: Unlimited Video Lessons Tracking
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
                            Column {
                                Text(
                                    text = "VIDEO LESSONS TRACKING",
                                    color = BrandTeal400,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "${topicVideos.size} Videos • $completedVideosCount Completed • $remainingVideosCount Remaining",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.openModal(ActiveModal.ADD_VIDEO) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandTeal500,
                                    contentColor = Navy950
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("add_video_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Add Video",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Video Completion Progress bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Lesson Completion",
                                color = Color(0xFFCBD5E1),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$videoPercent%",
                                color = BrandTeal400,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Navy950)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(videoPercent / 100f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(BrandTeal400)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Video items list (Unlimited)
                        if (topicVideos.isEmpty()) {
                            Text(
                                text = "No video lessons recorded yet. Tap '+ Add Video' to start tracking your curriculum watch list.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topicVideos.forEach { video ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Navy950)
                                            .border(1.dp, DarkCardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Checkbox(
                                            checked = video.isCompleted,
                                            onCheckedChange = { checked ->
                                                viewModel.toggleVideoCompletion(video, checked)
                                            },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = BrandTeal500,
                                                checkmarkColor = Navy950,
                                                uncheckedColor = Color(0xFF64748B)
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = video.name,
                                                color = if (video.isCompleted) Color(0xFF94A3B8) else Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                textDecoration = if (video.isCompleted) TextDecoration.LineThrough else null
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                if (video.isCompleted) {
                                                    Text(
                                                        text = "✓ Watched: ${video.watchedDate ?: "Yes"}",
                                                        color = BrandTeal400,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                } else {
                                                    Text(
                                                        text = "Not watched",
                                                        color = Color(0xFF64748B),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                Text(
                                                    text = "• ${video.durationMinutes} min",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            if (video.notes.isNotBlank()) {
                                                Text(
                                                    text = "Note: ${video.notes}",
                                                    color = Color(0xFF94A3B8),
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteVideo(video) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Video",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Spaced Retrieval / Revision Schedule Section
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
                            Column {
                                Text(
                                    text = "SPACED REVISION SCHEDULE",
                                    color = AccentAmber,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Active recall to combat the forgetting curve",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp
                                )
                            }

                            Button(
                                onClick = { viewModel.openModal(ActiveModal.ADD_REVISION) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentAmber,
                                    contentColor = Navy950
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("add_revision_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Add Rev",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (topicRevisions.isEmpty()) {
                            Text(
                                text = "No revisions scheduled for this topic yet.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topicRevisions.forEach { rev ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Navy950)
                                            .border(1.dp, DarkCardBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Revision #${rev.revisionNumber}",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(
                                                            if (rev.status == "Completed") AccentEmerald.copy(alpha = 0.2f)
                                                            else AccentAmber.copy(alpha = 0.2f)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = rev.status,
                                                        color = if (rev.status == "Completed") AccentEmerald else AccentAmber,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Scheduled: ${rev.date}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                            if (rev.notes.isNotBlank()) {
                                                Text(
                                                    text = rev.notes,
                                                    color = Color(0xFFCBD5E1),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Button(
                                                onClick = { viewModel.toggleRevisionStatus(rev) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (rev.status == "Completed") DarkCardBg else BrandTeal500,
                                                    contentColor = if (rev.status == "Completed") Color.White else Navy950
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Text(
                                                    text = if (rev.status == "Completed") "Pending" else "Done ✓",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.deleteRevision(rev) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = null,
                                                    tint = Color(0xFF64748B),
                                                    modifier = Modifier.size(14.dp)
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

            // 4. Connected Topic Notes Section
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
                                text = "CONNECTED NOTES",
                                color = BrandTeal400,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Button(
                                onClick = { viewModel.openModal(ActiveModal.ADD_NOTE) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DarkCardBg,
                                    contentColor = Color.White
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("add_topic_note_button")
                            ) {
                                Text(
                                    text = "+ New Note",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (topicNotes.isEmpty()) {
                            Text(
                                text = "No notes recorded for this topic.",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                topicNotes.forEach { note ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Navy950),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = note.title,
                                                    color = Color.White,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = note.date,
                                                    color = Color(0xFF64748B),
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = note.content,
                                                color = Color(0xFFCBD5E1),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Launch Study Timer for this Topic Action Button
            item {
                Button(
                    onClick = {
                        viewModel.startTimerForTopic(topic.subjectId, topic.id)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("launch_timer_for_topic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Launch Study Timer for this Topic",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
    }
}
