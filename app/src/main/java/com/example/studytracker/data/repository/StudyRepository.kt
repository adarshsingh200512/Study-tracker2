package com.example.studytracker.data.repository

import com.example.studytracker.data.dao.StudyDao
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
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(private val dao: StudyDao) {

    // Profile
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    suspend fun saveProfile(profile: UserProfileEntity) = dao.insertOrUpdateProfile(profile)
    suspend fun getProfileOnce(): UserProfileEntity? = dao.getUserProfileOnce()

    // Subjects
    val allSubjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    suspend fun getSubjectById(id: String) = dao.getSubjectById(id)
    suspend fun insertSubject(subject: SubjectEntity) = dao.insertSubject(subject)
    suspend fun updateSubject(subject: SubjectEntity) = dao.updateSubject(subject)
    suspend fun deleteSubject(subject: SubjectEntity) = dao.deleteSubject(subject)
    suspend fun deleteSubjectById(id: String) = dao.deleteSubjectById(id)

    // Topics
    val allTopics: Flow<List<TopicEntity>> = dao.getAllTopics()
    fun getTopicsForSubject(subjectId: String): Flow<List<TopicEntity>> = dao.getTopicsForSubject(subjectId)
    suspend fun getTopicById(id: String) = dao.getTopicById(id)
    suspend fun insertTopic(topic: TopicEntity) = dao.insertTopic(topic)
    suspend fun updateTopic(topic: TopicEntity) = dao.updateTopic(topic)
    suspend fun deleteTopic(topic: TopicEntity) = dao.deleteTopic(topic)
    suspend fun deleteTopicById(id: String) = dao.deleteTopicById(id)

    // Video Lessons
    val allVideos: Flow<List<VideoLessonEntity>> = dao.getAllVideos()
    fun getVideosForTopic(topicId: String): Flow<List<VideoLessonEntity>> = dao.getVideosForTopic(topicId)
    suspend fun insertVideo(video: VideoLessonEntity) = dao.insertVideo(video)
    suspend fun updateVideo(video: VideoLessonEntity) = dao.updateVideo(video)
    suspend fun deleteVideo(video: VideoLessonEntity) = dao.deleteVideo(video)
    suspend fun deleteVideoById(id: String) = dao.deleteVideoById(id)

    // Topic Revisions
    val allRevisions: Flow<List<TopicRevisionEntity>> = dao.getAllRevisions()
    fun getRevisionsForTopic(topicId: String): Flow<List<TopicRevisionEntity>> = dao.getRevisionsForTopic(topicId)
    suspend fun insertRevision(revision: TopicRevisionEntity) = dao.insertRevision(revision)
    suspend fun updateRevision(revision: TopicRevisionEntity) = dao.updateRevision(revision)
    suspend fun deleteRevision(revision: TopicRevisionEntity) = dao.deleteRevision(revision)
    suspend fun deleteRevisionById(id: String) = dao.deleteRevisionById(id)

    // Study Sessions
    val allStudySessions: Flow<List<StudySessionEntity>> = dao.getAllStudySessions()
    fun getStudySessionsForDate(date: String): Flow<List<StudySessionEntity>> = dao.getStudySessionsForDate(date)
    suspend fun insertStudySession(session: StudySessionEntity) = dao.insertStudySession(session)
    suspend fun deleteStudySession(session: StudySessionEntity) = dao.deleteStudySession(session)

    // Calendar Events
    val allCalendarEvents: Flow<List<CalendarEventEntity>> = dao.getAllCalendarEvents()
    fun getCalendarEventsForDate(date: String): Flow<List<CalendarEventEntity>> = dao.getCalendarEventsForDate(date)
    suspend fun insertCalendarEvent(event: CalendarEventEntity) = dao.insertCalendarEvent(event)
    suspend fun updateCalendarEvent(event: CalendarEventEntity) = dao.updateCalendarEvent(event)
    suspend fun deleteCalendarEvent(event: CalendarEventEntity) = dao.deleteCalendarEvent(event)
    suspend fun deleteCalendarEventById(id: String) = dao.deleteCalendarEventById(id)

    // Tasks
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: String) = dao.deleteTaskById(id)

    // Notes
    val allNotes: Flow<List<StudyNoteEntity>> = dao.getAllNotes()
    fun getNotesForTopic(topicId: String): Flow<List<StudyNoteEntity>> = dao.getNotesForTopic(topicId)
    suspend fun insertNote(note: StudyNoteEntity) = dao.insertNote(note)
    suspend fun updateNote(note: StudyNoteEntity) = dao.updateNote(note)
    suspend fun deleteNote(note: StudyNoteEntity) = dao.deleteNote(note)
    suspend fun deleteNoteById(id: String) = dao.deleteNoteById(id)

    // Planned Sessions
    val allPlannedSessions: Flow<List<PlannedSessionEntity>> = dao.getAllPlannedSessions()
    suspend fun insertPlannedSession(session: PlannedSessionEntity) = dao.insertPlannedSession(session)
    suspend fun deletePlannedSession(session: PlannedSessionEntity) = dao.deletePlannedSession(session)
    suspend fun deletePlannedSessionById(id: String) = dao.deletePlannedSessionById(id)
    suspend fun clearPlannedSessions() = dao.clearPlannedSessions()

    // --- Structured JSON Backup & Restore System ---

    suspend fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("timestamp", System.currentTimeMillis())

        // Profile
        val profile = dao.getUserProfileOnce()
        if (profile != null) {
            val pObj = JSONObject()
            pObj.put("name", profile.name)
            pObj.put("dailyTargetMinutes", profile.dailyTargetMinutes)
            pObj.put("studyMode", profile.studyMode)
            pObj.put("priorityMode", profile.priorityMode)
            pObj.put("streak", profile.streak)
            pObj.put("lastStreakDate", profile.lastStreakDate)
            pObj.put("xp", profile.xp)
            pObj.put("level", profile.level)
            root.put("profile", pObj)
        }

        // Subjects
        val subjectsArray = JSONArray()
        dao.getSubjectsSnapshot().forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("targetHours", s.targetHours.toDouble())
            obj.put("colorHex", s.colorHex)
            obj.put("priority", s.priority)
            obj.put("examDate", s.examDate ?: "")
            obj.put("examName", s.examName ?: "")
            obj.put("notes", s.notes)
            obj.put("isEnabled", s.isEnabled)
            subjectsArray.put(obj)
        }
        root.put("subjects", subjectsArray)

        // Topics
        val topicsArray = JSONArray()
        dao.getTopicsSnapshot().forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("subjectId", t.subjectId)
            obj.put("name", t.name)
            obj.put("description", t.description)
            obj.put("estimatedHours", t.estimatedHours.toDouble())
            obj.put("completedHours", t.completedHours.toDouble())
            obj.put("priority", t.priority)
            obj.put("status", t.status)
            obj.put("targetDate", t.targetDate ?: "")
            obj.put("notes", t.notes)
            topicsArray.put(obj)
        }
        root.put("topics", topicsArray)

        // Videos
        val videosArray = JSONArray()
        dao.getVideosSnapshot().forEach { v ->
            val obj = JSONObject()
            obj.put("id", v.id)
            obj.put("topicId", v.topicId)
            obj.put("name", v.name)
            obj.put("isCompleted", v.isCompleted)
            obj.put("watchedDate", v.watchedDate ?: "")
            obj.put("watchedTime", v.watchedTime ?: "")
            obj.put("durationMinutes", v.durationMinutes)
            obj.put("notes", v.notes)
            videosArray.put(obj)
        }
        root.put("videos", videosArray)

        // Revisions
        val revisionsArray = JSONArray()
        dao.getRevisionsSnapshot().forEach { r ->
            val obj = JSONObject()
            obj.put("id", r.id)
            obj.put("topicId", r.topicId)
            obj.put("revisionNumber", r.revisionNumber)
            obj.put("date", r.date)
            obj.put("status", r.status)
            obj.put("notes", r.notes)
            revisionsArray.put(obj)
        }
        root.put("revisions", revisionsArray)

        // Study Sessions
        val sessionsArray = JSONArray()
        dao.getStudySessionsSnapshot().forEach { ss ->
            val obj = JSONObject()
            obj.put("id", ss.id)
            obj.put("date", ss.date)
            obj.put("startTime", ss.startTime)
            obj.put("endTime", ss.endTime)
            obj.put("durationMinutes", ss.durationMinutes)
            obj.put("subjectId", ss.subjectId)
            obj.put("topicId", ss.topicId)
            obj.put("videoId", ss.videoId ?: "")
            obj.put("studyMode", ss.studyMode)
            obj.put("focusRating", ss.focusRating)
            obj.put("notes", ss.notes)
            obj.put("timestamp", ss.timestamp)
            sessionsArray.put(obj)
        }
        root.put("studySessions", sessionsArray)

        // Tasks
        val tasksArray = JSONArray()
        dao.getTasksSnapshot().forEach { t ->
            val obj = JSONObject()
            obj.put("id", t.id)
            obj.put("title", t.title)
            obj.put("date", t.date)
            obj.put("time", t.time)
            obj.put("durationMinutes", t.durationMinutes)
            obj.put("priority", t.priority)
            obj.put("subjectId", t.subjectId ?: "")
            obj.put("topicId", t.topicId ?: "")
            obj.put("status", t.status)
            tasksArray.put(obj)
        }
        root.put("tasks", tasksArray)

        // Calendar Events
        val eventsArray = JSONArray()
        dao.getCalendarEventsSnapshot().forEach { e ->
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("title", e.title)
            obj.put("type", e.type)
            obj.put("date", e.date)
            obj.put("time", e.time)
            obj.put("subjectId", e.subjectId ?: "")
            obj.put("topicId", e.topicId ?: "")
            obj.put("notes", e.notes)
            eventsArray.put(obj)
        }
        root.put("calendarEvents", eventsArray)

        // Notes
        val notesArray = JSONArray()
        dao.getNotesSnapshot().forEach { n ->
            val obj = JSONObject()
            obj.put("id", n.id)
            obj.put("subjectId", n.subjectId ?: "")
            obj.put("topicId", n.topicId ?: "")
            obj.put("date", n.date)
            obj.put("title", n.title)
            obj.put("content", n.content)
            notesArray.put(obj)
        }
        root.put("notes", notesArray)

        return root.toString(2)
    }

    suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (!root.has("subjects")) return false

            // Restore profile if present
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                dao.insertOrUpdateProfile(
                    UserProfileEntity(
                        id = 1,
                        name = p.optString("name", "Student"),
                        dailyTargetMinutes = p.optInt("dailyTargetMinutes", 240),
                        studyMode = p.optString("studyMode", "Day Study"),
                        priorityMode = p.optString("priorityMode", "High Priority Tag First"),
                        streak = p.optInt("streak", 1),
                        lastStreakDate = p.optString("lastStreakDate", ""),
                        xp = p.optInt("xp", 100),
                        level = p.optInt("level", 1)
                    )
                )
            }

            // Clear old data
            dao.clearNotes()
            dao.clearTasks()
            dao.clearCalendarEvents()
            dao.clearStudySessions()
            dao.clearRevisions()
            dao.clearVideos()
            dao.clearTopics()
            dao.clearSubjects()

            // Restore subjects
            val subjectsArr = root.optJSONArray("subjects") ?: JSONArray()
            for (i in 0 until subjectsArr.length()) {
                val o = subjectsArr.getJSONObject(i)
                dao.insertSubject(
                    SubjectEntity(
                        id = o.optString("id"),
                        name = o.optString("name"),
                        targetHours = o.optDouble("targetHours", 40.0).toFloat(),
                        colorHex = o.optString("colorHex", "#0D9488"),
                        priority = o.optString("priority", "Medium"),
                        examDate = o.optString("examDate").takeIf { it.isNotBlank() },
                        examName = o.optString("examName").takeIf { it.isNotBlank() },
                        notes = o.optString("notes", ""),
                        isEnabled = o.optBoolean("isEnabled", true)
                    )
                )
            }

            // Restore topics
            val topicsArr = root.optJSONArray("topics") ?: JSONArray()
            for (i in 0 until topicsArr.length()) {
                val o = topicsArr.getJSONObject(i)
                dao.insertTopic(
                    TopicEntity(
                        id = o.optString("id"),
                        subjectId = o.optString("subjectId"),
                        name = o.optString("name"),
                        description = o.optString("description", ""),
                        estimatedHours = o.optDouble("estimatedHours", 5.0).toFloat(),
                        completedHours = o.optDouble("completedHours", 0.0).toFloat(),
                        priority = o.optString("priority", "Medium"),
                        status = o.optString("status", "Not Started"),
                        targetDate = o.optString("targetDate").takeIf { it.isNotBlank() },
                        notes = o.optString("notes", "")
                    )
                )
            }

            // Restore videos
            val videosArr = root.optJSONArray("videos") ?: JSONArray()
            for (i in 0 until videosArr.length()) {
                val o = videosArr.getJSONObject(i)
                dao.insertVideo(
                    VideoLessonEntity(
                        id = o.optString("id"),
                        topicId = o.optString("topicId"),
                        name = o.optString("name"),
                        isCompleted = o.optBoolean("isCompleted", false),
                        watchedDate = o.optString("watchedDate").takeIf { it.isNotBlank() },
                        watchedTime = o.optString("watchedTime").takeIf { it.isNotBlank() },
                        durationMinutes = o.optInt("durationMinutes", 30),
                        notes = o.optString("notes", "")
                    )
                )
            }

            // Restore revisions
            val revsArr = root.optJSONArray("revisions") ?: JSONArray()
            for (i in 0 until revsArr.length()) {
                val o = revsArr.getJSONObject(i)
                dao.insertRevision(
                    TopicRevisionEntity(
                        id = o.optString("id"),
                        topicId = o.optString("topicId"),
                        revisionNumber = o.optInt("revisionNumber", 1),
                        date = o.optString("date"),
                        status = o.optString("status", "Pending"),
                        notes = o.optString("notes", "")
                    )
                )
            }

            // Restore sessions
            val sessArr = root.optJSONArray("studySessions") ?: JSONArray()
            for (i in 0 until sessArr.length()) {
                val o = sessArr.getJSONObject(i)
                dao.insertStudySession(
                    StudySessionEntity(
                        id = o.optString("id"),
                        date = o.optString("date"),
                        startTime = o.optString("startTime", ""),
                        endTime = o.optString("endTime", ""),
                        durationMinutes = o.optInt("durationMinutes", 25),
                        subjectId = o.optString("subjectId", ""),
                        topicId = o.optString("topicId", ""),
                        videoId = o.optString("videoId").takeIf { it.isNotBlank() },
                        studyMode = o.optString("studyMode", "Deep Study"),
                        focusRating = o.optInt("focusRating", 4),
                        notes = o.optString("notes", ""),
                        timestamp = o.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }

            // Restore tasks
            val tasksArr = root.optJSONArray("tasks") ?: JSONArray()
            for (i in 0 until tasksArr.length()) {
                val o = tasksArr.getJSONObject(i)
                dao.insertTask(
                    TaskEntity(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        date = o.optString("date"),
                        time = o.optString("time", ""),
                        durationMinutes = o.optInt("durationMinutes", 30),
                        priority = o.optString("priority", "Medium"),
                        subjectId = o.optString("subjectId").takeIf { it.isNotBlank() },
                        topicId = o.optString("topicId").takeIf { it.isNotBlank() },
                        status = o.optString("status", "To Do")
                    )
                )
            }

            // Restore events
            val eventsArr = root.optJSONArray("calendarEvents") ?: JSONArray()
            for (i in 0 until eventsArr.length()) {
                val o = eventsArr.getJSONObject(i)
                dao.insertCalendarEvent(
                    CalendarEventEntity(
                        id = o.optString("id"),
                        title = o.optString("title"),
                        type = o.optString("type", "Test"),
                        date = o.optString("date"),
                        time = o.optString("time", "10:00"),
                        subjectId = o.optString("subjectId").takeIf { it.isNotBlank() },
                        topicId = o.optString("topicId").takeIf { it.isNotBlank() },
                        notes = o.optString("notes", "")
                    )
                )
            }

            // Restore notes
            val notesArr = root.optJSONArray("notes") ?: JSONArray()
            for (i in 0 until notesArr.length()) {
                val o = notesArr.getJSONObject(i)
                dao.insertNote(
                    StudyNoteEntity(
                        id = o.optString("id"),
                        subjectId = o.optString("subjectId").takeIf { it.isNotBlank() },
                        topicId = o.optString("topicId").takeIf { it.isNotBlank() },
                        date = o.optString("date"),
                        title = o.optString("title"),
                        content = o.optString("content")
                    )
                )
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
