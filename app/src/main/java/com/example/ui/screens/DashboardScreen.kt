package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.AdminNavTab
import com.example.ui.AppMode
import com.example.ui.QuizAdminViewModel
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val event by viewModel.quizEvent.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val questions by viewModel.questions.collectAsState()
    val students by viewModel.students.collectAsState()
    val submissions by viewModel.submissions.collectAsState()

    val liveTimerSeconds by viewModel.liveTimerSeconds.collectAsState()
    val isLiveTimerTicking by viewModel.isLiveTimerTicking.collectAsState()

    var selectedLiveStageId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(stages) {
        if (selectedLiveStageId == null && stages.isNotEmpty()) {
            selectedLiveStageId = stages.first().id
        }
    }

    val activeStage = stages.find { it.id == selectedLiveStageId } ?: stages.firstOrNull()
    val activeStageQuestions = questions.filter { it.stageId == activeStage?.id }

    val topStudents = remember(students) {
        students.sortedWith(
            compareByDescending<Student> { it.totalScore }
                .thenBy { it.timeSpentSeconds }
        ).take(3)
    }

    val avgScore = remember(students) {
        if (students.isNotEmpty()) (students.map { it.totalScore }.average()).toInt() else 0
    }

    val avgTime = remember(students) {
        if (students.isNotEmpty()) (students.map { it.timeSpentSeconds }.average()).toInt() else 0
    }

    val correctSubmissionsCount = remember(submissions) {
        submissions.count { it.isCorrect }
    }

    val accuracyRate = remember(submissions) {
        if (submissions.isNotEmpty()) ((correctSubmissionsCount.toFloat() / submissions.size) * 100).toInt() else 0
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HERO BENTO CARD (EVENT & BRANDING OVERVIEW) ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoPrimary,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Aesthetic decorative translucent circles
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 30.dp, y = (-30).dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 16.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BentoDeepPurple
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(if (isLiveTimerTicking) Color(0xFF4ADE80) else Color(0xFFEADDFF))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isLiveTimerTicking) "ലൈവ് പ്രക്ഷേപണം സജീവം" else "ക്വിസ് അഡ്മിൻ കൺസോൾ",
                                        color = Color(0xFFEADDFF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { viewModel.selectTab(AdminNavTab.BRANDING) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ബ്രാൻഡിംഗ്",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = event?.title ?: "സംസ്ഥാന ക്വിസ് ചാമ്പ്യൻഷിപ്പ് 2026",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = event?.subtitle ?: "ഗാന്ധി സ്മൃതി & ശാസ്ത്രോത്സവം മെഗാ ഫിനാലെ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${event?.organizer ?: "ക്വിസ് സമിതി"} • ${stages.size} ഘട്ടങ്ങൾ • ${questions.size} ചോദ്യങ്ങൾ",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // --- 1.5 STUDENT QUIZ APP LAUNCHER BENTO BANNER ---
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFDCFCE7),
                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setAppMode(AppMode.STUDENT) }
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF15803D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "വിദ്യാർത്ഥി പരീക്ഷാ ആപ്പ് (Student Quiz App)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF14532D)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF15803D)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "മത്സരാർത്ഥികൾക്ക് രജിസ്റ്റർ നമ്പർ ഉപയോഗിച്ച് പരീക്ഷയെഴുതാൻ ക്ലിക്ക് ചെയ്യുക",
                            fontSize = 11.sp,
                            color = Color(0xFF166534)
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "തുറക്കുക",
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // --- 2. BENTO STATISTICS GRID (4 TILES) ---
        item {
            Text(
                text = "പ്രധാന സ്ഥിതിവിവരക്കണക്കുകൾ (Statistics)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
        }

        // Row 1: Participants & Questions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Participants Bento Card
                BentoStatCard(
                    title = "മത്സരാർത്ഥികൾ",
                    value = "${students.size}",
                    unit = "പേർ",
                    subtitle = "${students.count { it.status == StudentStatus.COMPLETED }} പേർ പരീക്ഷ പൂർത്തിയാക്കി",
                    badgeText = "ലിസ്റ്റ്",
                    icon = Icons.Default.PeopleAlt,
                    containerColor = BentoPastelPurple,
                    contentColor = BentoDeepPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.STUDENTS) }
                )

                // Questions Bento Card
                BentoStatCard(
                    title = "ചോദ്യങ്ങൾ",
                    value = "${questions.size}",
                    unit = "എണ്ണം",
                    subtitle = "${stages.size} റൗണ്ടുകളിലായി വിതരണം ചെയ്തു",
                    badgeText = "ചോദ്യശേഖരം",
                    icon = Icons.Default.Quiz,
                    containerColor = BentoPastelPink,
                    contentColor = BentoDeepPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.QUESTIONS) }
                )
            }
        }

        // Row 2: Average Score & Response Time
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avg Score Bento Card
                BentoStatCard(
                    title = "ശരാശരി സ്കോർ",
                    value = "$avgScore",
                    unit = "മാർക്ക്",
                    subtitle = "കൃത്യത: $accuracyRate% ($correctSubmissionsCount ശരി)",
                    badgeText = "റിപ്പോർട്ട്",
                    icon = Icons.Default.Leaderboard,
                    containerColor = BentoPastelViolet,
                    contentColor = BentoDeepViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.RESULTS) }
                )

                // Avg Time Bento Card
                BentoStatCard(
                    title = "ശരാശരി സമയം",
                    value = if (avgTime > 0) "$avgTime" else "35",
                    unit = "സെക്കൻഡ്",
                    subtitle = "${submissions.size} ഉത്തരങ്ങൾ രേഖപ്പെടുത്തി",
                    badgeText = "വേഗത",
                    icon = Icons.Default.Timer,
                    containerColor = BentoSurfaceContainer,
                    contentColor = BentoPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.RESULTS) }
                )
            }
        }

        // --- 3. LIVE QUIZ CONTROLS BENTO CARD (MAIN MASTER CONSOLE) ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header with Live Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BentoPastelPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Cast,
                                    contentDescription = null,
                                    tint = BentoDeepPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ക്വിസ് മാസ്റ്റർ കൺട്രോൾ റൂം",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoOnSurface
                                )
                                Text(
                                    text = "വേദിയിലെ തത്സമയ റൗണ്ട് കൺട്രോളുകൾ",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = BentoOnSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLiveTimerTicking) Color(0xFFFFEBEE) else BentoSurfaceContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isLiveTimerTicking) Color(0xFFDC2626) else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isLiveTimerTicking) "തത്സമയം ഓടുന്നു" else "തയ്യാറാണ്",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLiveTimerTicking) Color(0xFFDC2626) else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stage Selector Bento Chips
                    Text(
                        text = "കൺട്രോൾ ചെയ്യേണ്ട ഘട്ടം (Stage) തിരഞ്ഞെടുക്കുക:",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(stages) { stage ->
                            val isSelected = stage.id == activeStage?.id
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedLiveStageId = stage.id
                                    viewModel.resetLiveTimer(stage.timerSeconds)
                                },
                                shape = RoundedCornerShape(16.dp),
                                label = {
                                    Text(
                                        text = "റൗണ്ട് ${stage.stageNumber}: ${stage.title}",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BentoPastelPurple,
                                    selectedLabelColor = BentoDeepPurple
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Stage Master Card with Live Timer & Controls
                    activeStage?.let { stage ->
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoPastelPurple),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stage.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoOnSurface
                                        )
                                        Text(
                                            text = "${activeStageQuestions.size} ചോദ്യങ്ങൾ • ${stage.timerSeconds} സെക്കൻഡ് പരിധി",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = BentoOnSurfaceVariant
                                        )
                                    }

                                    // Big Live Countdown Clock Widget
                                    Surface(
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (liveTimerSeconds <= 5 && isLiveTimerTicking) Color(0xFFDC2626) else BentoDeepPurple,
                                        shadowElevation = 2.dp
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${liveTimerSeconds}s",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "ടൈമർ",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Main Control Buttons Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleLiveStageTimer(stage.timerSeconds) },
                                        modifier = Modifier.weight(1.3f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLiveTimerTicking) Color(0xFFDC2626) else BentoPrimary
                                        ),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Icon(
                                            if (isLiveTimerTicking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            if (isLiveTimerTicking) "ടൈമർ നിർത്തൂ" else "റൗണ്ട് തുടങ്ങൂ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.resetLiveTimer(stage.timerSeconds) },
                                        shape = RoundedCornerShape(18.dp),
                                        border = BorderStroke(1.dp, BentoOutline)
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "റീസെറ്റ്", modifier = Modifier.size(18.dp))
                                    }

                                    FilledTonalButton(
                                        onClick = { viewModel.startSimulator(stage) },
                                        shape = RoundedCornerShape(18.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = BentoPastelPurple,
                                            contentColor = BentoDeepPurple
                                        )
                                    ) {
                                        Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ടെസ്റ്റ് റൺ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 4. BENTO QUICK CONTROL ACTIONS (2x2 GRID) ---
        item {
            Text(
                text = "ദ്രുത കൺട്രോൾ കാർഡുകൾ (Quick Actions)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoActionCard(
                    title = "+ പുതിയ ചോദ്യം",
                    subtitle = "ചോദ്യങ്ങൾ നിർമ്മിക്കുക",
                    icon = Icons.Default.AddCircleOutline,
                    badgeColor = BentoPastelPurple,
                    iconColor = BentoDeepPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.QUESTIONS) }
                )

                BentoActionCard(
                    title = "+ പുതിയ വിദ്യാർത്ഥി",
                    subtitle = "മത്സരാർത്ഥികളെ ചേർക്കുക",
                    icon = Icons.Default.PersonAddAlt1,
                    badgeColor = BentoPastelPink,
                    iconColor = BentoDeepPink,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.STUDENTS) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoActionCard(
                    title = "സിമുലേറ്റർ റൺ",
                    subtitle = "ലൈവ് ടെസ്റ്റിംഗ് നടത്തുക",
                    icon = Icons.Default.SportsEsports,
                    badgeColor = BentoPastelViolet,
                    iconColor = BentoDeepViolet,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.SIMULATOR) }
                )

                BentoActionCard(
                    title = "CSV എക്സ്പോർട്ട്",
                    subtitle = "ഫലങ്ങൾ ഡൗൺലോഡ് ചെയ്യുക",
                    icon = Icons.Default.Share,
                    badgeColor = BentoSurfaceContainerHigh,
                    iconColor = BentoPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTab(AdminNavTab.RESULTS) }
                )
            }
        }

        // --- 5. STAGES STATUS OVERVIEW BENTO CARD ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoPastelPink),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Layers,
                                    contentDescription = null,
                                    tint = BentoDeepPink,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "റൗണ്ടുകളുടെ ക്രമീകരണ നില",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BentoPastelPurple,
                            modifier = Modifier.clickable { viewModel.selectTab(AdminNavTab.STAGES) }
                        ) {
                            Text(
                                text = "മാറ്റങ്ങൾ വരുത്തുക",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoDeepPurple,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (stages.isEmpty()) {
                        Text(
                            text = "റൗണ്ടുകളൊന്നും ചേർത്തിട്ടില്ല.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoOnSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            stages.forEachIndexed { index, stage ->
                                val stageQuestionsCount = questions.count { it.stageId == stage.id }
                                val iconBoxBg = when (stage.stageType) {
                                    StageType.MCQ -> BentoPastelPurple
                                    StageType.IMAGE_BASED -> BentoPastelPink
                                    StageType.AUDIO_BASED -> BentoPastelViolet
                                }
                                val iconTint = when (stage.stageType) {
                                    StageType.MCQ -> BentoDeepPurple
                                    StageType.IMAGE_BASED -> BentoDeepPink
                                    StageType.AUDIO_BASED -> BentoDeepViolet
                                }

                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = BentoSurfaceVariant,
                                    border = BorderStroke(1.dp, BentoOutline.copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedLiveStageId = stage.id
                                            viewModel.selectTab(AdminNavTab.STAGES)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(iconBoxBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "R${stage.stageNumber}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Black,
                                                color = iconTint
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = stage.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoOnSurface
                                            )
                                            Text(
                                                text = when (stage.stageType) {
                                                    StageType.MCQ -> "$stageQuestionsCount MCQ ചോദ്യങ്ങൾ • ${stage.timerSeconds}s"
                                                    StageType.IMAGE_BASED -> "$stageQuestionsCount ചിത്ര ചോദ്യങ്ങൾ • ${stage.timerSeconds}s"
                                                    StageType.AUDIO_BASED -> "$stageQuestionsCount ശബ്ദ ചോദ്യങ്ങൾ • ${stage.timerSeconds}s"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                fontSize = 11.sp,
                                                color = BentoOnSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = iconBoxBg
                                        ) {
                                            Text(
                                                text = when (stage.stageType) {
                                                    StageType.MCQ -> "സാധാരണ"
                                                    StageType.IMAGE_BASED -> "ചിത്രം"
                                                    StageType.AUDIO_BASED -> "ശബ്ദം"
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = iconTint,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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

        // --- 6. TOP PERFORMERS PODIUM BENTO CARD ---
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(BentoPastelViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = BentoDeepPurple,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "മികച്ച മത്സരാർത്ഥികൾ (Top Rankers)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                        }

                        TextButton(onClick = { viewModel.selectTab(AdminNavTab.RESULTS) }) {
                            Text("പൂർണ്ണ റാങ്ക് ലിസ്റ്റ്", fontWeight = FontWeight.Bold, color = BentoPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (topStudents.isEmpty()) {
                        Text(
                            text = "ഇതുവരെ സ്കോറുകൾ രേഖപ്പെടുത്തിയിട്ടില്ല. വിദ്യാർത്ഥികളെ രജിസ്റ്റർ ചെയ്ത് പരീക്ഷ നടത്തുക.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BentoOnSurfaceVariant
                        )
                    } else {
                        topStudents.forEachIndexed { index, student ->
                            val medalColor = when (index) {
                                0 -> Color(0xFFF59E0B) // Gold
                                1 -> Color(0xFF94A3B8) // Silver
                                2 -> Color(0xFFB45309) // Bronze
                                else -> BentoPrimary
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = BentoSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(medalColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoOnSurface
                                        )
                                        Text(
                                            text = "${student.registerNumber} • ${student.departmentOrGrade}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 11.sp,
                                            color = BentoOnSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${student.totalScore} മാർക്ക്",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = BentoPrimary
                                        )
                                        Text(
                                            text = "${student.timeSpentSeconds} സെക്കൻഡ്",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = BentoOnSurfaceVariant
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
}

@Composable
private fun BentoStatCard(
    title: String,
    value: String,
    unit: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = containerColor,
        border = BorderStroke(1.dp, BentoOutline.copy(alpha = 0.4f)),
        modifier = modifier
            .height(138.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = contentColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurfaceVariant
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = contentColor,
                        fontSize = 22.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = unit,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = BentoOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BentoActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    badgeColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = BentoSurface,
        border = BorderStroke(1.dp, BentoOutline),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = BentoOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
