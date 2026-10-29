package com.example.studytracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        UserProfileEntity::class,
        SubjectEntity::class,
        TopicEntity::class,
        VideoLessonEntity::class,
        TopicRevisionEntity::class,
        StudySessionEntity::class,
        CalendarEventEntity::class,
        TaskEntity::class,
        StudyNoteEntity::class,
        PlannedSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class StudyTrackerDatabase : RoomDatabase() {

    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: StudyTrackerDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): StudyTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyTrackerDatabase::class.java,
                    "study_tracker_db"
                )
                    .addCallback(StudyDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class StudyDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.studyDao())
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database.studyDao())
                }
            }
        }

        private suspend fun populateInitialData(dao: StudyDao) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val today = sdf.format(cal.time)

            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(cal.time)

            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, -2)
            val twoDaysAgo = sdf.format(cal.time)

            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, 18)
            val mathExamDate = sdf.format(cal.time)

            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, 24)
            val physicsExamDate = sdf.format(cal.time)

            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, 3)
            val revDate1 = sdf.format(cal.time)

            cal.time = Calendar.getInstance().time
            cal.add(Calendar.DAY_OF_YEAR, 7)
            val revDate2 = sdf.format(cal.time)

            // 1. Profile
            dao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Adarsh Singh",
                    dailyTargetMinutes = 240,
                    studyMode = "Day Study",
                    priorityMode = "High Priority Tag First",
                    streak = 12,
                    lastStreakDate = yesterday,
                    xp = 1250,
                    level = 12
                )
            )

            // 2. Initial Subjects
            val math = SubjectEntity(
                id = "sub_math",
                name = "Mathematics",
                targetHours = 60f,
                colorHex = "#0D9488", // Teal
                priority = "High",
                examDate = mathExamDate,
                examName = "Final Board Calculus & Algebra",
                notes = "Focus on calculus derivations and time-saving algebra shortcuts.",
                orderIndex = 0
            )
            val physics = SubjectEntity(
                id = "sub_physics",
                name = "Physics",
                targetHours = 50f,
                colorHex = "#3B82F6", // Blue
                priority = "High",
                examDate = physicsExamDate,
                examName = "Midterm Mechanics & Vectors",
                notes = "Pay attention to rotational dynamics and center of mass.",
                orderIndex = 1
            )
            val chemistry = SubjectEntity(
                id = "sub_chemistry",
                name = "Chemistry",
                targetHours = 40f,
                colorHex = "#8B5CF6", // Purple
                priority = "Medium",
                examDate = null,
                examName = null,
                notes = "Memorize organic reaction mechanisms and reagents.",
                orderIndex = 2
            )

            dao.insertSubject(math)
            dao.insertSubject(physics)
            dao.insertSubject(chemistry)

            // 3. Initial Topics
            val topTimeWork = TopicEntity(
                id = "top_math_1",
                subjectId = "sub_math",
                name = "Time and Work",
                description = "Pipes, cisterns, man-day efficiency rules, and alternate day questions.",
                estimatedHours = 8f,
                completedHours = 4.8f,
                priority = "High",
                status = "In Progress",
                targetDate = revDate1,
                notes = "Rule: Work = Rate × Time. Always determine total units using LCM.",
                orderIndex = 0
            )
            val topPercentage = TopicEntity(
                id = "top_math_2",
                subjectId = "sub_math",
                name = "Percentage & Profit/Loss",
                description = "Successive percentage changes, markup, discount calculations.",
                estimatedHours = 6f,
                completedHours = 6f,
                priority = "Medium",
                status = "Revision Pending",
                targetDate = today,
                notes = "Multipliers: 1/7 = 14.28%, 1/8 = 12.5%.",
                orderIndex = 1
            )
            val topRotational = TopicEntity(
                id = "top_phy_1",
                subjectId = "sub_physics",
                name = "Rotational Dynamics",
                description = "Moment of inertia, torque, parallel axes theorem, angular momentum.",
                estimatedHours = 10f,
                completedHours = 3.5f,
                priority = "High",
                status = "In Progress",
                targetDate = revDate2,
                notes = "Torque = I * alpha. Conservation of angular momentum holds when external torque is zero.",
                orderIndex = 0
            )

            dao.insertTopic(topTimeWork)
            dao.insertTopic(topPercentage)
            dao.insertTopic(topRotational)

            // 4. Unlimited Video Lessons for Time and Work
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_1",
                    topicId = "top_math_1",
                    name = "Video 1 — Basic Concept & Efficiency",
                    isCompleted = true,
                    watchedDate = twoDaysAgo,
                    watchedTime = "10:30 AM",
                    durationMinutes = 35,
                    notes = "LCM method for unit capacity explained very well.",
                    orderIndex = 0
                )
            )
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_2",
                    topicId = "top_math_1",
                    name = "Video 2 — Pipes & Cisterns Inflow/Outflow",
                    isCompleted = true,
                    watchedDate = yesterday,
                    watchedTime = "02:15 PM",
                    durationMinutes = 40,
                    notes = "Negative work convention for drain pipes.",
                    orderIndex = 1
                )
            )
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_3",
                    topicId = "top_math_1",
                    name = "Video 3 — Alternate Day Work Cycles",
                    isCompleted = false,
                    watchedDate = null,
                    watchedTime = null,
                    durationMinutes = 45,
                    notes = "",
                    orderIndex = 2
                )
            )
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_4",
                    topicId = "top_math_1",
                    name = "Video 4 — Advanced Exam Question Walkthrough",
                    isCompleted = false,
                    watchedDate = null,
                    watchedTime = null,
                    durationMinutes = 50,
                    notes = "",
                    orderIndex = 3
                )
            )
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_5",
                    topicId = "top_math_1",
                    name = "Video 5 — Practice Problems & Speed Drill",
                    isCompleted = false,
                    watchedDate = null,
                    watchedTime = null,
                    durationMinutes = 30,
                    notes = "",
                    orderIndex = 4
                )
            )

            // Video for percentages
            dao.insertVideo(
                VideoLessonEntity(
                    id = "vid_6",
                    topicId = "top_math_2",
                    name = "Video 1 — Fractions to Percentage Masterclass",
                    isCompleted = true,
                    watchedDate = threeDaysAgo(sdf),
                    watchedTime = "04:00 PM",
                    durationMinutes = 45,
                    notes = "Memorize 1/1 to 1/20 equivalents.",
                    orderIndex = 0
                )
            )

            // 5. Spaced Revisions
            dao.insertRevision(
                TopicRevisionEntity(
                    id = "rev_1",
                    topicId = "top_math_1",
                    revisionNumber = 1,
                    date = revDate1,
                    status = "Pending",
                    notes = "Solve 10 previous exam questions without notes."
                )
            )
            dao.insertRevision(
                TopicRevisionEntity(
                    id = "rev_2",
                    topicId = "top_math_2",
                    revisionNumber = 1,
                    date = today,
                    status = "Pending",
                    notes = "Rapid 15-minute flashcard recall."
                )
            )

            // 6. Recent Study Sessions
            dao.insertStudySession(
                StudySessionEntity(
                    id = "ses_1",
                    date = today,
                    startTime = "09:00 AM",
                    endTime = "10:00 AM",
                    durationMinutes = 60,
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    videoId = "vid_2",
                    studyMode = "Intense Focus",
                    focusRating = 5,
                    notes = "Solved 15 questions on efficiency.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3
                )
            )
            dao.insertStudySession(
                StudySessionEntity(
                    id = "ses_2",
                    date = yesterday,
                    startTime = "11:00 AM",
                    endTime = "11:45 AM",
                    durationMinutes = 45,
                    subjectId = "sub_physics",
                    topicId = "top_phy_1",
                    videoId = null,
                    studyMode = "Concept Derivation",
                    focusRating = 4,
                    notes = "Derived moment of inertia of solid cylinder and sphere.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 27
                )
            )
            dao.insertStudySession(
                StudySessionEntity(
                    id = "ses_3",
                    date = twoDaysAgo,
                    startTime = "02:00 PM",
                    endTime = "03:15 PM",
                    durationMinutes = 75,
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    videoId = "vid_1",
                    studyMode = "Video Lecture",
                    focusRating = 5,
                    notes = "Detailed notes on unit work formula.",
                    timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 50
                )
            )

            // Seed historical study sessions across past 4 weeks for realistic heatmap activity
            val pastDaysOffsets = listOf(
                Pair(3, 120),  // 3 days ago: 2h (sub_physics)
                Pair(4, 210),  // 4 days ago: 3.5h (sub_math)
                Pair(5, 90),   // 5 days ago: 1.5h (sub_chemistry)
                Pair(6, 180),  // 6 days ago: 3h (sub_physics)
                Pair(7, 330),  // 7 days ago: 5.5h peak (sub_math)
                Pair(8, 150),  // 8 days ago: 2.5h (sub_physics)
                Pair(9, 60),   // 9 days ago: 1h (sub_chemistry)
                Pair(11, 240), // 11 days ago: 4h (sub_math)
                Pair(12, 180), // 12 days ago: 3h (sub_physics)
                Pair(13, 300), // 13 days ago: 5h peak (sub_math)
                Pair(14, 120), // 14 days ago: 2h (sub_chemistry)
                Pair(16, 90),  // 16 days ago: 1.5h (sub_physics)
                Pair(17, 210), // 17 days ago: 3.5h (sub_math)
                Pair(18, 270), // 18 days ago: 4.5h (sub_math)
                Pair(20, 180), // 20 days ago: 3h (sub_physics)
                Pair(21, 360), // 21 days ago: 6h marathon (sub_math)
                Pair(23, 120), // 23 days ago: 2h (sub_chemistry)
                Pair(25, 240), // 25 days ago: 4h (sub_physics)
                Pair(27, 150)  // 27 days ago: 2.5h (sub_math)
            )

            for ((dayOffset, duration) in pastDaysOffsets) {
                val histCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -dayOffset) }
                val histDateStr = sdf.format(histCal.time)
                val subjId = if (dayOffset % 2 == 0) "sub_math" else if (dayOffset % 3 == 0) "sub_chemistry" else "sub_physics"
                dao.insertStudySession(
                    StudySessionEntity(
                        id = "hist_ses_$dayOffset",
                        date = histDateStr,
                        startTime = "02:00 PM",
                        endTime = "05:00 PM",
                        durationMinutes = duration,
                        subjectId = subjId,
                        topicId = "top_math_1",
                        videoId = null,
                        studyMode = if (duration >= 300) "Deep Mastery" else "Concept Revision",
                        focusRating = if (duration >= 300) 5 else 4,
                        notes = "Completed scheduled topic exercises and practice tests.",
                        timestamp = histCal.timeInMillis
                    )
                )
            }


            // 7. Calendar Events
            dao.insertCalendarEvent(
                CalendarEventEntity(
                    id = "evt_1",
                    title = "Mathematics Weekly Mock Quiz",
                    type = "Quiz",
                    date = today,
                    time = "04:00 PM",
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    notes = "Timed 20 question challenge."
                )
            )
            dao.insertCalendarEvent(
                CalendarEventEntity(
                    id = "evt_2",
                    title = "Physics Lab Assignment Due",
                    type = "Assignment",
                    date = revDate1,
                    time = "11:59 PM",
                    subjectId = "sub_physics",
                    topicId = "top_phy_1",
                    notes = "Submit lab graph PDF."
                )
            )

            // 8. Tasks
            dao.insertTask(
                TaskEntity(
                    id = "tsk_1",
                    title = "Solve 20 problems on Pipes & Cisterns",
                    date = today,
                    time = "03:00 PM",
                    durationMinutes = 45,
                    priority = "High",
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    status = "In Progress"
                )
            )
            dao.insertTask(
                TaskEntity(
                    id = "tsk_2",
                    title = "Write summary formula sheet for Rotational Dynamics",
                    date = today,
                    time = "06:00 PM",
                    durationMinutes = 30,
                    priority = "Medium",
                    subjectId = "sub_physics",
                    topicId = "top_phy_1",
                    status = "To Do"
                )
            )

            // 9. Study Notes
            dao.insertNote(
                StudyNoteEntity(
                    id = "not_1",
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    date = today,
                    title = "Efficiency Shortcut Formula",
                    content = "When A takes x days and B takes y days, their combined rate is (x*y)/(x+y). For 3 people, use 1/A + 1/B + 1/C."
                )
            )
            dao.insertNote(
                StudyNoteEntity(
                    id = "not_2",
                    subjectId = "sub_physics",
                    topicId = "top_phy_1",
                    date = yesterday,
                    title = "Parallel Axes Theorem Note",
                    content = "I = I_cm + M*d^2. Must ensure I_cm is strictly through the center of mass, not any arbitrary parallel axis."
                )
            )

            // 10. Planner sessions
            dao.insertPlannedSession(
                PlannedSessionEntity(
                    id = "plan_1",
                    dayOfWeek = "Monday",
                    subjectId = "sub_math",
                    topicId = "top_math_1",
                    durationMinutes = 60,
                    time = "09:00 AM"
                )
            )
            dao.insertPlannedSession(
                PlannedSessionEntity(
                    id = "plan_2",
                    dayOfWeek = "Tuesday",
                    subjectId = "sub_physics",
                    topicId = "top_phy_1",
                    durationMinutes = 60,
                    time = "10:30 AM"
                )
            )
        }

        private fun threeDaysAgo(sdf: SimpleDateFormat): String {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -3)
            return sdf.format(c.time)
        }
    }
}
