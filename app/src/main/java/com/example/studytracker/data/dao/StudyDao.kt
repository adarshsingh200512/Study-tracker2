package com.example.studytracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studytracker.data.model.CalendarEventEntity
import com.example.studytracker.data.model.PlannedSessionEntity
import com.example.studytracker.data.model.StudyNoteEntity
import com.example.studytracker.data.model.StudySessionEntity
import com.example.studytracker.data.model.SubjectEntity
import com.example.studytracker.data.model.TaskEntity
import com.example.studytracker.data.model.TopicEntity
import com.example.studytracker.data.model.TopicRevisionEntity
import com.example.studytracker.data.model.UserProfileEntity
import com.example.studytracker.data.model.VideoLessonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // --- Subjects ---
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC, name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: String)

    // --- Topics ---
    @Query("SELECT * FROM topics ORDER BY orderIndex ASC, name ASC")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectId = :subjectId ORDER BY orderIndex ASC, name ASC")
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE id = :topicId LIMIT 1")
    suspend fun getTopicById(topicId: String): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity)

    @Update
    suspend fun updateTopic(topic: TopicEntity)

    @Delete
    suspend fun deleteTopic(topic: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :topicId")
    suspend fun deleteTopicById(topicId: String)

    // --- Video Lessons (Unlimited per topic) ---
    @Query("SELECT * FROM video_lessons ORDER BY orderIndex ASC, name ASC")
    fun getAllVideos(): Flow<List<VideoLessonEntity>>

    @Query("SELECT * FROM video_lessons WHERE topicId = :topicId ORDER BY orderIndex ASC, name ASC")
    fun getVideosForTopic(topicId: String): Flow<List<VideoLessonEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoLessonEntity)

    @Update
    suspend fun updateVideo(video: VideoLessonEntity)

    @Delete
    suspend fun deleteVideo(video: VideoLessonEntity)

    @Query("DELETE FROM video_lessons WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: String)

    // --- Topic Revisions ---
    @Query("SELECT * FROM topic_revisions ORDER BY date ASC")
    fun getAllRevisions(): Flow<List<TopicRevisionEntity>>

    @Query("SELECT * FROM topic_revisions WHERE topicId = :topicId ORDER BY date ASC")
    fun getRevisionsForTopic(topicId: String): Flow<List<TopicRevisionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRevision(revision: TopicRevisionEntity)

    @Update
    suspend fun updateRevision(revision: TopicRevisionEntity)

    @Delete
    suspend fun deleteRevision(revision: TopicRevisionEntity)

    @Query("DELETE FROM topic_revisions WHERE id = :revisionId")
    suspend fun deleteRevisionById(revisionId: String)

    // --- Study Sessions ---
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllStudySessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE date = :date ORDER BY timestamp ASC")
    fun getStudySessionsForDate(date: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySessionEntity)

    @Delete
    suspend fun deleteStudySession(session: StudySessionEntity)

    // --- Calendar Events ---
    @Query("SELECT * FROM calendar_events ORDER BY date ASC, time ASC")
    fun getAllCalendarEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY time ASC")
    fun getCalendarEventsForDate(date: String): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalendarEvent(event: CalendarEventEntity)

    @Update
    suspend fun updateCalendarEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteCalendarEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteCalendarEventById(eventId: String)

    // --- Tasks ---
    @Query("SELECT * FROM tasks ORDER BY date ASC, priority DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    // --- Study Notes ---
    @Query("SELECT * FROM study_notes ORDER BY date DESC")
    fun getAllNotes(): Flow<List<StudyNoteEntity>>

    @Query("SELECT * FROM study_notes WHERE topicId = :topicId ORDER BY date DESC")
    fun getNotesForTopic(topicId: String): Flow<List<StudyNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: StudyNoteEntity)

    @Update
    suspend fun updateNote(note: StudyNoteEntity)

    @Delete
    suspend fun deleteNote(note: StudyNoteEntity)

    @Query("DELETE FROM study_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    // --- Planned Sessions ---
    @Query("SELECT * FROM planned_sessions ORDER BY dayOfWeek ASC, time ASC")
    fun getAllPlannedSessions(): Flow<List<PlannedSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedSession(session: PlannedSessionEntity)

    @Delete
    suspend fun deletePlannedSession(session: PlannedSessionEntity)

    @Query("DELETE FROM planned_sessions WHERE id = :sessionId")
    suspend fun deletePlannedSessionById(sessionId: String)

    @Query("DELETE FROM planned_sessions")
    suspend fun clearPlannedSessions()

    // --- Direct Snapshot Reads for Backup ---
    @Query("SELECT * FROM subjects")
    suspend fun getSubjectsSnapshot(): List<SubjectEntity>

    @Query("SELECT * FROM topics")
    suspend fun getTopicsSnapshot(): List<TopicEntity>

    @Query("SELECT * FROM video_lessons")
    suspend fun getVideosSnapshot(): List<VideoLessonEntity>

    @Query("SELECT * FROM topic_revisions")
    suspend fun getRevisionsSnapshot(): List<TopicRevisionEntity>

    @Query("SELECT * FROM study_sessions")
    suspend fun getStudySessionsSnapshot(): List<StudySessionEntity>

    @Query("SELECT * FROM calendar_events")
    suspend fun getCalendarEventsSnapshot(): List<CalendarEventEntity>

    @Query("SELECT * FROM tasks")
    suspend fun getTasksSnapshot(): List<TaskEntity>

    @Query("SELECT * FROM study_notes")
    suspend fun getNotesSnapshot(): List<StudyNoteEntity>

    @Query("SELECT * FROM planned_sessions")
    suspend fun getPlannedSessionsSnapshot(): List<PlannedSessionEntity>

    // --- Clear all for restore / reset ---
    @Query("DELETE FROM subjects")
    suspend fun clearSubjects()

    @Query("DELETE FROM topics")
    suspend fun clearTopics()

    @Query("DELETE FROM video_lessons")
    suspend fun clearVideos()

    @Query("DELETE FROM topic_revisions")
    suspend fun clearRevisions()

    @Query("DELETE FROM study_sessions")
    suspend fun clearStudySessions()

    @Query("DELETE FROM calendar_events")
    suspend fun clearCalendarEvents()

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM study_notes")
    suspend fun clearNotes()
}
