package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.QuizStage
import com.example.data.model.StageType
import com.example.ui.AdminNavTab
import com.example.ui.QuizAdminViewModel
import com.example.ui.components.SAMPLE_IMAGE_PRESETS
import com.example.ui.theme.*

@Composable
fun QuizSimulatorScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val simState by viewModel.simulatorState.collectAsState()
    val stages by viewModel.stages.collectAsState()
    val playbackState by viewModel.audioPlayer.playbackState.collectAsState()

    var selectedStageForTest by remember(stages) { mutableStateOf(stages.firstOrNull()) }

    if (!simState.isRunning || simState.questions.isEmpty()) {
        // --- STAGE SELECTION LAUNCHER VIEW IN BENTO STYLE ---
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(BentoBackground),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "തത്സമയ ക്വിസ് സിമുലേറ്റർ (പരിശീലനം)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )
                Text(
                    text = "മത്സരാർത്ഥികൾക്ക് അനുഭവപ്പെടുന്നതുപോലെ ടൈമർ, ഓഡിയോ, ചിത്രം, തൽക്ഷണ സ്കോറിംഗ് എന്നിവ പരീക്ഷിച്ച് നോക്കുക.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = BentoSurface,
                    border = BorderStroke(1.dp, BentoOutline),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "പരീക്ഷിക്കാൻ ഒരു ഘട്ടം (STAGE) തിരഞ്ഞെടുക്കുക",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnSurfaceVariant
                        )

                        if (stages.isEmpty()) {
                            Text(
                                text = "ഘട്ടങ്ങളൊന്നും ലഭ്യമല്ല. ദയവായി ഘട്ടങ്ങളുടെ പേജിൽ പുതിയ ഘട്ടം ഉണ്ടാക്കുക.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            stages.forEach { stage ->
                                val isSelected = selectedStageForTest?.id == stage.id
                                val (badgeBg, badgeText, icon, typeMalayalam) = when (stage.stageType) {
                                    StageType.MCQ -> Quadruple(BentoPastelPurple, BentoDeepPurple, Icons.Default.FormatListBulleted, "സാധാരണ ചോദ്യങ്ങൾ")
                                    StageType.IMAGE_BASED -> Quadruple(BentoPastelPink, BentoDeepPink, Icons.Default.Image, "ചിത്രം അടിസ്ഥാനമാക്കിയത്")
                                    StageType.AUDIO_BASED -> Quadruple(BentoPastelViolet, BentoDeepViolet, Icons.Default.GraphicEq, "ശബ്ദ സൂചനയുള്ളത്")
                                }

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) badgeBg.copy(alpha = 0.6f) else BentoSurfaceVariant,
                                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) badgeText else BentoOutline),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedStageForTest = stage }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(badgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(icon, contentDescription = null, tint = badgeText, modifier = Modifier.size(20.dp))
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
                                                text = "$typeMalayalam • ${stage.timerSeconds} സെക്കൻഡ് ടൈമർ",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = BentoOnSurfaceVariant
                                            )
                                        }
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedStageForTest = stage },
                                            colors = RadioButtonDefaults.colors(selectedColor = BentoPrimary)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Surface(
                                shape = RoundedCornerShape(28.dp),
                                color = BentoPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable {
                                        selectedStageForTest?.let { viewModel.startSimulator(it) }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("സിമുലേഷൻ ആരംഭിക്കുക", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // --- STAGE FINISHED SUMMARY VIEW IN BENTO STYLE ---
    if (simState.isStageFinished) {
        val totalQuestions = simState.questions.size
        val maxPoints = simState.questions.sumOf { it.points }
        val percentage = if (maxPoints > 0) ((simState.simulatedScore.toFloat() / maxPoints) * 100).toInt() else 0
        val isPassed = percentage >= 50

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BentoBackground)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(if (isPassed) BentoPastelPurple else Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPassed) Icons.Default.EmojiEvents else Icons.Default.Replay,
                            contentDescription = null,
                            tint = if (isPassed) BentoDeepPurple else Color(0xFFDC2626),
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (isPassed) "ഘട്ടം വിജയകരമായി പൂർത്തിയായി!" else "പരീക്ഷണം പൂർത്തിയായി",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = BentoOnSurface
                    )

                    Text(
                        text = simState.selectedStage?.title ?: "ക്വിസ് ഘട്ടം",
                        style = MaterialTheme.typography.bodyMedium,
                        color = BentoOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = BentoPastelPurple,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("നേടിയ മാർക്ക്", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                                Text("${simState.simulatedScore}/$maxPoints", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BentoDeepPurple)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ശതമാനം", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                                Text("$percentage%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isPassed) Color(0xFF166534) else Color(0xFF991B1B))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ആകെ ചോദ്യങ്ങൾ", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoDeepPurple)
                                Text("$totalQuestions എണ്ണം", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BentoDeepPurple)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = BentoSurfaceVariant,
                            border = BorderStroke(1.dp, BentoOutline),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { simState.selectedStage?.let { viewModel.startSimulator(it) } }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("വീണ്ടും ചെയ്യുക", fontWeight = FontWeight.Bold, color = BentoTextDark)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = BentoPrimary,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { viewModel.exitSimulator() }
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("പ്രധാന മെനുവിലേക്ക്", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
        return
    }

    // --- ACTIVE QUESTION TEST RUN VIEW IN BENTO STYLE ---
    val currentQuestion = simState.questions.getOrNull(simState.currentQuestionIndex) ?: return

    val timerColor = when {
        simState.timeRemainingSeconds <= 5 -> Color(0xFFDC2626)
        simState.timeRemainingSeconds <= 10 -> Color(0xFFD97706)
        else -> BentoPrimary
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Stage Header & Exit Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = simState.selectedStage?.title ?: "ക്വിസ് ഘട്ടം",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoPrimary
                    )
                    Text(
                        text = "ചോദ്യം ${simState.currentQuestionIndex + 1} / ${simState.questions.size} • ${currentQuestion.points} മാർക്ക്",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoOnSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { viewModel.exitSimulator() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(BentoSurfaceVariant, CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Test", tint = BentoOnSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Live Question Bento Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = BentoSurface,
                border = BorderStroke(1.dp, BentoOutline),
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Circular Timer Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (currentQuestion.questionType) {
                                StageType.MCQ -> BentoPastelPurple
                                StageType.IMAGE_BASED -> BentoPastelPink
                                StageType.AUDIO_BASED -> BentoPastelViolet
                            }
                        ) {
                            Text(
                                text = when (currentQuestion.questionType) {
                                    StageType.MCQ -> "സാധാരണ ചോദ്യം"
                                    StageType.IMAGE_BASED -> "ചിത്ര സൂചന"
                                    StageType.AUDIO_BASED -> "ശബ്ദ സൂചന"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentQuestion.questionType) {
                                    StageType.MCQ -> BentoDeepPurple
                                    StageType.IMAGE_BASED -> BentoDeepPink
                                    StageType.AUDIO_BASED -> BentoDeepViolet
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Big Timer Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = timerColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = timerColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${simState.timeRemainingSeconds} സെക്കൻഡ്",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = timerColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = currentQuestion.questionText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = BentoOnSurface
                    )

                    // Image Media Clue
                    if (currentQuestion.questionType == StageType.IMAGE_BASED && currentQuestion.mediaUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val preset = SAMPLE_IMAGE_PRESETS.find { it.key == currentQuestion.mediaUri }
                        val displayUrl = preset?.imageUrl ?: currentQuestion.mediaUri

                        AsyncImage(
                            model = ImageRequest.Builder(context).data(displayUrl).crossfade(true).build(),
                            contentDescription = "Visual Clue",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        if (currentQuestion.mediaCaption.isNotBlank()) {
                            Text(
                                text = "ചിത്ര സൂചന: ${currentQuestion.mediaCaption}",
                                style = MaterialTheme.typography.labelSmall,
                                color = BentoOnSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Audio Media Clue
                    if (currentQuestion.questionType == StageType.AUDIO_BASED && currentQuestion.mediaUri.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val isPlaying = playbackState.isPlaying && playbackState.currentAudioId == currentQuestion.mediaUri

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = BentoPastelViolet,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (isPlaying) {
                                            viewModel.audioPlayer.stop()
                                        } else {
                                            viewModel.audioPlayer.playAudio(currentQuestion.mediaUri, "Mystery Audio")
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(BentoDeepViolet, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                        contentDescription = "Play Audio",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isPlaying) "ശബ്ദം കേൾക്കുന്നു..." else "ശബ്ദ സൂചന കേൾക്കാൻ ഇവിടെ തൊടുക",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoDeepViolet
                                    )
                                    Text(
                                        text = currentQuestion.mediaCaption.ifBlank { "ശബ്ദം ശ്രദ്ധിച്ചു കേട്ട് ശരിയായ ഉത്തരം കണ്ടെത്തുക" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = BentoOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // MCQ Interactive Option Bento Rows
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SimulatorOptionRow(
                    label = "A",
                    text = currentQuestion.optionA,
                    isSelected = simState.selectedOption == "A",
                    isSubmitted = simState.isAnswerSubmitted,
                    isCorrect = currentQuestion.correctAnswer == "A",
                    onSelect = { viewModel.selectSimulatorOption("A") }
                )
                SimulatorOptionRow(
                    label = "B",
                    text = currentQuestion.optionB,
                    isSelected = simState.selectedOption == "B",
                    isSubmitted = simState.isAnswerSubmitted,
                    isCorrect = currentQuestion.correctAnswer == "B",
                    onSelect = { viewModel.selectSimulatorOption("B") }
                )
                if (currentQuestion.optionC.isNotBlank()) {
                    SimulatorOptionRow(
                        label = "C",
                        text = currentQuestion.optionC,
                        isSelected = simState.selectedOption == "C",
                        isSubmitted = simState.isAnswerSubmitted,
                        isCorrect = currentQuestion.correctAnswer == "C",
                        onSelect = { viewModel.selectSimulatorOption("C") }
                    )
                }
                if (currentQuestion.optionD.isNotBlank()) {
                    SimulatorOptionRow(
                        label = "D",
                        text = currentQuestion.optionD,
                        isSelected = simState.selectedOption == "D",
                        isSubmitted = simState.isAnswerSubmitted,
                        isCorrect = currentQuestion.correctAnswer == "D",
                        onSelect = { viewModel.selectSimulatorOption("D") }
                    )
                }
            }
        }

        // Explanation & Result Card on Submit
        if (simState.isAnswerSubmitted) {
            item {
                val isCorrect = simState.selectedOption == currentQuestion.correctAnswer
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFFEBEE),
                    border = BorderStroke(1.dp, if (isCorrect) Color(0xFF86EFAC) else Color(0xFFFFCDD2)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                contentDescription = null,
                                tint = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isCorrect) "ശരിയുത്തരം! (+${currentQuestion.points} മാർക്ക്)" else "തെറ്റായ ഉത്തരം (0 മാർക്ക്)",
                                fontWeight = FontWeight.Bold,
                                color = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                        }

                        if (currentQuestion.explanation.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentQuestion.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoTextDark
                            )
                        }
                    }
                }
            }
        }

        // Action Button: Submit or Next Question
        item {
            Spacer(modifier = Modifier.height(4.dp))
            if (!simState.isAnswerSubmitted) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (simState.selectedOption != null) BentoPrimary else BentoOutline,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(enabled = simState.selectedOption != null) {
                            viewModel.submitSimulatorAnswer()
                        }
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "ഉത്തരം സമർപ്പിക്കുക",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (simState.selectedOption != null) Color.White else BentoOnSurfaceVariant
                        )
                    }
                }
            } else {
                val isLastQ = simState.currentQuestionIndex >= simState.questions.size - 1
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = BentoPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable { viewModel.nextSimulatorQuestion() }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            if (isLastQ) "ഘട്ടം പൂർത്തിയാക്കുക" else "അടുത്ത ചോദ്യം",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SimulatorOptionRow(
    label: String,
    text: String,
    isSelected: Boolean,
    isSubmitted: Boolean,
    isCorrect: Boolean,
    onSelect: () -> Unit
) {
    if (text.isBlank()) return

    val (bgColor, borderColor, textColor) = when {
        isSubmitted && isCorrect -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), Color(0xFF166534))
        isSubmitted && isSelected && !isCorrect -> Triple(Color(0xFFFFEBEE), Color(0xFFDC2626), Color(0xFF991B1B))
        isSelected -> Triple(BentoPastelPurple, BentoPrimary, BentoDeepPurple)
        else -> Triple(BentoSurface, BentoOutline, BentoOnSurface)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(if (isSelected || (isSubmitted && isCorrect)) 2.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSubmitted) { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isSelected || (isSubmitted && isCorrect)) borderColor else BentoSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected || (isSubmitted && isCorrect)) Color.White else BentoOnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (isSubmitted) {
                if (isCorrect) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF166534))
                } else if (isSelected) {
                    Icon(Icons.Default.Cancel, contentDescription = "Wrong", tint = Color(0xFFDC2626))
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
