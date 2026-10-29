package com.example.studytracker.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studytracker.data.model.StudyNoteEntity
import com.example.studytracker.ui.components.defaultTextFieldColors
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.BrandTeal500
import com.example.ui.theme.DarkCardBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MathBlue
import com.example.ui.theme.Navy950
import com.example.ui.theme.PhysicsGreen
import com.example.ui.theme.TestPurple
import com.example.ui.theme.VibrantGreen

@Composable
fun NotesScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.notes.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val topics by viewModel.topics.collectAsState()

    val searchQuery by viewModel.notesSearchQuery.collectAsState()
    val selectedSubjectFilter by viewModel.selectedNoteSubjectId.collectAsState()

    val context = LocalContext.current

    // Filter notes by search query and selected subject
    val filteredNotes = notes.filter { note ->
        val matchesSearch = searchQuery.isBlank() ||
                note.title.contains(searchQuery, ignoreCase = true) ||
                note.content.contains(searchQuery, ignoreCase = true)
        val matchesSubject = selectedSubjectFilter == null || note.subjectId == selectedSubjectFilter
        matchesSearch && matchesSubject
    }

    val pinnedNotes = filteredNotes.filter { it.isPinned }
    val regularNotes = filteredNotes.filter { !it.isPinned }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Navy950)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(2.dp)) }

            // ==========================================
            // 1. HEADER & SEARCH
            // ==========================================
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Study Notes & Formulas",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${notes.size} notes stored across subjects",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        }

                        // Add Note button in header
                        Button(
                            onClick = { viewModel.openModal(ActiveModal.ADD_NOTE) },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Note", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setNotesSearchQuery(it) },
                        placeholder = { Text("Search notes, formulas, theorems...", color = Color(0xFF64748B), fontSize = 12.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                        },
                        colors = defaultTextFieldColors(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
                            .testTag("notes_search_input"),
                        singleLine = true
                    )
                }
            }

            // ==========================================
            // 2. SUBJECT FILTER CHIPS
            // ==========================================
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Subjects chip
                    val isAllSelected = selectedSubjectFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isAllSelected) VibrantGreen.copy(alpha = 0.2f) else DarkSurface)
                            .border(1.dp, if (isAllSelected) VibrantGreen else DarkCardBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setSelectedNoteSubjectFilter(null) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "All Subjects",
                            color = if (isAllSelected) VibrantGreen else Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    subjects.forEach { sub ->
                        val isSelected = selectedSubjectFilter == sub.id
                        val subColor = when {
                            sub.name.contains("Math", true) -> MathBlue
                            sub.name.contains("Phys", true) -> PhysicsGreen
                            else -> TestPurple
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) subColor.copy(alpha = 0.2f) else DarkSurface)
                                .border(1.dp, if (isSelected) subColor else DarkCardBorder, RoundedCornerShape(10.dp))
                                .clickable { viewModel.setSelectedNoteSubjectFilter(sub.id) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(subColor)
                                )
                                Text(
                                    text = sub.name,
                                    color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // ==========================================
            // 3. PINNED NOTES SECTION
            // ==========================================
            if (pinnedNotes.isNotEmpty()) {
                item {
                    Text(
                        text = "PINNED FORMULAS & SHORTCUTS 📌",
                        color = AccentAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                items(pinnedNotes, key = { it.id }) { note ->
                    val sub = subjects.find { it.id == note.subjectId }
                    val top = topics.find { it.id == note.topicId }
                    NoteCardItem(
                        note = note,
                        subjectName = sub?.name,
                        topicName = top?.name,
                        onTogglePin = { viewModel.toggleNotePin(note) },
                        onDelete = { viewModel.deleteNote(note) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Study Note", "${note.title}\n\n${note.content}"))
                            viewModel.showToast("Copied note to clipboard!")
                        }
                    )
                }
            }

            // ==========================================
            // 4. REGULAR NOTES LIST
            // ==========================================
            item {
                Text(
                    text = "ALL NOTES (${regularNotes.size})",
                    color = BrandTeal400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            if (regularNotes.isEmpty() && pinnedNotes.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No notes found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Add your formula sheets, definitions, and class summaries.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                }
            } else {
                items(regularNotes, key = { it.id }) { note ->
                    val sub = subjects.find { it.id == note.subjectId }
                    val top = topics.find { it.id == note.topicId }
                    NoteCardItem(
                        note = note,
                        subjectName = sub?.name,
                        topicName = top?.name,
                        onTogglePin = { viewModel.toggleNotePin(note) },
                        onDelete = { viewModel.deleteNote(note) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Study Note", "${note.title}\n\n${note.content}"))
                            viewModel.showToast("Copied note to clipboard!")
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(70.dp)) }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { viewModel.openModal(ActiveModal.ADD_NOTE) },
            containerColor = VibrantGreen,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_note")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note", modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun NoteCardItem(
    note: StudyNoteEntity,
    subjectName: String?,
    topicName: String?,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (note.isPinned) AccentAmber.copy(alpha = 0.5f) else DarkCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Subject pill + Date + Pin/Copy/Delete icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (subjectName != null) {
                        val pillColor = when {
                            subjectName.contains("Math", true) -> MathBlue
                            subjectName.contains("Phys", true) -> PhysicsGreen
                            else -> TestPurple
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(pillColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = subjectName,
                                color = pillColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (topicName != null) {
                        Text(
                            text = topicName,
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(110.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Pin Note",
                            tint = if (note.isPinned) AccentAmber else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Note",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Note",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = note.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Content
            Text(
                text = note.content,
                color = Color(0xFFCBD5E1),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = if (isExpanded) 50 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved on ${note.date}",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp
                )
                Text(
                    text = if (isExpanded) "Show Less" else "Tap to Read More",
                    color = BrandTeal400,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
