package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.components.AdminBottomNavigationBar
import com.example.ui.components.AdminNavigationRail
import com.example.ui.components.AppHeader
import com.example.ui.screens.*

@Composable
fun QuizAdminApp(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val appMode by viewModel.appMode.collectAsState()
    val event by viewModel.quizEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarMessage.collect { message ->
            if (message != null) {
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    AnimatedContent(
        targetState = appMode,
        label = "AppModeTransition",
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) { mode ->
        when (mode) {
            AppMode.STUDENT -> {
                StudentQuizScreen(viewModel = viewModel)
            }
            AppMode.ADMIN -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
                    val isWideScreen = maxWidth >= 600.dp

                    if (isWideScreen) {
                        // Tablet / Desktop / Wide Landscape Layout: Navigation Rail on Left
                        Row(modifier = Modifier.fillMaxSize()) {
                            AdminNavigationRail(
                                currentTab = currentTab,
                                onTabSelected = { viewModel.selectTab(it) }
                            )

                            Scaffold(
                                topBar = {
                                    AppHeader(
                                        quizEvent = event,
                                        currentTab = currentTab,
                                        onTabSelected = { viewModel.selectTab(it) },
                                        appMode = appMode,
                                        onModeToggle = { viewModel.setAppMode(it) }
                                    )
                                },
                                snackbarHost = { SnackbarHost(snackbarHostState) },
                                containerColor = MaterialTheme.colorScheme.background
                            ) { innerPadding ->
                                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                    TabContent(
                                        currentTab = currentTab,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    } else {
                        // Mobile Portrait Layout: Header at top, Screen in middle, Bottom Nav at bottom
                        Scaffold(
                            topBar = {
                                AppHeader(
                                    quizEvent = event,
                                    currentTab = currentTab,
                                    onTabSelected = { viewModel.selectTab(it) },
                                    appMode = appMode,
                                    onModeToggle = { viewModel.setAppMode(it) }
                                )
                            },
                            bottomBar = {
                                AdminBottomNavigationBar(
                                    currentTab = currentTab,
                                    onTabSelected = { viewModel.selectTab(it) }
                                )
                            },
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            containerColor = MaterialTheme.colorScheme.background
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                TabContent(
                                    currentTab = currentTab,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    currentTab: AdminNavTab,
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = currentTab,
        label = "AdminTabTransition",
        modifier = modifier.fillMaxSize()
    ) { tab ->
        when (tab) {
            AdminNavTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
            AdminNavTab.BRANDING -> EventBrandingScreen(viewModel = viewModel)
            AdminNavTab.STAGES -> StageManagementScreen(viewModel = viewModel)
            AdminNavTab.QUESTIONS -> QuestionBankScreen(viewModel = viewModel)
            AdminNavTab.STUDENTS -> StudentManagementScreen(viewModel = viewModel)
            AdminNavTab.RESULTS -> ResultsExportScreen(viewModel = viewModel)
            AdminNavTab.SIMULATOR -> QuizSimulatorScreen(viewModel = viewModel)
        }
    }
}
