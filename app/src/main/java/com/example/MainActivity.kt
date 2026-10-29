package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.studytracker.ui.components.FullScreenFocusOverlay
import com.example.studytracker.ui.components.StudyTrackerBottomBar
import com.example.studytracker.ui.components.StudyTrackerHeader
import com.example.studytracker.ui.screens.AnalyticsScreen
import com.example.studytracker.ui.screens.CalendarScreen
import com.example.studytracker.ui.screens.FocusModeScreen
import com.example.studytracker.ui.screens.HomeScreen
import com.example.studytracker.ui.screens.ModalsAndSheetsContainer
import com.example.studytracker.ui.screens.NotesScreen
import com.example.studytracker.ui.screens.SyllabusScreen
import com.example.studytracker.ui.screens.TasksScreen
import com.example.studytracker.ui.screens.TimerScreen
import com.example.studytracker.ui.screens.TopicDetailScreen
import com.example.studytracker.ui.viewmodel.ActiveModal
import com.example.studytracker.ui.viewmodel.NavigationTab
import com.example.studytracker.ui.viewmodel.StudyViewModel
import com.example.ui.theme.BrandTeal400
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.Navy950
import com.example.ui.theme.StudyTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyTrackerTheme(darkTheme = true) {
                StudyTrackerApp()
            }
        }
    }
}

@Composable
fun StudyTrackerApp(
    viewModel: StudyViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val timerStatus by viewModel.timerStatus.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val selectedTopic by viewModel.selectedTopicForDetail.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Navy950)
    ) {
        Scaffold(
            containerColor = Navy950,
            topBar = {
                if (selectedTopic == null) {
                    StudyTrackerHeader(
                        xp = userProfile?.xp ?: 0,
                        level = userProfile?.level ?: 1,
                        isMissionComplete = (userProfile?.streak ?: 0) > 0,
                        onOpenMission = { viewModel.openModal(ActiveModal.DAILY_MISSION) },
                        onOpenGamification = { viewModel.openModal(ActiveModal.GAMIFICATION) },
                        onOpenSettings = { viewModel.openModal(ActiveModal.SETTINGS) }
                    )
                }
            },
            bottomBar = {
                if (selectedTopic == null) {
                    StudyTrackerBottomBar(
                        currentTab = currentTab,
                        timerStatus = timerStatus,
                        onSelectTab = { tab -> viewModel.selectTab(tab) },
                        modifier = Modifier.navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // If a topic is opened, show TopicDetailScreen
                if (selectedTopic != null) {
                    TopicDetailScreen(
                        topic = selectedTopic!!,
                        viewModel = viewModel
                    )
                } else {
                    // Main 6 Navigation Tabs
                    when (currentTab) {
                        NavigationTab.HOME -> {
                            HomeScreen(viewModel = viewModel)
                        }
                        NavigationTab.CALENDAR -> {
                            CalendarScreen(viewModel = viewModel)
                        }
                        NavigationTab.FOCUS -> {
                            FocusModeScreen(viewModel = viewModel)
                        }
                        NavigationTab.NOTES -> {
                            NotesScreen(viewModel = viewModel)
                        }
                        NavigationTab.SYLLABUS -> {
                            SyllabusScreen(viewModel = viewModel)
                        }
                        NavigationTab.TIMER -> {
                            FocusModeScreen(viewModel = viewModel)
                        }
                        NavigationTab.ANALYTICS -> {
                            AnalyticsScreen(viewModel = viewModel)
                        }
                        NavigationTab.TASKS -> {
                            TasksScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }

        // Modals & BottomSheets Overlay
        ModalsAndSheetsContainer(viewModel = viewModel)

        // Full-Screen Focus Mode Overlay (Disables navigation interaction, keeps timer visible, Stop Focus Mode button)
        val isFocusOverlayActive by viewModel.isFocusOverlayActive.collectAsState()
        if (isFocusOverlayActive) {
            FullScreenFocusOverlay(
                viewModel = viewModel,
                onStopFocusMode = { viewModel.toggleFocusMode(false) }
            )
        }
    }
}
