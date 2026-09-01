package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.AppMode
import com.example.ui.QuizAdminViewModel
import com.example.ui.StudentQuizPhase
import com.example.ui.theme.*

@Composable
fun StudentQuizScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val quizState by viewModel.studentQuizState.collectAsState()
    val event by viewModel.quizEvent.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val registeredStudents by viewModel.students.collectAsState()

    val isAdminPasswordDialogOpen by viewModel.isAdminPasswordDialogOpen.collectAsState()
    val adminPasswordInput by viewModel.adminPasswordInput.collectAsState()
    val adminPasswordError by viewModel.adminPasswordError.collectAsState()

    // Security: strictly restrict back navigation once exam is in active phase
    BackHandler(enabled = quizState.phase == StudentQuizPhase.ACTIVE_QUIZ) {
        viewModel.showBackWarningDialog(true)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        AnimatedContent(
            targetState = quizState.phase,
            label = "StudentQuizPhaseTransition",
            modifier = Modifier.fillMaxSize()
        ) { phase ->
            when (phase) {
                StudentQuizPhase.LOGIN -> {
                    StudentLoginView(
                        quizState = quizState,
                        event = event,
                        registeredStudents = registeredStudents,
                        onRegNoChange = { viewModel.updateStudentRegNoInput(it) },
                        onLogin = { viewModel.loginStudentWithRegNo(it) },
                        onSecretAdminTrigger = { viewModel.openAdminPasswordDialog() }
                    )
                }

                StudentQuizPhase.INSTRUCTIONS -> {
                    StudentInstructionsView(
                        quizState = quizState,
                        event = event,
                        stages = stages,
                        totalQuestionsCount = questions.size,
                        onStartExam = { viewModel.startStudentExam() },
                        onCancel = { viewModel.resetStudentQuiz() }
                    )
                }

                StudentQuizPhase.ACTIVE_QUIZ -> {
                    StudentActiveExamView(
                        viewModel = viewModel,
                        quizState = quizState,
                        stages = stages
                    )
                }

                StudentQuizPhase.SUBMISSION_SUMMARY -> {
                    StudentThankYouView(
                        quizState = quizState,
                        event = event,
                        stages = stages,
                        onNewStudentLogin = { viewModel.resetStudentQuiz() },
                        onSecretAdminTrigger = { viewModel.openAdminPasswordDialog() }
                    )
                }
            }
        }

        // Security Alert Dialog: Prevent leaving active exam
        if (quizState.isBackDialogVisible) {
            AlertDialog(
                onDismissRequest = { viewModel.showBackWarningDialog(false) },
                icon = {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "പരീക്ഷ പുരോഗമിക്കുകയാണ്!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        text = "പരീക്ഷ അവസാനിക്കുന്നതുവരെ പുറത്തുപോകാൻ സാധിക്കില്ല. എല്ലാ ചോദ്യങ്ങൾക്കും ഉത്തരമെഴുതി 'സമർപ്പിക്കുക' ബട്ടൺ അമർത്തുക.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.showBackWarningDialog(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("പരീക്ഷ തുടരുക", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Admin Password Security Dialog (Protected Admin Entrance)
        if (isAdminPasswordDialogOpen) {
            AdminPasswordDialog(
                passwordInput = adminPasswordInput,
                errorMessage = adminPasswordError,
                onPasswordChange = { viewModel.updateAdminPasswordInput(it) },
                onConfirm = { viewModel.verifyAndUnlockAdmin() },
                onDismiss = { viewModel.closeAdminPasswordDialog() }
            )
        }

        // Submission Confirmation Dialog
        if (quizState.isConfirmSubmitDialogVisible) {
            val answeredCount = quizState.answers.size
            val totalCount = questions.size
            val unansweredCount = (totalCount - answeredCount).coerceAtLeast(0)

            AlertDialog(
                onDismissRequest = { viewModel.cancelSubmitExamPrompt() },
                icon = {
                    Icon(
                        Icons.Default.CheckCircleOutline,
                        contentDescription = null,
                        tint = BentoPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                },
                title = {
                    Text(
                        text = "പരീക്ഷ സമർപ്പിക്കണോ?",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "താങ്കൾ നൽകിയ ഉത്തരങ്ങൾ സ്ഥിരീകരിക്കാനും സ്കോർ രേഖപ്പെടുത്താനും തയ്യാറാണോ?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BentoSurfaceContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "ഉത്തരമെഴുതിയവ: $answeredCount / $totalCount",
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimary,
                                    fontSize = 13.sp
                                )
                                if (unansweredCount > 0) {
                                    Text(
                                        text = "ശ്രദ്ധിക്കുക: $unansweredCount ചോദ്യങ്ങൾക്ക് ഉത്തരം നൽകിയിട്ടില്ല.",
                                        color = Color(0xFFDC2626),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmSubmitExam() },
                        colors = ButtonDefaults.buttonColors(containerColor = BentoDeepPurple),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("അതെ, സമർപ്പിക്കൂ", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.cancelSubmitExamPrompt() }) {
                        Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Fullscreen Image Zoom Dialog for visual clues
        if (quizState.isZoomModalVisible && quizState.zoomMediaUri.isNotBlank()) {
            Dialog(
                onDismissRequest = { viewModel.closeImageZoomModal() },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.94f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        IconButton(
                            onClick = { viewModel.closeImageZoomModal() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "അടയ്ക്കുക", tint = Color.White)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AsyncImage(
                                model = quizState.zoomMediaUri,
                                contentDescription = "ചിത്ര ക്ലൂ സൂം",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )

                            if (quizState.zoomMediaCaption.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = quizState.zoomMediaCaption,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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

// =========================================================================
// 1. STUDENT LOGIN VIEW (REGISTER NUMBER VERIFICATION)
// =========================================================================
@Composable
private fun StudentLoginView(
    quizState: com.example.ui.StudentQuizState,
    event: QuizEvent?,
    registeredStudents: List<Student>,
    onRegNoChange: (String) -> Unit,
    onLogin: (String?) -> Unit,
    onSecretAdminTrigger: () -> Unit
) {
    var secretTapCount by remember { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))

            // Role Header Pill (Clean student portal indicator)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "വിദ്യാർത്ഥി പരീക്ഷാ പോർട്ടൽ (Student Portal)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )
                }
            }
        }

        // Hero Bento Banner
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoPrimary,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 20.dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )

                    Column {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoDeepPurple,
                            modifier = Modifier.clickable {
                                secretTapCount++
                                if (secretTapCount >= 3) {
                                    secretTapCount = 0
                                    onSecretAdminTrigger()
                                }
                            }
                        ) {
                            Text(
                                text = "STUDENT PORTAL",
                                color = Color(0xFFEADDFF),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = event?.title ?: "സംസ്ഥാന ക്വിസ് ചാമ്പ്യൻഷിപ്പ്",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = event?.subtitle ?: "തത്സമയ പരീക്ഷയിൽ പങ്കെടുക്കൂ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                            Text(
                                text = event?.organizer ?: "ക്വിസ് സമിതി",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Login Input Bento Card
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "പരീക്ഷയിൽ പ്രവേശിക്കുക (Login)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )
                    Text(
                        text = "അഡ്മിൻ നൽകിയ സാധുവായ രജിസ്റ്റർ നമ്പർ രേഖപ്പെടുത്തുക",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = quizState.registerNumberInput,
                        onValueChange = onRegNoChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("രജിസ്റ്റർ നമ്പർ (Register Number)") },
                        placeholder = { Text("ഉദാ: KL-2026-101") },
                        leadingIcon = {
                            Icon(Icons.Default.Badge, contentDescription = null, tint = BentoPrimary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoOutline
                        )
                    )

                    // Error Alert Box
                    if (quizState.loginErrorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFEBEE),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = quizState.loginErrorMessage,
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { onLogin(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("പരീക്ഷയിലേക്ക് പ്രവേശിക്കുക", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Demo Students Chips (For fast testing)
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = BentoSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "രജിസ്റ്റർ ചെയ്ത മത്സരാർത്ഥികൾ (ദ്രുത ലോഗിൻ):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnSurfaceVariant
                        )
                        Text(
                            text = "${registeredStudents.size} പേർ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(registeredStudents) { st ->
                            val isCompleted = st.status == StudentStatus.COMPLETED
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isCompleted) BentoSurfaceContainer else BentoPastelPurple,
                                border = BorderStroke(1.dp, if (isCompleted) BentoOutline else BentoPastelPurple),
                                modifier = Modifier.clickable {
                                    onRegNoChange(st.registerNumber)
                                    onLogin(st.registerNumber)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${st.registerNumber} (${st.name})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) BentoOnSurfaceVariant else BentoDeepPurple
                                    )
                                    if (isCompleted) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Discreet Footer with subtle padlock trigger for Admin Access
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "© 2026 ${event?.organizer ?: "ക്വിസ് പ്ലാറ്റ്‌ഫോം"} • സിസ്റ്റം v1.0",
                    fontSize = 11.sp,
                    color = BentoOnSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Very subtle discreet admin lock button
                IconButton(
                    onClick = onSecretAdminTrigger,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "അഡ്മിൻ സുരക്ഷാ ലോഗിൻ",
                        tint = BentoOnSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. STUDENT INSTRUCTIONS VIEW
// =========================================================================
@Composable
private fun StudentInstructionsView(
    quizState: com.example.ui.StudentQuizState,
    event: QuizEvent?,
    stages: List<QuizStage>,
    totalQuestionsCount: Int,
    onStartExam: () -> Unit,
    onCancel: () -> Unit
) {
    val student = quizState.loggedInStudent ?: return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Student Profile Header Card
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(BentoPastelPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(1),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoDeepPurple
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnSurface
                        )
                        Text(
                            text = "രജിസ്റ്റർ നമ്പർ: ${student.registerNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                        Text(
                            text = student.departmentOrGrade,
                            fontSize = 11.sp,
                            color = BentoOnSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = "വെരിഫൈഡ്",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quiz Breakdown Bento Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoPastelPurple,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ആകെ റൗണ്ടുകൾ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                        Text("${stages.size} ഘട്ടങ്ങൾ", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BentoDeepPurple)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoPastelPink,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ആകെ ചോദ്യങ്ങൾ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDeepPink)
                        Text("$totalQuestionsCount ചോദ്യങ്ങൾ", fontSize = 18.sp, fontWeight = FontWeight.Black, color = BentoDeepPink)
                    }
                }
            }
        }

        // Examination Guidelines Card
        item {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "പ്രധാന പരീക്ഷാ നിർദ്ദേശങ്ങൾ:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    InstructionRow(
                        number = "1",
                        title = "ഘട്ടങ്ങളായുള്ള ചോദ്യങ്ങൾ",
                        desc = "MCQ, ചിത്രങ്ങൾ, ഓഡിയോ ക്ലൂകൾ എന്നിവ അടങ്ങിയ വിവിധ റൗണ്ടുകൾ ഉണ്ടാകും."
                    )
                    InstructionRow(
                        number = "2",
                        title = "സമയ പരിധി (Timer)",
                        desc = "ഓരോ ചോദ്യത്തിനും നിശ്ചിത സമയപരിധിയുണ്ട്. സമയം കഴിഞ്ഞാൽ ചോദ്യം ഓട്ടോമാറ്റിക്കായി അടുത്തതിലേക്ക് മാറും."
                    )
                    InstructionRow(
                        number = "3",
                        title = "സുരക്ഷാ നിയന്ത്രണം (Security)",
                        desc = "പരീക്ഷ തുടങ്ങിയാൽ ബാക്ക് പോകാനോ ടാബ് മാറാനോ സാധിക്കില്ല."
                    )
                    InstructionRow(
                        number = "4",
                        title = "അന്തിമ സമർപ്പണം (Submission)",
                        desc = "എല്ലാ ചോദ്യങ്ങൾക്കും ഉത്തരമെഴുതിയ ശേഷം അവസാന പേജിൽ സമർപ്പിക്കുക. ഫലം ഉടനടി ലഭ്യമാകും."
                    )
                }
            }
        }

        // Actions
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onStartExam,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("പരീക്ഷ ആരംഭിക്കുക (Start Quiz)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("മറ്റൊരു രജിസ്റ്റർ നമ്പറിൽ ലോഗിൻ ചെയ്യുക", color = BentoOnSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun InstructionRow(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(BentoPastelPurple),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoOnSurface)
            Text(desc, fontSize = 11.sp, color = BentoOnSurfaceVariant)
        }
    }
}

// =========================================================================
// 3. ACTIVE STAGE-BY-STAGE EXAM VIEW
// =========================================================================
@Composable
private fun StudentActiveExamView(
    viewModel: QuizAdminViewModel,
    quizState: com.example.ui.StudentQuizState,
    stages: List<QuizStage>
) {
    val currentStage = stages.getOrNull(quizState.currentStageIndex)
    val currentQ = quizState.stageQuestions.getOrNull(quizState.currentQuestionIndex)
    val audioState by viewModel.audioPlayer.playbackState.collectAsState()
    val isAudioPlaying = audioState.isPlaying

    val timeRemaining = quizState.questionTimeRemaining
    val isUrgent = timeRemaining <= 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBackground)
    ) {
        // Sticky Top Control Bar
        Surface(
            color = BentoSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Stage Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoPastelPurple
                    ) {
                        Text(
                            text = "റൗണ്ട് ${quizState.currentStageIndex + 1}/${stages.size}: ${currentStage?.title ?: ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Countdown Clock Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isUrgent) Color(0xFFDC2626) else BentoDeepPurple,
                        shadowElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${timeRemaining}s",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // Prompt Submit Action
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BentoSurfaceContainer,
                        modifier = Modifier.clickable { viewModel.promptSubmitExam() }
                    ) {
                        Text(
                            text = "സമർപ്പിക്കുക",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Linear Progress Indicator
                val progress = if (quizState.stageQuestions.isNotEmpty()) {
                    (quizState.currentQuestionIndex + 1).toFloat() / quizState.stageQuestions.size
                } else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ചോദ്യം ${quizState.currentQuestionIndex + 1} / ${quizState.stageQuestions.size}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% പൂർത്തിയായി",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BentoPrimary,
                    trackColor = BentoSurfaceContainer
                )
            }
        }

        // Question & Clue Body
        if (currentQ != null) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Question Text Bento Card
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = BentoSurface,
                        border = BorderStroke(1.dp, BentoOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = BentoPastelPurple
                                ) {
                                    Text(
                                        text = "+${currentQ.points} മാർക്ക്",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDeepPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = when (currentQ.questionType) {
                                        StageType.MCQ -> "സാധാരണ MCQ"
                                        StageType.IMAGE_BASED -> "ചിത്ര ക്ലൂ ചോദ്യം"
                                        StageType.AUDIO_BASED -> "ശബ്ദ ക്ലൂ ചോദ്യം"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BentoOnSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentQ.questionText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Image Clue with Zoom Feature (StageType.IMAGE_BASED)
                if (currentQ.questionType == StageType.IMAGE_BASED && currentQ.mediaUri.isNotBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = BentoSurface,
                            border = BorderStroke(1.dp, BentoOutline),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.openImageZoomModal(currentQ.mediaUri, currentQ.mediaCaption)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                ) {
                                    AsyncImage(
                                        model = currentQ.mediaUri,
                                        contentDescription = "ചിത്ര ചോദ്യം",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("സൂം ചെയ്യൂ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                if (currentQ.mediaCaption.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentQ.mediaCaption,
                                        fontSize = 11.sp,
                                        color = BentoOnSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Audio Clue Player (StageType.AUDIO_BASED)
                if (currentQ.questionType == StageType.AUDIO_BASED && currentQ.mediaUri.isNotBlank()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = BentoPastelViolet,
                            border = BorderStroke(1.dp, BentoPastelPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isAudioPlaying) {
                                            viewModel.audioPlayer.stop()
                                        } else {
                                            viewModel.audioPlayer.playAudio(currentQ.mediaUri, "ഓഡിയോ ക്ലൂ")
                                        }
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(BentoDeepViolet, CircleShape)
                                ) {
                                    Icon(
                                        if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "ഓഡിയോ പ്ലേ ചെയ്യുക",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isAudioPlaying) "ശബ്ദ ക്ലിപ്പ് പ്ലേ ചെയ്യുന്നു..." else "ശബ്ദ ക്ലിപ്പ് കേൾക്കൂ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoDeepViolet
                                    )
                                    Text(
                                        text = currentQ.mediaCaption.ifBlank { "ശബ്ദം തിരിച്ചറിഞ്ഞ് ശരിയുത്തരം തിരഞ്ഞെടുക്കുക" },
                                        fontSize = 11.sp,
                                        color = BentoDeepViolet.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4 Interactive Bento Option Cards (A, B, C, D)
                val chosenOption = quizState.answers[currentQ.id]
                val options = listOf(
                    "A" to currentQ.optionA,
                    "B" to currentQ.optionB,
                    "C" to currentQ.optionC,
                    "D" to currentQ.optionD
                ).filter { it.second.isNotBlank() }

                items(options) { (optLetter, optText) ->
                    val isSelected = chosenOption == optLetter

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) BentoPastelPurple else BentoSurface,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) BentoPrimary else BentoOutline
                        ),
                        shadowElevation = if (isSelected) 2.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectStudentAnswer(optLetter) }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) BentoPrimary else BentoSurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = optLetter,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) Color.White else BentoOnSurface
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) BentoDeepPurple else BentoOnSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BentoPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar
        Surface(
            color = BentoSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Button
                if (quizState.currentQuestionIndex > 0) {
                    OutlinedButton(
                        onClick = { viewModel.studentPreviousQuestion() },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, BentoOutline),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("മുമ്പത്തെ ചോദ്യം", fontSize = 12.sp)
                    }
                }

                // Next or Submit Button
                val isLastQuestionInStage = quizState.currentQuestionIndex >= quizState.stageQuestions.size - 1
                val isLastStage = quizState.currentStageIndex >= stages.size - 1

                Button(
                    onClick = {
                        if (isLastQuestionInStage && isLastStage) {
                            viewModel.promptSubmitExam()
                        } else {
                            viewModel.studentNextQuestion()
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastQuestionInStage && isLastStage) BentoDeepPurple else BentoPrimary
                    )
                ) {
                    Text(
                        text = if (isLastQuestionInStage && isLastStage) "പരീക്ഷ പൂർത്തിയാക്കൂ" else "അടുത്ത ചോദ്യം",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        if (isLastQuestionInStage && isLastStage) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. THANK YOU / SUBMISSION RESULTS VIEW
// =========================================================================
@Composable
private fun StudentThankYouView(
    quizState: com.example.ui.StudentQuizState,
    event: QuizEvent?,
    stages: List<QuizStage>,
    onNewStudentLogin: () -> Unit,
    onSecretAdminTrigger: () -> Unit
) {
    val student = quizState.loggedInStudent ?: return
    val score = quizState.finalScore
    val maxScore = quizState.maxPossibleScore.coerceAtLeast(1)
    val percentage = ((score.toFloat() / maxScore) * 100).toInt()

    val passingPercentage = event?.passingScorePercentage ?: 50
    val isPassed = percentage >= passingPercentage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Celebratory Trophy Badge
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) Color(0xFFDCFCE7) else BentoPastelPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = if (isPassed) Color(0xFF15803D) else BentoDeepPurple,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "നന്ദി, പരീക്ഷ പൂർത്തിയായി!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = BentoOnSurface
            )
            Text(
                text = "നിങ്ങളുടെ ഉത്തരങ്ങൾ അഡ്മിൻ കൺസോളിലേക്ക് വിജയകരമായി അയച്ചു.",
                style = MaterialTheme.typography.bodyMedium,
                color = BentoOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Score Card Bento Grid
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoPrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = student.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "രജിസ്റ്റർ നമ്പർ: ${student.registerNumber}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$score / $maxScore",
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "ആകെ സ്കോർ ($percentage%)",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isPassed) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                    ) {
                        Text(
                            text = if (isPassed) "വിജയിച്ചു (QUALIFIED)" else "പങ്കെടുത്തു (PARTICIPATED)",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Performance Statistics Bento Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoSurface,
                    border = BorderStroke(1.dp, BentoOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("എടുത്ത സമയം", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Text("${quizState.totalExamTimeSeconds}s", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BentoPrimary)
                        Text("സെക്കൻഡുകൾ", fontSize = 10.sp, color = BentoOnSurfaceVariant)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = BentoSurface,
                    border = BorderStroke(1.dp, BentoOutline),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("നൽകിയ ഉത്തരങ്ങൾ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
                        Text("${quizState.answers.size}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BentoDeepPurple)
                        Text("ചോദ്യങ്ങൾ", fontSize = 10.sp, color = BentoOnSurfaceVariant)
                    }
                }
            }
        }

        // Action Buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onNewStudentLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("മറ്റൊരു വിദ്യാർത്ഥിക്ക് ലോഗിൻ ചെയ്യുക", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Discreet Footer with subtle lock trigger
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "© 2026 ${event?.organizer ?: "ഡിജിറ്റൽ ക്വിസ് പ്ലാറ്റ്‌ഫോം"} • സിസ്റ്റം പൂർത്തിയായി",
                    fontSize = 11.sp,
                    color = BentoOnSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Discreet lock icon button for admin verification
                IconButton(
                    onClick = onSecretAdminTrigger,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "അഡ്മിൻ പ്രവേശനം",
                        tint = BentoOnSurfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 5. ADMIN PASSWORD SECURITY DIALOG
// =========================================================================
@Composable
private fun AdminPasswordDialog(
    passwordInput: String,
    errorMessage: String?,
    onPasswordChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BentoPastelPurple),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = BentoPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "അഡ്മിൻ പ്രവേശനം (Admin Access)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ക്വിസ് ക്രമീകരണങ്ങളും ഡാഷ്‌ബോർഡും നിയന്ത്രിക്കാൻ അഡ്മിൻ പാസ്‌വേഡ് നൽകുക.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("പാസ്‌വേഡ് (PIN)") },
                    placeholder = { Text("പാസ്‌വേഡ് നൽകുക") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "പാസ്‌വേഡ് മറയ്ക്കുക" else "പാസ്‌വേഡ് കാണിക്കുക",
                                tint = BentoOnSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoOutline
                    )
                )

                if (errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFDC2626),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                } else {
                    Text(
                        text = "സൂചന: സ്ഥിരസ്ഥിതി പാസ്‌വേഡ്: 1234",
                        fontSize = 10.sp,
                        color = BentoOnSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("പ്രവേശിക്കുക", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
            }
        },
        containerColor = BentoSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
