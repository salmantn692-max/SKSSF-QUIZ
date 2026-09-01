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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.*
import com.example.ui.QuizAdminViewModel
import com.example.ui.components.AudioMediaSelector
import com.example.ui.components.ImageMediaSelector
import com.example.ui.components.SAMPLE_IMAGE_PRESETS
import com.example.ui.theme.*

@Composable
fun QuestionBankScreen(
    viewModel: QuizAdminViewModel,
    modifier: Modifier = Modifier
) {
    val stages by viewModel.stages.collectAsState()
    val allQuestions by viewModel.questions.collectAsState()
    val selectedStageFilter by viewModel.selectedStageFilter.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var questionToEdit by remember { mutableStateOf<QuizQuestion?>(null) }
    var questionToDelete by remember { mutableStateOf<QuizQuestion?>(null) }

    val filteredQuestions = remember(allQuestions, selectedStageFilter, searchQuery) {
        allQuestions.filter { q ->
            val matchesStage = selectedStageFilter == null || q.stageId == selectedStageFilter
            val matchesSearch = searchQuery.isBlank() ||
                    q.questionText.contains(searchQuery, ignoreCase = true) ||
                    q.optionA.contains(searchQuery, ignoreCase = true) ||
                    q.optionB.contains(searchQuery, ignoreCase = true) ||
                    q.optionC.contains(searchQuery, ignoreCase = true) ||
                    q.optionD.contains(searchQuery, ignoreCase = true)
            matchesStage && matchesSearch
        }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    questionToEdit = null
                    showAddEditDialog = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("+ പുതിയ ചോദ്യം", fontWeight = FontWeight.Bold) },
                containerColor = BentoPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            )
        },
        containerColor = BentoBackground
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "ചോദ്യ ശേഖരം (Question Bank)",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoOnSurface
                )
                Text(
                    text = "MCQ, ചിത്ര ക്ലൂകൾ, ശബ്ദരേഖ ചോദ്യങ്ങൾ എന്നിവ ക്രമീകരിക്കുക. ഓരോ ചോദ്യത്തിനും മാർക്കും ടൈമറും നൽകാം.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoOnSurfaceVariant
                )
            }

            // Search Bar in Bento Style
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ചോദ്യങ്ങളോ ഉത്തരങ്ങളോ തിരയുക...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = BentoPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Stage Filter Bento Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedStageFilter == null,
                            onClick = { viewModel.selectedStageFilter.value = null },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text("എല്ലാ റൗണ്ടുകളും (${allQuestions.size})", fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPastelPurple,
                                selectedLabelColor = BentoDeepPurple
                            )
                        )
                    }
                    items(stages) { stage ->
                        val count = allQuestions.count { it.stageId == stage.id }
                        val isSelected = selectedStageFilter == stage.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedStageFilter.value = if (isSelected) null else stage.id },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text("${stage.title} ($count)", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPastelPurple,
                                selectedLabelColor = BentoDeepPurple
                            )
                        )
                    }
                }
            }

            if (filteredQuestions.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = BentoSurface,
                        border = BorderStroke(1.dp, BentoOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(BentoPastelPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = BentoDeepPurple
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "ചോദ്യങ്ങളൊന്നും കണ്ടെത്തിയില്ല",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BentoOnSurface
                            )
                            Text(
                                text = "'+ പുതിയ ചോദ്യം' ബട്ടൺ അമർത്തി ചോദ്യങ്ങൾ ചേർക്കുക.",
                                style = MaterialTheme.typography.bodySmall,
                                color = BentoOnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredQuestions, key = { it.id }) { question ->
                    val stage = stages.find { it.id == question.stageId }
                    QuestionBentoCard(
                        question = question,
                        stageName = stage?.title ?: "റൗണ്ട് ${question.stageId}",
                        viewModel = viewModel,
                        onEdit = {
                            questionToEdit = question
                            showAddEditDialog = true
                        },
                        onDelete = { questionToDelete = question }
                    )
                }
            }
        }
    }

    // --- ADD / EDIT QUESTION DIALOG ---
    if (showAddEditDialog) {
        QuestionEditorDialog(
            question = questionToEdit,
            stages = stages,
            defaultStageId = selectedStageFilter ?: stages.firstOrNull()?.id ?: 1L,
            viewModel = viewModel,
            onDismiss = {
                showAddEditDialog = false
                questionToEdit = null
            },
            onSave = { id, stageId, text, type, optA, optB, optC, optD, correct, media, caption, points, timer, explanation ->
                viewModel.saveQuestion(
                    id = id,
                    stageId = stageId,
                    questionText = text,
                    questionType = type,
                    optionA = optA,
                    optionB = optB,
                    optionC = optC,
                    optionD = optD,
                    correctAnswer = correct,
                    mediaUri = media,
                    mediaCaption = caption,
                    points = points,
                    timerSeconds = timer,
                    explanation = explanation
                )
                showAddEditDialog = false
                questionToEdit = null
            }
        )
    }

    // --- DELETE CONFIRMATION ---
    questionToDelete?.let { q ->
        AlertDialog(
            onDismissRequest = { questionToDelete = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("ചോദ്യം നീക്കം ചെയ്യണോ?", fontWeight = FontWeight.Bold) },
            text = { Text("ഈ ചോദ്യം ഡിലീറ്റ് ചെയ്യുമെന്ന് ഉറപ്പാണോ: '${q.questionText}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteQuestion(q)
                        questionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("ഡിലീറ്റ് ചെയ്യുക", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { questionToDelete = null }) {
                    Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun QuestionBentoCard(
    question: QuizQuestion,
    stageName: String,
    viewModel: QuizAdminViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val typeBg = when (question.questionType) {
        StageType.MCQ -> BentoPastelPurple
        StageType.IMAGE_BASED -> BentoPastelPink
        StageType.AUDIO_BASED -> BentoPastelViolet
    }
    val typeTextColor = when (question.questionType) {
        StageType.MCQ -> BentoDeepPurple
        StageType.IMAGE_BASED -> BentoDeepPink
        StageType.AUDIO_BASED -> BentoDeepViolet
    }
    val typeIcon = when (question.questionType) {
        StageType.MCQ -> Icons.Default.FormatListBulleted
        StageType.IMAGE_BASED -> Icons.Default.Image
        StageType.AUDIO_BASED -> Icons.Default.GraphicEq
    }
    val typeName = when (question.questionType) {
        StageType.MCQ -> "MCQ"
        StageType.IMAGE_BASED -> "ചിത്ര ക്ലൂ"
        StageType.AUDIO_BASED -> "ഓഡിയോ ക്ലൂ"
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = BentoSurface,
        border = BorderStroke(1.dp, BentoOutline),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Stage badge, Type badge, Timer & Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = typeBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(typeIcon, contentDescription = null, tint = typeTextColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(typeName, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = typeTextColor)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoSurfaceVariant
                    ) {
                        Text(
                            text = stageName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = BentoOnSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoPastelPurple
                    ) {
                        Text(
                            text = "+${question.points} പോയിന്റ്",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BentoSurfaceVariant
                    ) {
                        Text(
                            text = "${question.timerSeconds} സെക്കൻഡ്",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = BentoOnSurface
            )

            // Image Preview (if image-based question)
            if (question.questionType == StageType.IMAGE_BASED && question.mediaUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val preset = SAMPLE_IMAGE_PRESETS.find { it.key == question.mediaUri }
                val displayUrl = preset?.imageUrl ?: question.mediaUri

                AsyncImage(
                    model = ImageRequest.Builder(context).data(displayUrl).crossfade(true).build(),
                    contentDescription = "ചോദ്യ ചിത്രം",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
                if (question.mediaCaption.isNotBlank()) {
                    Text(
                        text = question.mediaCaption,
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoOnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Audio Preview (if audio-based question)
            if (question.questionType == StageType.AUDIO_BASED && question.mediaUri.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                val playbackState by viewModel.audioPlayer.playbackState.collectAsState()
                val isPlaying = playbackState.isPlaying && playbackState.currentAudioId == question.mediaUri

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BentoPastelViolet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) viewModel.audioPlayer.stop() else viewModel.audioPlayer.playAudio(question.mediaUri, "ഓഡിയോ ക്ലൂ")
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "ഓഡിയോ കേൾക്കുക",
                                tint = BentoDeepPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "ശബ്ദം കേൾക്കുന്നു..." else "ഓഡിയോ ക്ലൂ തയ്യാറാണ്",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoDeepPurple
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                BentoOptionPill(label = "A", text = question.optionA, isCorrect = question.correctAnswer == "A")
                BentoOptionPill(label = "B", text = question.optionB, isCorrect = question.correctAnswer == "B")
                if (question.optionC.isNotBlank()) {
                    BentoOptionPill(label = "C", text = question.optionC, isCorrect = question.correctAnswer == "C")
                }
                if (question.optionD.isNotBlank()) {
                    BentoOptionPill(label = "D", text = question.optionD, isCorrect = question.correctAnswer == "D")
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = BentoPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "വിശദീകരണം: ${question.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoOnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = BentoPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("തിരുത്തുക", fontWeight = FontWeight.Bold, color = BentoPrimary)
                }
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ഡിലീറ്റ്", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BentoOptionPill(label: String, text: String, isCorrect: Boolean) {
    if (text.isBlank()) return
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCorrect) Color(0xFFE8F5E9) else BentoSurfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isCorrect) Color(0xFF10B981) else BentoOutline.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isCorrect) Color(0xFF10B981) else BentoSurfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color.White else BentoOnSurface
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isCorrect) FontWeight.Bold else FontWeight.Normal,
                color = if (isCorrect) Color(0xFF047857) else BentoOnSurface,
                modifier = Modifier.weight(1f)
            )
            if (isCorrect) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "ശരിയായ ഉത്തരം",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestionEditorDialog(
    question: QuizQuestion?,
    stages: List<QuizStage>,
    defaultStageId: Long,
    viewModel: QuizAdminViewModel,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        stageId: Long,
        text: String,
        type: StageType,
        optA: String,
        optB: String,
        optC: String,
        optD: String,
        correct: String,
        media: String,
        caption: String,
        points: Int,
        timer: Int,
        explanation: String
    ) -> Unit
) {
    var selectedStageId by remember { mutableStateOf(question?.stageId ?: defaultStageId) }
    val initialStage = stages.find { it.id == selectedStageId }
    var selectedType by remember { mutableStateOf(question?.questionType ?: initialStage?.stageType ?: StageType.MCQ) }

    var questionText by remember { mutableStateOf(question?.questionText ?: "") }
    var optionA by remember { mutableStateOf(question?.optionA ?: "") }
    var optionB by remember { mutableStateOf(question?.optionB ?: "") }
    var optionC by remember { mutableStateOf(question?.optionC ?: "") }
    var optionD by remember { mutableStateOf(question?.optionD ?: "") }
    var correctAnswer by remember { mutableStateOf(question?.correctAnswer ?: "A") }
    var mediaUri by remember { mutableStateOf(question?.mediaUri ?: "") }
    var mediaCaption by remember { mutableStateOf(question?.mediaCaption ?: "") }
    var points by remember { mutableStateOf(question?.points ?: 10) }
    var timerSeconds by remember { mutableStateOf(question?.timerSeconds ?: (initialStage?.timerSeconds ?: 30)) }
    var explanation by remember { mutableStateOf(question?.explanation ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                if (question == null) "പുതിയ ചോദ്യം ചേർക്കുക" else "ചോദ്യം തിരുത്തുക",
                fontWeight = FontWeight.Bold,
                color = BentoOnSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Target Stage Selector
                Text("ഏത് റൗണ്ടിലേക്കാണ്?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BentoOnSurface)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(stages) { stage ->
                        val isSelected = stage.id == selectedStageId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedStageId = stage.id
                                selectedType = stage.stageType
                                timerSeconds = stage.timerSeconds
                            },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text(stage.title) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPastelPurple,
                                selectedLabelColor = BentoDeepPurple
                            )
                        )
                    }
                }

                // Question Text
                OutlinedTextField(
                    value = questionText,
                    onValueChange = { questionText = it },
                    label = { Text("ചോദ്യം നൽകുക *") },
                    placeholder = { Text("ചോദ്യം ഇവിടെ ടൈപ്പ് ചെയ്യുക...") },
                    maxLines = 4,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Media Selector (Image or Audio based on question type)
                if (selectedType == StageType.IMAGE_BASED) {
                    ImageMediaSelector(
                        selectedMediaUri = mediaUri,
                        onMediaSelected = { mediaUri = it }
                    )
                    OutlinedTextField(
                        value = mediaCaption,
                        onValueChange = { mediaCaption = it },
                        label = { Text("ചിത്ര വിവരണം / ക്ലൂ സൂചന") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (selectedType == StageType.AUDIO_BASED) {
                    AudioMediaSelector(
                        selectedMediaUri = mediaUri,
                        audioPlayer = viewModel.audioPlayer,
                        onMediaSelected = { mediaUri = it }
                    )
                    OutlinedTextField(
                        value = mediaCaption,
                        onValueChange = { mediaCaption = it },
                        label = { Text("ഓഡിയോ വിവരണം / ശബ്ദ സൂചന") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Options A, B, C, D
                Text("ഉത്തര ഓപ്ഷനുകൾ", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BentoOnSurface)
                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("ഓപ്ഷൻ A *") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("ഓപ്ഷൻ B *") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionC,
                    onValueChange = { optionC = it },
                    label = { Text("ഓപ്ഷൻ C") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = optionD,
                    onValueChange = { optionD = it },
                    label = { Text("ഓപ്ഷൻ D") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Correct Answer Choice (A, B, C, D)
                Text("ശരിയായ ഓപ്ഷൻ തെരഞ്ഞെടുക്കുക", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = BentoOnSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("A", "B", "C", "D").forEach { optKey ->
                        val isSelected = correctAnswer == optKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { correctAnswer = optKey },
                            shape = RoundedCornerShape(16.dp),
                            label = { Text("ഓപ്ഷൻ $optKey", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BentoPastelPurple,
                                selectedLabelColor = BentoDeepPurple
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Points and Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = points.toString(),
                        onValueChange = { points = it.toIntOrNull() ?: 10 },
                        label = { Text("മാർക്ക് / പോയിന്റ്") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = timerSeconds.toString(),
                        onValueChange = { timerSeconds = it.toIntOrNull() ?: 30 },
                        label = { Text("ടൈമർ (സെക്കൻഡ്)") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Solution / Explanation
                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("ശരിയുത്തര വിശദീകരണം (Explanation)") },
                    placeholder = { Text("ഉത്തരം നൽകിയ ശേഷം കാണിക്കേണ്ട വസ്തുത...") },
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()) {
                        onSave(
                            question?.id ?: 0L,
                            selectedStageId,
                            questionText,
                            selectedType,
                            optionA,
                            optionB,
                            optionC,
                            optionD,
                            correctAnswer,
                            mediaUri,
                            mediaCaption,
                            points,
                            timerSeconds,
                            explanation
                        )
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text(if (question == null) "ചോദ്യം ചേർക്കുക" else "മാറ്റങ്ങൾ സേവ് ചെയ്യുക", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("റദ്ദാക്കുക", fontWeight = FontWeight.Bold, color = BentoOnSurfaceVariant)
            }
        }
    )
}
