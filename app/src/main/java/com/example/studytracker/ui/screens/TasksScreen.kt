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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.data.model.TaskEntity
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
fun TasksScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val pendingTasks = tasks.filter { it.status != "Completed" }
    val completedTasks = tasks.filter { it.status == "Completed" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Header & Add Task Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Task Manager",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${pendingTasks.size} Pending • ${completedTasks.size} Completed",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.openModal(ActiveModal.ADD_TASK) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTeal500,
                        contentColor = Navy950
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_task_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New Task",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Active Tasks Section
        if (pendingTasks.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "✨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No pending tasks",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap 'New Task' to organize your daily syllabus items.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        } else {
            items(pendingTasks, key = { it.id }) { task ->
                val sub = subjects.find { it.id == task.subjectId }
                TaskItemCard(
                    task = task,
                    subjectName = sub?.name,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        // Completed Tasks Section
        if (completedTasks.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "COMPLETED TASKS (${completedTasks.size})",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            items(completedTasks, key = { it.id }) { task ->
                val sub = subjects.find { it.id == task.subjectId }
                TaskItemCard(
                    task = task,
                    subjectName = sub?.name,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onDelete = { viewModel.deleteTask(task) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    subjectName: String?,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == "Completed"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) DarkSurface.copy(alpha = 0.5f) else DarkSurface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleCompletion() },
                colors = CheckboxDefaults.colors(
                    checkedColor = BrandTeal500,
                    checkmarkColor = Navy950,
                    uncheckedColor = Color(0xFF64748B)
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (isDone) Color(0xFF94A3B8) else Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = task.date,
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                    if (task.time.isNotBlank()) {
                        Text(text = "•", color = Color(0xFF64748B), fontSize = 10.sp)
                        Text(text = task.time, color = Color(0xFF94A3B8), fontSize = 10.sp)
                    }
                    if (!subjectName.isNullOrBlank()) {
                        Text(text = "•", color = Color(0xFF64748B), fontSize = 10.sp)
                        Text(text = subjectName, color = BrandTeal400, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Priority Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        when (task.priority) {
                            "High" -> AccentRed.copy(alpha = 0.2f)
                            "Medium" -> AccentAmber.copy(alpha = 0.2f)
                            else -> DarkCardBg
                        }
                    )
                    .border(
                        1.dp,
                        when (task.priority) {
                            "High" -> AccentRed.copy(alpha = 0.5f)
                            "Medium" -> AccentAmber.copy(alpha = 0.5f)
                            else -> DarkCardBorder
                        },
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
                Text(
                    text = task.priority,
                    color = when (task.priority) {
                        "High" -> Color(0xFFFCA5A5)
                        "Medium" -> AccentAmber
                        else -> Color(0xFFCBD5E1)
                    },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Task",
                    tint = Color(0xFF64748B),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
