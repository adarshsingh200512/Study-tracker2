package com.example.studytracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.studytracker.data.db.StudyTrackerDatabase
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
import com.example.studytracker.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

enum class NavigationTab {
    HOME, CALENDAR, FOCUS, NOTES, SYLLABUS, ANALYTICS, TASKS, TIMER
}

enum class TimerStatus {
    IDLE, RUNNING, PAUSED
}

enum class TimerMode {
    COUNTDOWN, STOPWATCH
}

enum class ActiveModal {
    NONE,
    PLANNER,
    DAILY_MISSION,
    EXAM_COUNTDOWN,
    ALL_NOTES,
    GAMIFICATION,
    SETTINGS,
    ADD_SUBJECT,
    EDIT_SUBJECT,
    ADD_TOPIC,
    EDIT_TOPIC,
    ADD_VIDEO,
    ADD_REVISION,
    ADD_EVENT,
    ADD_TASK,
    ADD_NOTE,
    FOCUS_RATING
}

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StudyRepository

    init {
        val db = StudyTrackerDatabase.getDatabase(application, viewModelScope)
        repository = StudyRepository(db.studyDao())
    }

    val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

    // Reactive State from Repository
    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<SubjectEntity>> = repository.allSubjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topics: StateFlow<List<TopicEntity>> = repository.allTopics
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoLessonEntity>> = repository.allVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val revisions: StateFlow<List<TopicRevisionEntity>> = repository.allRevisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarEvents: StateFlow<List<CalendarEventEntity>> = repository.allCalendarEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<StudyNoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val plannedSessions: StateFlow<List<PlannedSessionEntity>> = repository.allPlannedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Navigation State
    private val _currentTab = MutableStateFlow(NavigationTab.HOME)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _activeModal = MutableStateFlow(ActiveModal.NONE)
    val activeModal: StateFlow<ActiveModal> = _activeModal.asStateFlow()

    private val _selectedTopicForDetail = MutableStateFlow<TopicEntity?>(null)
    val selectedTopicForDetail: StateFlow<TopicEntity?> = _selectedTopicForDetail.asStateFlow()

    // Editing buffers
    val editingSubject = MutableStateFlow<SubjectEntity?>(null)
    val editingTopic = MutableStateFlow<TopicEntity?>(null)
    val targetSubjectIdForNewTopic = MutableStateFlow<String?>(null)

    // Toast message state
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // ----------------------------------------------------
    // TIMER ENGINE
    // ----------------------------------------------------
    private val _timerStatus = MutableStateFlow(TimerStatus.IDLE)
    val timerStatus: StateFlow<TimerStatus> = _timerStatus.asStateFlow()

    private val _timerMode = MutableStateFlow(TimerMode.COUNTDOWN)
    val timerMode: StateFlow<TimerMode> = _timerMode.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(25 * 60)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    val timerSubjectId = MutableStateFlow("")
    val timerTopicId = MutableStateFlow("")
    val timerVideoId = MutableStateFlow<String?>(null)
    val timerStudyMode = MutableStateFlow("Deep Study")

    private var timerJob: Job? = null

    fun selectTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun openModal(modal: ActiveModal) {
        _activeModal.value = modal
    }

    fun closeModal() {
        _activeModal.value = ActiveModal.NONE
    }

    fun openTopicDetail(topic: TopicEntity) {
        _selectedTopicForDetail.value = topic
    }

    fun closeTopicDetail() {
        _selectedTopicForDetail.value = null
    }

    fun startTimer() {
        if (_timerStatus.value == TimerStatus.RUNNING) return

        // Default to first subject if not set
        if (timerSubjectId.value.isEmpty() && subjects.value.isNotEmpty()) {
            timerSubjectId.value = subjects.value.first().id
        }

        _timerStatus.value = TimerStatus.RUNNING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerStatus.value == TimerStatus.RUNNING) {
                delay(1000)
                if (_timerMode.value == TimerMode.COUNTDOWN) {
                    if (_remainingSeconds.value > 0) {
                        _remainingSeconds.value -= 1
                        _elapsedSeconds.value += 1
                    } else {
                        // Countdown reached 0
                        finishTimerSession()
                        break
                    }
                } else {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun pauseTimer() {
        _timerStatus.value = TimerStatus.PAUSED
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerStatus.value = TimerStatus.IDLE
        _remainingSeconds.value = _totalDurationSeconds.value
        _elapsedSeconds.value = 0
    }

    fun setTimerPreset(minutes: Int) {
        pauseTimer()
        _timerMode.value = TimerMode.COUNTDOWN
        _totalDurationSeconds.value = minutes * 60
        _remainingSeconds.value = minutes * 60
        _elapsedSeconds.value = 0
        _timerStatus.value = TimerStatus.IDLE
    }

    fun setCustomTimerDuration(minutes: Int) {
        if (minutes > 0) {
            setTimerPreset(minutes)
        }
    }

    fun startTimerForTopic(subjectId: String, topicId: String) {
        timerSubjectId.value = subjectId
        timerTopicId.value = topicId
        closeTopicDetail()
        selectTab(NavigationTab.TIMER)
        startTimer()
    }

    // Pending session attributes for Focus Rating dialog
    val pendingSessionMinutes = MutableStateFlow(25)

    fun finishTimerSession() {
        pauseTimer()
        val minutes = maxOf(1, _elapsedSeconds.value / 60)
        pendingSessionMinutes.value = minutes
        resetTimer()
        openModal(ActiveModal.FOCUS_RATING)
    }

    fun saveCompletedSession(focusRating: Int, notes: String) {
        val duration = pendingSessionMinutes.value
        val today = todayDateString
        val subId = timerSubjectId.value.ifEmpty { subjects.value.firstOrNull()?.id ?: "" }
        val topId = timerTopicId.value
        val vidId = timerVideoId.value
        val mode = timerStudyMode.value

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val nowStr = timeFormat.format(Calendar.getInstance().time)

        viewModelScope.launch {
            val session = StudySessionEntity(
                id = UUID.randomUUID().toString(),
                date = today,
                startTime = nowStr,
                endTime = nowStr,
                durationMinutes = duration,
                subjectId = subId,
                topicId = topId,
                videoId = vidId,
                studyMode = mode,
                focusRating = focusRating,
                notes = notes,
                timestamp = System.currentTimeMillis()
            )
            repository.insertStudySession(session)

            // Update topic completed hours
            if (topId.isNotEmpty()) {
                val topic = topics.value.find { it.id == topId }
                if (topic != null) {
                    val updated = topic.copy(
                        completedHours = topic.completedHours + (duration / 60f),
                        status = if (topic.status == "Not Started") "In Progress" else topic.status
                    )
                    repository.updateTopic(updated)
                    // If we currently have topic detail open for this topic, refresh it
                    if (_selectedTopicForDetail.value?.id == topId) {
                        _selectedTopicForDetail.value = updated
                    }
                }
            }

            // Award XP & update Streak
            val currentProfile = userProfile.value ?: UserProfileEntity()
            val earnedXp = (duration * 1.5f).toInt() + (focusRating * 5)
            val newXp = currentProfile.xp + earnedXp
            val newLevel = (newXp / 500) + 1

            // Streak check
            var newStreak = currentProfile.streak
            if (currentProfile.lastStreakDate != today) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
                if (currentProfile.lastStreakDate == yesterday) {
                    newStreak += 1
                } else if (currentProfile.lastStreakDate.isEmpty()) {
                    newStreak = 1
                }
            }

            repository.saveProfile(
                currentProfile.copy(
                    xp = newXp,
                    level = newLevel,
                    streak = newStreak,
                    lastStreakDate = today
                )
            )

            closeModal()
            showToast("+$earnedXp XP! Session saved successfully.")
        }
    }

    // ----------------------------------------------------
    // FOCUS MODE & SELECTIVE APP BLOCKER STATE
    // ----------------------------------------------------
    val defaultDistractionApps = listOf(
        "Instagram", "YouTube", "WhatsApp", "Games (PUBG/BGMI)",
        "TikTok / Reels", "Snapchat", "Netflix", "Twitter / X", "Reddit", "Chrome Browser"
    )

    private val _blockedApps = MutableStateFlow(
        setOf("Instagram", "YouTube", "WhatsApp", "Games (PUBG/BGMI)", "TikTok / Reels")
    )
    val blockedApps: StateFlow<Set<String>> = _blockedApps.asStateFlow()

    private val _isStrictShield = MutableStateFlow(true)
    val isStrictShield: StateFlow<Boolean> = _isStrictShield.asStateFlow()

    private val _activeAmbientSound = MutableStateFlow("Rain")
    val activeAmbientSound: StateFlow<String> = _activeAmbientSound.asStateFlow()

    private val _shieldBlockedAppAttempt = MutableStateFlow<String?>(null)
    val shieldBlockedAppAttempt: StateFlow<String?> = _shieldBlockedAppAttempt.asStateFlow()

    fun toggleBlockedApp(appName: String) {
        val current = _blockedApps.value.toMutableSet()
        if (current.contains(appName)) {
            current.remove(appName)
            showToast("Unblocked $appName")
        } else {
            current.add(appName)
            showToast("Blocked $appName during study")
        }
        _blockedApps.value = current
    }

    fun setAllAppsBlocked(blockAll: Boolean) {
        _blockedApps.value = if (blockAll) defaultDistractionApps.toSet() else emptySet()
        showToast(if (blockAll) "All 10 distraction apps blocked!" else "All app blockers cleared.")
    }

    fun toggleStrictShield() {
        _isStrictShield.value = !_isStrictShield.value
        showToast(if (_isStrictShield.value) "Strict Study Shield: ON" else "Strict Study Shield: OFF")
    }

    fun setAmbientSound(sound: String) {
        _activeAmbientSound.value = sound
    }

    fun triggerAppBlockShieldAlert(appName: String) {
        _shieldBlockedAppAttempt.value = appName
    }

    fun dismissAppBlockShieldAlert() {
        _shieldBlockedAppAttempt.value = null
    }

    // ----------------------------------------------------
    // FULL-SCREEN FOCUS OVERLAY & STUDY SHIELD
    // ----------------------------------------------------
    private val _isFocusOverlayActive = MutableStateFlow(false)
    val isFocusOverlayActive: StateFlow<Boolean> = _isFocusOverlayActive.asStateFlow()

    fun toggleFocusMode(enable: Boolean? = null) {
        val newState = enable ?: !_isFocusOverlayActive.value
        _isFocusOverlayActive.value = newState
        if (newState) {
            if (_timerStatus.value != TimerStatus.RUNNING) {
                startTimer()
            }
            showToast("Focus Mode Active 🛡️")
        } else {
            showToast("Focus Mode Stopped")
        }
    }

    fun stopFocusModeFromOverlay() {
        _isFocusOverlayActive.value = false
        if (_timerStatus.value == TimerStatus.RUNNING) {
            pauseTimer()
        }
        showToast("Focus Mode Stopped")
    }

    // ----------------------------------------------------
    // NOTES SEARCH & FILTER STATE
    // ----------------------------------------------------
    private val _notesSearchQuery = MutableStateFlow("")
    val notesSearchQuery: StateFlow<String> = _notesSearchQuery.asStateFlow()

    private val _selectedNoteSubjectId = MutableStateFlow<String?>(null)
    val selectedNoteSubjectId: StateFlow<String?> = _selectedNoteSubjectId.asStateFlow()

    fun setNotesSearchQuery(query: String) {
        _notesSearchQuery.value = query
    }

    fun setSelectedNoteSubjectFilter(subjectId: String?) {
        _selectedNoteSubjectId.value = subjectId
    }

    fun toggleNotePin(note: StudyNoteEntity) {
        viewModelScope.launch {
            val updated = note.copy(isPinned = !note.isPinned)
            repository.updateNote(updated)
            showToast(if (updated.isPinned) "Note pinned to top 📌" else "Note unpinned")
        }
    }

    // ----------------------------------------------------
    // CALENDAR INTEGRATED 4-IN-1 STATE
    // ----------------------------------------------------
    private val _calendarSelectedDate = MutableStateFlow(todayDateString)
    val calendarSelectedDate: StateFlow<String> = _calendarSelectedDate.asStateFlow()

    private val _calendarYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val calendarYear: StateFlow<Int> = _calendarYear.asStateFlow()

    private val _calendarMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH)) // 0..11
    val calendarMonth: StateFlow<Int> = _calendarMonth.asStateFlow()

    fun selectCalendarDate(date: String) {
        _calendarSelectedDate.value = date
    }

    fun changeCalendarMonth(delta: Int) {
        var m = _calendarMonth.value + delta
        var y = _calendarYear.value
        if (m < 0) {
            m = 11
            y -= 1
        } else if (m > 11) {
            m = 0
            y += 1
        }
        _calendarMonth.value = m
        _calendarYear.value = y
    }

    fun goToCalendarToday() {
        val cal = Calendar.getInstance()
        _calendarYear.value = cal.get(Calendar.YEAR)
        _calendarMonth.value = cal.get(Calendar.MONTH)
        _calendarSelectedDate.value = todayDateString
    }

    // ----------------------------------------------------
    // SUBJECTS CRUD
    // ----------------------------------------------------
    fun addSubject(name: String, targetHours: Float, colorHex: String, priority: String, examDate: String?, examName: String?, notes: String) {
        viewModelScope.launch {
            val sub = SubjectEntity(
                name = name,
                targetHours = targetHours,
                colorHex = colorHex,
                priority = priority,
                examDate = examDate?.takeIf { it.isNotBlank() },
                examName = examName?.takeIf { it.isNotBlank() },
                notes = notes,
                orderIndex = subjects.value.size
            )
            repository.insertSubject(sub)
            awardXp(25, "Subject Created")
            closeModal()
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
            closeModal()
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
            closeModal()
        }
    }

    // ----------------------------------------------------
    // TOPICS CRUD
    // ----------------------------------------------------
    fun addTopic(subjectId: String, name: String, description: String, estimatedHours: Float, priority: String, status: String, targetDate: String?, notes: String) {
        viewModelScope.launch {
            val top = TopicEntity(
                subjectId = subjectId,
                name = name,
                description = description,
                estimatedHours = estimatedHours,
                completedHours = 0f,
                priority = priority,
                status = status,
                targetDate = targetDate?.takeIf { it.isNotBlank() },
                notes = notes,
                orderIndex = topics.value.count { it.subjectId == subjectId }
            )
            repository.insertTopic(top)
            awardXp(20, "Topic Created")
            closeModal()
        }
    }

    fun updateTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.updateTopic(topic)
            if (_selectedTopicForDetail.value?.id == topic.id) {
                _selectedTopicForDetail.value = topic
            }
            closeModal()
        }
    }

    fun deleteTopic(topic: TopicEntity) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
            if (_selectedTopicForDetail.value?.id == topic.id) {
                _selectedTopicForDetail.value = null
            }
            closeModal()
        }
    }

    // ----------------------------------------------------
    // UNLIMITED VIDEO LESSONS CRUD (CRITICAL FEATURE)
    // ----------------------------------------------------
    fun addVideoLesson(topicId: String, name: String, durationMinutes: Int, isCompleted: Boolean, notes: String) {
        viewModelScope.launch {
            val vid = VideoLessonEntity(
                topicId = topicId,
                name = name,
                isCompleted = isCompleted,
                watchedDate = if (isCompleted) todayDateString else null,
                watchedTime = if (isCompleted) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time) else null,
                durationMinutes = durationMinutes,
                notes = notes,
                orderIndex = videos.value.count { it.topicId == topicId }
            )
            repository.insertVideo(vid)
            if (isCompleted) {
                awardXp(15, "Video Completed")
                // Add duration to topic completed hours
                val topic = topics.value.find { it.id == topicId }
                if (topic != null) {
                    val updated = topic.copy(completedHours = topic.completedHours + (durationMinutes / 60f))
                    repository.updateTopic(updated)
                    _selectedTopicForDetail.value = updated
                }
            }
            closeModal()
        }
    }

    fun toggleVideoCompletion(video: VideoLessonEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            val updated = video.copy(
                isCompleted = isCompleted,
                watchedDate = if (isCompleted) todayDateString else null,
                watchedTime = if (isCompleted) SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Calendar.getInstance().time) else null
            )
            repository.updateVideo(updated)

            val topic = topics.value.find { it.id == video.topicId }
            if (topic != null) {
                val hourDelta = (video.durationMinutes / 60f) * (if (isCompleted) 1 else -1)
                val newCompleted = maxOf(0f, topic.completedHours + hourDelta)
                val updatedTopic = topic.copy(completedHours = newCompleted)
                repository.updateTopic(updatedTopic)
                if (_selectedTopicForDetail.value?.id == topic.id) {
                    _selectedTopicForDetail.value = updatedTopic
                }
            }

            if (isCompleted) {
                awardXp(15, "Video Completed")
            }
        }
    }

    fun deleteVideo(video: VideoLessonEntity) {
        viewModelScope.launch {
            repository.deleteVideo(video)
        }
    }

    // ----------------------------------------------------
    // REVISIONS CRUD (Spaced Repetition)
    // ----------------------------------------------------
    fun addRevision(topicId: String, date: String, notes: String) {
        viewModelScope.launch {
            val count = revisions.value.count { it.topicId == topicId }
            val rev = TopicRevisionEntity(
                topicId = topicId,
                revisionNumber = count + 1,
                date = date,
                status = "Pending",
                notes = notes
            )
            repository.insertRevision(rev)
            closeModal()
        }
    }

    fun toggleRevisionStatus(revision: TopicRevisionEntity) {
        viewModelScope.launch {
            val newStatus = if (revision.status == "Completed") "Pending" else "Completed"
            val updated = revision.copy(status = newStatus)
            repository.updateRevision(updated)
            if (newStatus == "Completed") {
                awardXp(30, "Revision Mastered")
            }
        }
    }

    fun deleteRevision(revision: TopicRevisionEntity) {
        viewModelScope.launch {
            repository.deleteRevision(revision)
        }
    }

    // ----------------------------------------------------
    // TASKS CRUD
    // ----------------------------------------------------
    fun addTask(title: String, date: String, time: String, duration: Int, priority: String, subjectId: String?, topicId: String?) {
        viewModelScope.launch {
            val tsk = TaskEntity(
                title = title,
                date = date.ifBlank { todayDateString },
                time = time,
                durationMinutes = duration,
                priority = priority,
                subjectId = subjectId?.takeIf { it.isNotBlank() },
                topicId = topicId?.takeIf { it.isNotBlank() },
                status = "To Do"
            )
            repository.insertTask(tsk)
            closeModal()
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "Completed") "To Do" else "Completed"
            repository.updateTask(task.copy(status = newStatus))
            if (newStatus == "Completed") {
                awardXp(10, "Task Complete")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // ----------------------------------------------------
    // CALENDAR EVENTS CRUD
    // ----------------------------------------------------
    fun addCalendarEvent(title: String, type: String, date: String, time: String, subjectId: String?, notes: String) {
        viewModelScope.launch {
            val evt = CalendarEventEntity(
                title = title,
                type = type,
                date = date,
                time = time,
                subjectId = subjectId?.takeIf { it.isNotBlank() },
                notes = notes
            )
            repository.insertCalendarEvent(evt)
            closeModal()
        }
    }

    fun deleteCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            repository.deleteCalendarEvent(event)
        }
    }

    // ----------------------------------------------------
    // NOTES CRUD
    // ----------------------------------------------------
    fun addNote(title: String, content: String, subjectId: String?, topicId: String?) {
        viewModelScope.launch {
            val note = StudyNoteEntity(
                title = title,
                content = content,
                date = todayDateString,
                subjectId = subjectId?.takeIf { it.isNotBlank() },
                topicId = topicId?.takeIf { it.isNotBlank() }
            )
            repository.insertNote(note)
            awardXp(10, "Note Written")
            closeModal()
        }
    }

    fun deleteNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    // ----------------------------------------------------
    // SMART STUDY PLANNER ALGORITHM
    // ----------------------------------------------------
    fun generateSmartWeeklyPlan(strategy: String) {
        viewModelScope.launch {
            repository.clearPlannedSessions()
            val subs = subjects.value
            if (subs.isEmpty()) return@launch

            val sortedSubs = when (strategy) {
                "Nearest Exam First" -> {
                    subs.sortedBy { it.examDate ?: "2099-12-31" }
                }
                "Weakest Subject First" -> {
                    // Sort by total study minutes logged (least first)
                    subs.sortedBy { sub ->
                        studySessions.value.filter { it.subjectId == sub.id }.sumOf { it.durationMinutes }
                    }
                }
                else -> {
                    // High Priority Tag First
                    subs.sortedByDescending {
                        when (it.priority) {
                            "High" -> 3
                            "Medium" -> 2
                            else -> 1
                        }
                    }
                }
            }

            val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
            days.forEachIndexed { index, day ->
                val sub = sortedSubs[index % sortedSubs.size]
                val subTopics = topics.value.filter { it.subjectId == sub.id && it.status != "Completed" }
                val targetTopic = subTopics.firstOrNull()

                val session = PlannedSessionEntity(
                    id = UUID.randomUUID().toString(),
                    dayOfWeek = day,
                    subjectId = sub.id,
                    topicId = targetTopic?.id,
                    durationMinutes = 60,
                    time = if (index % 2 == 0) "09:00 AM" else "04:00 PM"
                )
                repository.insertPlannedSession(session)
            }
            showToast("Adaptive $strategy weekly plan generated!")
        }
    }

    fun addManualPlannedSession(day: String, subjectId: String, topicId: String?, duration: Int, time: String) {
        viewModelScope.launch {
            val session = PlannedSessionEntity(
                id = UUID.randomUUID().toString(),
                dayOfWeek = day,
                subjectId = subjectId,
                topicId = topicId,
                durationMinutes = duration,
                time = time
            )
            repository.insertPlannedSession(session)
        }
    }

    fun deletePlannedSession(session: PlannedSessionEntity) {
        viewModelScope.launch {
            repository.deletePlannedSession(session)
        }
    }

    // ----------------------------------------------------
    // PROFILE & SETTINGS
    // ----------------------------------------------------
    fun updateProfile(name: String, dailyTargetMinutes: Int, studyMode: String, priorityMode: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveProfile(
                current.copy(
                    name = name,
                    dailyTargetMinutes = dailyTargetMinutes,
                    studyMode = studyMode,
                    priorityMode = priorityMode
                )
            )
            showToast("Settings updated successfully.")
            closeModal()
        }
    }

    private suspend fun awardXp(amount: Int, reason: String) {
        val current = userProfile.value ?: UserProfileEntity()
        val newXp = current.xp + amount
        val newLevel = (newXp / 500) + 1
        repository.saveProfile(current.copy(xp = newXp, level = newLevel))
        showToast("+$amount XP! $reason")
    }

    // ----------------------------------------------------
    // BACKUP & RESTORE
    // ----------------------------------------------------
    suspend fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun restoreBackupJson(jsonString: String) {
        viewModelScope.launch {
            val success = repository.restoreBackupJson(jsonString)
            if (success) {
                showToast("Backup restored successfully!")
                closeModal()
            } else {
                showToast("Failed to restore: invalid backup format.")
            }
        }
    }
}
