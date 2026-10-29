package com.example.studytracker.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Adarsh Singh",
    val dailyTargetMinutes: Int = 240, // 4 hours
    val studyMode: String = "Day Study", // Day Study, Night Study
    val priorityMode: String = "High Priority Tag First", // Weakest Subject First, Nearest Exam First, High Priority Tag First
    val streak: Int = 12,
    val lastStreakDate: String = "",
    val xp: Int = 1250,
    val level: Int = 12
)

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetHours: Float = 50f,
    val colorHex: String = "#0D9488", // Teal default
    val priority: String = "High", // High, Medium, Low
    val examDate: String? = null, // YYYY-MM-DD
    val examName: String? = null,
    val notes: String = "",
    val isEnabled: Boolean = true,
    val orderIndex: Int = 0
)

@Entity(
    tableName = "topics",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("subjectId")]
)
data class TopicEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val name: String,
    val description: String = "",
    val estimatedHours: Float = 6f,
    val completedHours: Float = 0f,
    val priority: String = "High",
    val status: String = "Not Started", // Not Started, In Progress, Revision Pending, Completed
    val targetDate: String? = null, // YYYY-MM-DD
    val notes: String = "",
    val orderIndex: Int = 0
)

@Entity(
    tableName = "video_lessons",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class VideoLessonEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val topicId: String,
    val name: String,
    val isCompleted: Boolean = false,
    val watchedDate: String? = null, // YYYY-MM-DD
    val watchedTime: String? = null, // HH:mm
    val durationMinutes: Int = 30,
    val notes: String = "",
    val orderIndex: Int = 0
)

@Entity(
    tableName = "topic_revisions",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class TopicRevisionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val topicId: String,
    val revisionNumber: Int = 1,
    val date: String, // YYYY-MM-DD
    val status: String = "Pending", // Pending, Completed
    val notes: String = ""
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String, // YYYY-MM-DD
    val startTime: String = "",
    val endTime: String = "",
    val durationMinutes: Int = 25,
    val subjectId: String = "",
    val topicId: String = "",
    val videoId: String? = null,
    val studyMode: String = "Deep Study",
    val focusRating: Int = 4, // 1 to 5
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: String = "Quiz", // Test, Quiz, Exam, Revision, Assignment, Study Session, Reminder
    val date: String, // YYYY-MM-DD
    val time: String = "10:00",
    val subjectId: String? = null,
    val topicId: String? = null,
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val repeatOption: String = "None"
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: String, // YYYY-MM-DD
    val time: String = "",
    val durationMinutes: Int = 30,
    val priority: String = "Medium", // High, Medium, Low
    val subjectId: String? = null,
    val topicId: String? = null,
    val status: String = "To Do" // To Do, In Progress, Completed
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val subjectId: String? = null,
    val topicId: String? = null,
    val videoId: String? = null,
    val date: String, // YYYY-MM-DD
    val title: String,
    val content: String,
    val imageUri: String? = null,
    val isPinned: Boolean = false
)

@Entity(tableName = "planned_sessions")
data class PlannedSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val dayOfWeek: String, // Monday, Tuesday, ...
    val subjectId: String,
    val topicId: String? = null,
    val durationMinutes: Int = 60,
    val time: String = "09:00 AM",
    val date: String = ""
)
